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

/**
 * @since 10.1
 */
public class Constants {

    public static final String CONF_KEY_BASE_URL = "nuxeo.recordlion.baseurl";

    public static final String CONF_KEY_LOGIN = "nuxeo.recordlion.login";

    public static final String CONF_KEY_PASSWORD = "nuxeo.recordlion.password";

    /*
     * The end point constants have the same labels as the .NET SDK, the pattern changes a little (using %s instead of
     * parameter numbers). See comments for the changes added here
     */
    public static final String POST_RECORDIZERS = "/api/v1/recordization";

    // Original is DELETE_RECORDIZERS which allows for deletion of one doc or for children (using "path starts with")
    // We limit to a single deletion
    //public static final String DELETE_RECORDIZERS = "/api/v1/recordization?uri={0}&all={1}";
    public static final String DELETE_ONE_RECORD = "/api/v1/recordization?uri=%s";

    // We hard code page and pageSize
    public static final String GET_RECORDCLASSES_ALL = "/api/v1/recordclasses?all=true&page=0&pageSize=10";

    // We hard code page and pageSize
    public static final String GET_RECORDCLASSES_CONTAINING_TITLEORCODE = "/api/v1/recordclasses?titleOrCode=%s&page=0&pageSize=1";

    // We hard code page and pageSize
    public static final String GET_PENDING_ACTIONITEMS_CONTAINING_RECORDURI = "/api/v1/actionitemspending?recordUri=%s&page=0&pageSize=10";

}
