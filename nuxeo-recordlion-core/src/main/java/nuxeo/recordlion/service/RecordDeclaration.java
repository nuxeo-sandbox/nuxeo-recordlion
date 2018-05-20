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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nuxeo.recordlion.Constants.RecordDeclarationState;

/**
 * From RecordDeclaration.cs
 *
 * @since 10.1
 */
public class RecordDeclaration {

    protected RecordDeclarationState record = RecordDeclarationState.None;

    protected RecordDeclarationState vital = RecordDeclarationState.None;

    protected RecordDeclarationState obsolete = RecordDeclarationState.None;

    protected RecordDeclarationState superseded = RecordDeclarationState.None;

    public RecordDeclaration() {

    }

    public JsonNode build() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();

        obj.put("Record", RecordDeclarationState.toInt(record));
        obj.put("Vital", RecordDeclarationState.toInt(vital));
        obj.put("Obsolete", RecordDeclarationState.toInt(obsolete));
        obj.put("Superseded", RecordDeclarationState.toInt(superseded));

        return obj;
    }

    public RecordDeclarationState getRecord() {
        return record;
    }

    public void setRecord(RecordDeclarationState record) {
        this.record = record;
    }

    public RecordDeclarationState getVital() {
        return vital;
    }

    public void setVital(RecordDeclarationState vital) {
        this.vital = vital;
    }

    public RecordDeclarationState getObsolete() {
        return obsolete;
    }

    public void setObsolete(RecordDeclarationState obsolete) {
        this.obsolete = obsolete;
    }

    public RecordDeclarationState getSuperseded() {
        return superseded;
    }

    public void setSuperseded(RecordDeclarationState superseded) {
        this.superseded = superseded;
    }

}
