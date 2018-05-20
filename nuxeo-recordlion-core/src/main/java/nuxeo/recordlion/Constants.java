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
 *     Thibaud ARguillere
 */
package nuxeo.recordlion;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 10.1
 */
public class Constants {

    private static final Log log = LogFactory.getLog(Constants.class);

    public static final String CONF_KEY_BASE_URL = "nuxeo.recordlion.baseurl";

    public static final String CONF_KEY_LOGIN = "nuxeo.recordlion.login";

    public static final String CONF_KEY_PASSWORD = "nuxeo.recordlion.password";

    public static final String CONF_KEY_DEFAULT_RECORDCLASSID = "nuxeo.recordlion.defaultRecordClassId";

    public static final int RECORDSTATE_NEW_OR_MODIFIED = 0;

    public static final int RECORDSTATE_REMOVED = 0;

    public enum LifecyclePhaseAction {
        None, Transfer, Workflow, DeclareRecord, UndeclareRecord, DisposeDelete, DisposeTransfer, Permanent, DisposeRecycle;

        private static final LifecyclePhaseAction[] allValues = values();

        public static LifecyclePhaseAction fromInt(int n) {
            return allValues[n];
        }
    }

    public enum RecordDeclarationState {
        None, Declare, Undeclare;

        public static int toInt(RecordDeclarationState value) {
            switch (value) {
            case None:
                return 0;

            case Declare:
                return 1;

            case Undeclare:
                return 2;

            default:
                return 0;
            }
        }
    }

    public enum RecordizePropertyState {
        Default, // Overwritten with each Property Bag Update
        Static, // Overwritten if provided in Property Bag, but never deleted
        Constant; // Never deleted or overwritten

        public static int toInt(RecordizePropertyState value) {
            switch (value) {
            case Default:
                return 0;

            case Static:
                return 1;

            case Constant:
                return 2;

            default:
                return 0;
            }
        }
    }

    /*
     * The end point constants have the same labels as the .NET SDK, the pattern changes a little (using %s instead of
     * parameter numbers). See comments for the changes added here
     */
    public static final String POST_RECORDIZERS = "/api/v1/recordization";

    // Original is DELETE_RECORDIZERS which allows for deletion of one doc or for children (using "path starts with")
    // We limit to a single deletion
    // public static final String DELETE_RECORDIZERS = "/api/v1/recordization?uri={0}&all={1}";
    public static final String DELETE_ONE_RECORD = "/api/v1/recordization?uri=%s";

    // We hard code page and pageSize
    public static final String GET_RECORDCLASSES_ALL = "/api/v1/recordclasses?all=true&page=0&pageSize=10";

    // We hard code page and pageSize
    public static final String GET_RECORDCLASSES_CONTAINING_TITLEORCODE = "/api/v1/recordclasses?titleOrCode=%s&page=0&pageSize=1";

    // We hard code page and pageSize
    public static final String GET_PENDING_ACTIONITEMS_CONTAINING_RECORDURI = "/api/v1/actionitemspending?recordUri=%s&page=0&pageSize=10";

    // We hard code page and pageSize
    public static final String GET_PENDING_ACTIONITEMS_CONTAINING_RECORDTITLEORURI = "/api/v1/actionitemspending?recordTitleOrUri=%s&page=0&pageSize=10";

    public static final String PUT_RECORD_DECLARATION = "/api/v1/records?uri=%s";

    public static final String PUT_RECORD_DECLARATION_WITH_ID = "/api/v1/records?id=%s";

    public static final String PUT_RECORD_DECLARATION_WITH_IDENTIFIER = "/api/v1/records?identifier=%s";

    /*
     * -------------------> TO BE REMOVED ONCE WE KNOW THE API TO BUILD A URL FROM A DocumentModel
     * <-----------------------
     */
    protected static String baseUrl = null;

    protected static String LOCK = "getUrl_LOCK";

    public static String getUrl(DocumentModel doc) {
        // So hard to just get the permalink from the document.
        // Let's hard code all this currently
        if (baseUrl == null) {
            synchronized (LOCK) {
                if (baseUrl == null) {
                    baseUrl = Framework.getProperty("nuxeo.url");
                    if (StringUtils.isBlank(baseUrl)) {
                        log.warn("No nuxeo.url property found => hard coding localhost:8080");
                        baseUrl = "http://localhost:8080";
                    }
                    if (!baseUrl.endsWith("/")) {
                        baseUrl += "/";
                    }
                }
            }
        }

        return baseUrl + "ui/#!/doc/" + doc.getId();
    }

}
