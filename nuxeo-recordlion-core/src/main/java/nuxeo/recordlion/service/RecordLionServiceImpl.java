package nuxeo.recordlion.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nuxeo.recordlion.Constants;
import nuxeo.recordlion.Constants.LifecyclePhaseAction;
import nuxeo.recordlion.Constants.RecordDeclarationState;

/**
 * The service to handle RecordLion connector.
 * <p>
 * First implementation (2018-05): handles only one configuration (no connection to multiple RecordLion servers)
 * <p>
 * So, the flow as expected by recordLion API is something like:
 * <ul>
 * <li>Create the record via "recordization"</li>
 * <li>Regularly pull "action items" to catch when RecorLion actually has finished preparing the record</li>
 * <li>Post the corresponding action item (for example, retention in RecordLion => must lock in nuxeo and tell
 * RecordLion it is done)</li>
 * <li>Same cycle for other actions: delet in recordlion => pulll actions => send "Nuxeo did delete" => etc.</li>
 * <li></li>
 * </ul>
 * <p>
 * WARNOING WARNING WARNING: RecordLion uses the uri of the document uniqueley identify it. We will use the ID url, not
 * the path. This means:
 * <ul>
 * <li>Cannot use the "delete everything in a folder" from Record Lion (using a "uri starts with"</li>
 * <li>But more reliable, somehow</li>
 * <li>(that said, not sure we need to over complicate the thing, at least we don't need in the context of the POC)</li>
 * </ul>
 *
 * @since 10.1
 */
public class RecordLionServiceImpl extends DefaultComponent implements RecordLionService {

    private static final Log log = LogFactory.getLog(RecordLionServiceImpl.class);

    public static final int DEFAULT_PULLACTIONS_TIMEOUT_SECONDS = 60;

    protected static final String XP = "configuration";

    protected RecordLionDescriptor config = null;

    protected String basicAuthenticationString;

    /**
     * Component activated notification. Called when the component is activated. All component dependencies are resolved
     * at that moment. Use this method to initialize the component.
     *
     * @param context the component context.
     */
    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    /**
     * Component deactivated notification. Called before a component is unregistered. Use this method to do cleanup if
     * any and free any resources held by the component.
     *
     * @param context the component context.
     */
    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
    }

    /**
     * Application started notification. Called after the application started. You can do here any initialization that
     * requires a working application (all resolved bundles and components are active at that moment)
     *
     * @param context the component context. Use it to get the current bundle context
     * @throws Exception
     */
    @Override
    public void applicationStarted(ComponentContext context) {
        // do nothing by default. You can remove this method if not used.
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (XP.equals(extensionPoint)) {
            if (contribution instanceof RecordLionDescriptor) {
                config = (RecordLionDescriptor) contribution;
                basicAuthenticationString = "Basic "
                        + new String(Base64.encodeBase64((config.getLogin() + ":" + config.getPassword()).getBytes()));
            } else {
                throw new NuxeoException("Invalid descriptor: " + contribution.getClass());
            }
        } else {
            throw new NuxeoException("Invalid extension point: " + extensionPoint);
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        // Logic to do when unregistering any contribution
    }

    @Override
    public RecordLionDescriptor getDescriptor() {
        return config;
    }

    @Override
    public JsonNode recordizeDocument(DocumentModel doc, long recordClassId, boolean isManuallyClassified,
            JsonNode propertiesToAppend) throws IOException {

        Recordize recordize = new Recordize(doc, recordClassId, isManuallyClassified);
        ObjectNode recordizeNode = (ObjectNode) recordize.build();
        String uri = recordizeNode.get("Uri").asText();

        // =============================
        Record rec = new Record(doc);
        JsonNode recordJson = rec.buildAsRecordizePropertyArray(propertiesToAppend, uri);
        recordizeNode.set("Properties", recordJson); // recordizeNode.setAll((ObjectNode) recordJson);
        // =============================

        ObjectMapper mapper = new ObjectMapper();
        // For "Recordization", we lmust send an array or Recordize
        ArrayNode array = mapper.createArrayNode();
        array.add(recordizeNode);

        String body = mapper.writeValueAsString(array);
        JsonNode result = callWithBody("POST", Constants.POST_RECORDIZERS, body, false);

        // Result is an array of one in this case (we created one record only)
        array = (ArrayNode) result;
        JsonNode object = array.get(0);

        return object;
    }

    @Override
    public List<Constants.LifecyclePhaseAction> pullActions(DocumentModel doc, String forceUri) throws IOException {

        String endPoint;

        if (StringUtils.isBlank(forceUri)) {
            endPoint = String.format(Constants.GET_PENDING_ACTIONITEMS_CONTAINING_RECORDTITLEORURI,
                    Constants.getUrl(doc));
        } else {
            endPoint = String.format(Constants.GET_PENDING_ACTIONITEMS_CONTAINING_RECORDTITLEORURI, forceUri);
        }

        System.out.print("pullActions endPoint:\n" + endPoint + "\n");

        JsonNode result = callGET(endPoint);

        List<Constants.LifecyclePhaseAction> actions = new ArrayList<Constants.LifecyclePhaseAction>();
        ArrayNode items = (ArrayNode) result.get("Items");
        items.forEach((JsonNode obj) -> {
            int action = obj.get("Action").asInt();
            actions.add(LifecyclePhaseAction.fromInt(action));
        });

        return actions;
    }

    @Override
    public JsonNode declareRecordForIdentifier(String recordIdentifier) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        RecordDeclaration recDecl = new RecordDeclaration();
        recDecl.setRecord(RecordDeclarationState.Declare);
        JsonNode recordDeclarationJson = recDecl.build();

        String body = mapper.writeValueAsString(recordDeclarationJson);
        String endPoint = String.format(Constants.PUT_RECORD_DECLARATION_WITH_IDENTIFIER, recordIdentifier);
        JsonNode result = callWithBody("PUT", endPoint, body, true);

        return result;

    }


    @Override
    public JsonNode createRecord(DocumentModel doc, long recordClassId, boolean isManuallyClassified,
            long timeOutInSeconds) throws IOException {

        JsonNode result = null;

        // ============================================================
        // 1. Recordize
        // ============================================================
        ObjectNode resordizeJson = (ObjectNode) recordizeDocument(doc, recordClassId, isManuallyClassified, null);
        String recordIdentifier = resordizeJson.get("Identifier").asText();

        // ============================================================
        // 2. Get the DeclareRecord action
        // ============================================================
        String uri = resordizeJson.get("Uri").asText();
        String title = resordizeJson.get("Title").asText();

        boolean timedOut = false;
        boolean gotIt = false;
        if (timeOutInSeconds < 10) {
            timeOutInSeconds = DEFAULT_PULLACTIONS_TIMEOUT_SECONDS;
        }
        long timeOutMS = timeOutInSeconds * 1000;
        long startTime = System.currentTimeMillis();
        System.out.println("==========> timeOutMS: " + timeOutMS);
        System.out.println("==========> uri: " + uri);
        do {
            try {
                // WARNING: CALLING THIS WITH THE uri FAILS WITH A 404... Must call with the title
                List<Constants.LifecyclePhaseAction> actions = pullActions(null, title);
                gotIt = actions.contains(LifecyclePhaseAction.DeclareRecord);
                if (!gotIt) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    timedOut = timeOutMS > 0 && (System.currentTimeMillis() - startTime) > timeOutMS;
                }
            } catch (IOException e) {
                log.error("Got an error in pullActions(): " + e.getMessage());
                System.out.println("==========> Got an error in pullActions(): " + e.getMessage());

                if(e.getMessage().equals("Failed connecting to the server with HTTP result code: 404")) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException other) {
                        // Ignore
                    }
                    timedOut = timeOutMS > 0 && (System.currentTimeMillis() - startTime) > timeOutMS;
                } else {
                    throw e;
                }

            }
        } while (!gotIt && !timedOut);

        // ============================================================
        // 3. Declare the Record
        // ============================================================
        if (gotIt) {

            result = declareRecordForIdentifier(recordIdentifier);
            ((ObjectNode) result).put("result", "OK");
        } else {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readTree("{\"result\":\"KO\"}");
        }

        // If we are here, then all went well. Make sure to add info for the caller
        // since PUT_RECORD_DECLARATION_WITH_IDENTIFIER is a void operation (we added a "result" return code)
        ((ObjectNode) result).put("uri", uri);
        ((ObjectNode) result).put("title", title);
        ((ObjectNode) result).put("recordIdentifier", recordIdentifier);

        return result;
    }

    @Override
    public JsonNode deleteRecord(DocumentModel doc) throws IOException {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode callGET(String api) throws IOException {

        JsonNode node = null;

        String urlStr = buildUrl(api);
        URL url = new URL(urlStr);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            addDefaultHeaders(connection);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                
                try(InputStream stream = connection.getInputStream()) {
                    String jsonStr = IOUtils.toString(stream, UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    node = mapper.readTree(jsonStr);
                }
                
            } else {
                throw new IOException("Failed connecting to the server with HTTP result code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return node;
    }

    @Override
    public JsonNode callWithBody(String httpVerb, String api, String body, boolean noResponseExpected)
            throws IOException {

        if (StringUtils.isBlank(httpVerb) || (!httpVerb.equals("POST") && !httpVerb.equals("PUT"))) {
            throw new IllegalArgumentException("Invalid http verb");
        }

        JsonNode node = null;

        String urlStr = buildUrl(api);
        URL url = new URL(urlStr);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            addDefaultHeaders(connection);

            connection.setRequestMethod(httpVerb);

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            ObjectMapper mapper = new ObjectMapper();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                try(InputStream stream = connection.getInputStream()) {
                    String jsonStr = IOUtils.toString(stream, UTF_8);
                    node = mapper.readTree(jsonStr);
                }

            } else if (noResponseExpected && responseCode == 204) {
                // Ok, on response expected, 204 NO CONTENT was returned
                ObjectNode obj = mapper.createObjectNode();
                obj.put("result", "OK");
                obj.put("responseCode", responseCode);
                node = obj;
            } else {
                throw new IOException("Failed connecting to the server with HTTP result code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return node;
    }

    @Override
    public long getDefaultRecordClassId() {
        return config.getDefaultRecordClassId();
    }

    protected String buildUrl(String api) {
        if (!api.startsWith("/")) {
            api = "/" + api;
        }
        return config.getBaseUrl() + api;
    }

    protected void addDefaultHeaders(HttpURLConnection connection) {

        connection.setRequestProperty("Authorization", basicAuthenticationString);
        connection.setRequestProperty("Content-Type", "application/json");

    }

}
