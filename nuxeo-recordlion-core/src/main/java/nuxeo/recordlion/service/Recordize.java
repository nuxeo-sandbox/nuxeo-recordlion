/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package nuxeo.recordlion.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * RecordLion, has some required fields when creating a "recordization".
 * <p>
 * In the required fields, there are "@file" and "@filesize". in this v0.1 of the POC, we:
 * <ul>
 * <li>Always use file:content, when available</li>
 * <li>If there is no file (empty, or a Folderish for example) we set file to ID of the document and the filesize to
 * 10</li>
 * </ul>
 *
 * @since 10.2
 */
public class Recordize {

    // Just becaise we need a file size, while not all documents have a file (example: A Folderish)
    // When a Document has no file, the file ID will be set to the UID of the document
    public static final int DEFAULT_FILE_SIZE = 0;

    protected static final String LOCK = "Recordize_lock";

    protected DocumentModel doc;

    public Recordize(DocumentModel doc) {
        this.doc = doc;
    }

    public JsonNode buildRecord(JsonNode propertiesToAppend) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();

        obj.put("@created", getISODate((Calendar) doc.getPropertyValue("dc:created")));

        Blob blob = doc.hasSchema("file") ? (Blob) doc.getPropertyValue("file:content") : null;
        if(blob != null) {
            obj.put("@file", blob.getDigest());
            obj.put("@filesize", blob.getLength());
        } else {
            obj.put("@file", doc.getId());
            obj.put("@filesize", DEFAULT_FILE_SIZE);
        }

        obj.put("@folder", "Folder");// Put any value here

        obj.put("@modified", getISODate((Calendar) doc.getPropertyValue("dc:created")));

        obj.put("@repo", "nuxeo");

        // So hard to just get the permalink from the document.
        // Let's hard code all this currently
        String uri = "https://gartner2018.nuxeo.com/ui/#!/doc/" + doc.getId();
        obj.put("@uri", uri);

        if(propertiesToAppend != null) {
            obj.setAll((ObjectNode) propertiesToAppend);
        }

        return obj;
    }

    protected String getISODate(Calendar date) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
        String isoStr = df.format(date.getTimeInMillis());

        return isoStr;
    }
}
