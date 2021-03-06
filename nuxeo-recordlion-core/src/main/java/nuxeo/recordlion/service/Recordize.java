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

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nuxeo.recordlion.Constants;

/**
 * (Inspired from Recordize.cs)
 *
 * @since 10.1
 */
public class Recordize {

    protected DocumentModel doc;

    protected String uri;

    protected int state;

    protected String title;

    protected String description;

    protected long recordClassId;

    protected boolean IsManuallyClassified;

    public Recordize(DocumentModel doc, long recordClassId,
            boolean isManuallyClassified) {
        super();

        this.doc = doc;
        uri = Constants.getUrl(doc);
        state = Constants.RECORDSTATE_NEW_OR_MODIFIED;
        title = doc.getTitle();
        description = "";
        this.recordClassId = recordClassId;
        IsManuallyClassified = isManuallyClassified;
    }

    public void setDescription(String description) {

        if(StringUtils.isBlank(description)) {
            this.description = "Nuxeo Document";
        } else {
            this.description = description;
        }
    }

    public JsonNode build() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();

        obj.put("State", state);
        obj.put("Title", title);
        obj.put("Uri", uri);
        obj.put("Description", description);

        // "To use automatic classification, set `IsManuallyClassified` to `false` and do not send a record class ID"
        if(IsManuallyClassified) {
            obj.put("RecordClassId", recordClassId);
        }
        obj.put("IsManuallyClassified", IsManuallyClassified);

        obj.put("isRecord", true);

        return obj;
    }

}
