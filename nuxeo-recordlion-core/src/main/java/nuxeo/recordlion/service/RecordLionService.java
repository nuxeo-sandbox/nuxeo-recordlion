package nuxeo.recordlion.service;

import java.io.IOException;

import org.nuxeo.ecm.core.api.DocumentModel;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Ultimately, the ervice should expose wrappers around the APIs, at least the most used one. This like
 * getRecordClasses, getRecordClassForIdOrName, recorizeNewRecord, ...
 * <p>
 * In this POC, we have to make it simple and implement just what we need for  demo.
 *
 * @since 10.1
 */
public interface RecordLionService {

    public RecordLionDescriptor getDescriptor();

    /**
     * Creates the record in the distant recordlion server, returns true if all went well. (the record is not
     * immediately available in record lion, you need to regularly pull info to check when it is really created nd
     * available)
     *
     * @param doc
     * @return
     * @throws IOException
     * @since 10.2
     */
    JsonNode createRecordForDocument(DocumentModel doc, JsonNode propertiesToAppend) throws IOException;

    public JsonNode deleteRecord(DocumentModel doc);

    public JsonNode pullInfo(DocumentModel doc);

    public JsonNode callGET(String api) throws IOException;

    public JsonNode callPOST(String api, String body) throws IOException;

}
