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
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @since 10.1
 */
@XObject("configuration")
public class RecordLionDescriptor {

    protected boolean baseUrlChecked = false;

    @XNode("name")
    protected String name = "";

    @XNode("baseUrl")
    protected String baseUrl = "";

    @XNode("login")
    protected String login = "";

    @XNode("password")
    protected String password = "";

    @XNode("defaultRecordClassId")
    protected String defaultRecordClassId = "0";

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        // I just hate the idea of testing this for every call...
        if (!baseUrlChecked) {
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(1);
            }
            baseUrlChecked = true;
        }
        return baseUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public long getDefaultRecordClassId() {
        if(StringUtils.isBlank(defaultRecordClassId)) {
            return 0L;
        }

        return Long.valueOf(defaultRecordClassId);
    }

}
