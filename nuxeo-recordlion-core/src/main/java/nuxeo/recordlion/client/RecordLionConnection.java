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
package nuxeo.recordlion.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;



/**
 * WARNING - WARNING - WARNING - WARNING - WARNING - WARNING - WARNING - WARNING
 * <p>
 * Quick'n dirty test. Nothing must be hard coded, all must be read from config, etc. etc., it's missing closing
 * resources, try...catch, etc.
 *
 * @since 10.1
 */
public class RecordLionConnection {

    protected String username;

    protected String password;

    protected String credentialType;

    // To be read from configuration, not hardcoded..
    protected String API_URL = "https://test.recordlion.net/api/v1/";

    public RecordLionConnection(String username, String password) {
        this.username = username;
        this.password = password;
        credentialType = "Forms";
    }

    public void testConnection() throws IOException {

        String url = API_URL + "recordclasses?all=true&page={0}&pageSize={10}";
        URL theUrl = new URL(url);

        HttpsURLConnection connection = null;
        connection = (HttpsURLConnection) theUrl.openConnection();

        // Quick test with Basic. Don't think iot will work
        // => it does not :-)
        String basicAuthentication = "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
        connection.setRequestProperty("Authorization", basicAuthentication);
        connection.setRequestProperty("Content-Type", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

            InputStream stream = null;

            stream = connection.getInputStream();

            String responseStr = IOUtils.toString(stream, UTF_8);

            stream.close();
        }

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCredentialType() {
        return credentialType;
    }

}
