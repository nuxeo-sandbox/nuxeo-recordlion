package nuxeo.recordlion.service;

import java.io.IOException;
import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;

import com.fasterxml.jackson.databind.JsonNode;

import nuxeo.recordlion.Constants;

/**
 * Ultimately, the ervice should expose wrappers around the APIs, at least the most used one. This like
 * getRecordClasses, getRecordClassForIdOrName, recorizeNewRecord, ...
 * <p>
 * In this POC, we have to make it simple and implement just what we need for demo.
 *
 * @since 10.1
 */
public interface RecordLionService {

    public RecordLionDescriptor getDescriptor();

    /**
     * Creates the Recordization in the distant recordlion server, returns the inof. (the record is not immediately
     * available in record lion, you need to regularly pullActions() to check when it is time to actually create it
     * (action DeclareRecord).
     *
     * @param doc
     * @return
     * @throws IOException
     * @since 10.2
     */
    public JsonNode recordizeDocument(DocumentModel doc, long recordClassId, boolean isManuallyClassified,
            JsonNode propertiesToAppend) throws IOException;

    // (forceUri is for testing)
    public List<Constants.LifecyclePhaseAction> pullActions(DocumentModel doc, String forceUri) throws IOException;

    public JsonNode declareRecordForIdentifier(String recordIdentifier) throws IOException;


    /**
     * WARNING: This can take time beofr RecordLion prepares the action, etc. This shoudl never be called synchrnousely,
     * in the UI, etc.
     * <p>
     * It 1. Recordize 2. Regularly pull until the DeclareRecord action item is received and 3. Declare the record.
     * <p>
     * <b>WARNING</b><br>
     * =====================================<br>
     * This API is useful only if you set the LifeCycle to start retention immediately ("created + 0 days")<br>
     * If the policy creates the retention in 1 month, the call will fail.<br>
     * This was good in the context of the POC.
     * =====================================<br>
     *
     * @param doc
     * @param recordClassId
     * @param isManuallyClassified
     * @param timeOutInSeconds the timeout. Any value < 10 is reset to 10s.
     * @return
     * @throws IOException
     * @since 10.1
     */
    public JsonNode createRecord(DocumentModel doc, long recordClassId, boolean isManuallyClassified,
            long timeOutInSeconds) throws IOException;

    public JsonNode deleteRecord(DocumentModel doc) throws IOException;

    public JsonNode callGET(String api) throws IOException;

    public JsonNode callWithBody(String httpVerb, String api, String body, boolean noResponseExpected)
            throws IOException;

    public long getDefaultRecordClassId();

}
