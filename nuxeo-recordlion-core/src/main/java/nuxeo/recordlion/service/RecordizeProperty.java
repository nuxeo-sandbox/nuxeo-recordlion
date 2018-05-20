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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nuxeo.recordlion.Constants.RecordizePropertyState;

/**
 *
 * @since 10.1
 */
public class RecordizeProperty {

    protected String key;

    protected String value;

    protected RecordizePropertyState state = RecordizePropertyState.Default;

    public RecordizeProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public RecordizeProperty(String key, String value, RecordizePropertyState state) {
        this.key = key;
        this.value = value;
        this.state = state;

    }

    ObjectNode build() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();

        obj.put("Key", key);
        obj.put("Value", value);
        obj.put("State", RecordizePropertyState.toInt(state));

        return obj;

    }
}
