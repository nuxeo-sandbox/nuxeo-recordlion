package nuxeo.recordlion.operations;

import java.io.IOException;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nuxeo.recordlion.service.RecordLionService;

/**
 * Creates a record in RecordLion. MUST BE CALLED ASYNCHRNOUSELY (calls to the distant server, etc.)
 * <p>
 * Set the contextVariable whoie name is passed as parameter to the JSON string of the returned value.
 *
 * @since 10.1
 */
@Operation(id = CreateRecord.ID, category = Constants.CAT_SERVICES, label = "RecordLion: Create Record", description = "Creates a record in the RecordLion server (setup in nuxeo.conf). Returns the JSON String of the result. The al SHOULD BE ASYNCHRNOUS because it cas take time.")
public class CreateRecord {

    public static final String ID = "RecordLion.CreateRecord";

    @Context
    protected CoreSession session;

    @Context
    protected RecordLionService recordlionservice;

    @Context
    protected OperationContext ctx;

    @Param(name = "resultVarName", required = true)
    protected String resultVarName;

    @Param(name = "recordClassId", required = false)
    protected Long recordClassId = null;

    @Param(name = "isManuallyClassified", required = false)
    protected boolean isManuallyClassified = false;

    @Param(name = "timeOutInSeconds", required = false)
    protected int timeOutInSeconds = 0;

    @OperationMethod
    public DocumentModel run(DocumentModel input) throws JsonProcessingException {

        String resultStr = "";

        if (recordClassId == null || recordClassId == 0) {
            recordClassId = recordlionservice.getDefaultRecordClassId();
        }

        try {
            JsonNode node = recordlionservice.createRecord(input, recordClassId, isManuallyClassified,
                    timeOutInSeconds);
            ObjectMapper mapper = new ObjectMapper();
            resultStr = mapper.writeValueAsString(node);

        } catch (IOException e) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();

            result.put("result", "KO");
            result.put("message", e.getMessage());
            resultStr = mapper.writeValueAsString(result);
        }

        ctx.put(resultVarName, resultStr);

        return input;
    }
}
