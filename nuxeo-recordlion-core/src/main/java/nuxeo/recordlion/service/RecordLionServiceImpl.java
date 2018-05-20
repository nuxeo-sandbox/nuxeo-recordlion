package nuxeo.recordlion.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nuxeo.recordlion.Constants;

/**
 * The service to handle RecordLion connector.
 * <p>
 * First implementation (2018-05): handles only one configuration (no connection to multiple RecordLion servers)
 * <p>
 * So, the flow as expected by recordLion API is something  like:
 * <ul>
 * <li>Create the record via "recordization"</li>
 * <li>Regularly pull "action items" to catch when RecorLion actually has finished preparing the record</li>
 * <li>Post the corresponding action item (for example, retention in RecordLion => must lock in nuxeo and tell RecordLion it is done)</li>
 * <li>Same cycle for other actions: delet in recordlion => pulll actions => send "Nuxeo did delete" => etc.</li>
 * <li></li>
 * </ul>
 * <p>
 * WARNOING WARNING WARNING: RecordLion uses the uri  of the document uniqueley identify it. We will use the ID url, not the path. This means:
 * <ul>
 * <li>Cannot use the "delete everything in a folder" from Record Lion (using a "uri starts with"</li>
 * <li>But more reliable, somehow</li>
 * <li>(that said, not sure we need to over complicate the thing, at least we don't need in the context of the POC)</li>
 * </ul>
 *
 * @since 10.1
 */
public class RecordLionServiceImpl extends DefaultComponent implements RecordLionService {

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
    public JsonNode createRecordForDocument(DocumentModel doc, JsonNode propertiesToAppend) throws IOException {

        JsonNode result = null;

        Recordize recordize = new Recordize(doc);
        JsonNode mainNode = recordize.buildRecord(propertiesToAppend);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(mainNode);

        result = callPOST(Constants.POST_RECORDIZERS, body);

        return result;
    }

    @Override
    public JsonNode deleteRecord(DocumentModel doc) {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode pullInfo(DocumentModel doc) {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode callGET(String api) throws IOException {

        JsonNode node = null;
        InputStream stream = null;

        String urlStr = buildUrl(api);
        URL url = new URL(urlStr);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            addDefaultHeaders(connection);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                stream = connection.getInputStream();

                String jsonStr = IOUtils.toString(stream, UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                node = mapper.readTree(jsonStr);
            } else {
                throw new IOException("Failed connecting to the server with HTTP result code: " + responseCode);
            }
        } finally {
            IOUtils.closeQuietly(stream);
            if(connection != null) {
                connection.disconnect();
            }
        }

        return node;
    }

    @Override
    public JsonNode callPOST(String api, String body) throws IOException {

        JsonNode node = null;
        InputStream stream = null;

        String urlStr = buildUrl(api);
        URL url = new URL(urlStr);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            addDefaultHeaders(connection);

            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
            wr.writeBytes (body);
            wr.flush ();
            wr.close ();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                stream = connection.getInputStream();

                String jsonStr = IOUtils.toString(stream, UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                node = mapper.readTree(jsonStr);
            } else {
                throw new IOException("Failed connecting to the server with HTTP result code: " + responseCode);
            }
        } finally {
            IOUtils.closeQuietly(stream);
            if(connection != null) {
                connection.disconnect();
            }
        }

        return node;
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
