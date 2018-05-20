/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
package nuxeo.recordlion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.SimpleFeature;

/**
 * Important: To test the feature, we don't want to hard code the credentials
 * (since this code is published on GitHub). There are two ways to inject the
 * values for testing:
 * <ul>
 * <li>Use environment variables: Setup your environment and inject the expected
 * variables. This would be used when automating testing with maven for example
 * (passing the env. variables to maven)</li>
 *
 * <li>Use the (git ignored) "private-test.conf" file:
 * <ul>
 * <li>We have a file named private-test.conf at
 * nuxeo-recordlion-core/src/test/resources/</li>
 * <li>The file contains the apiUrl, login, ...</li>
 * <li>The .gitignore config file ignores this file, so it is not sent on
 * GitHub</li>
 * </ul>
 *
 * </li>
 * </ul>
 *
 * So, basically to run the test locally, create this file at
 * nuxeo-recordlion-core/src/test/resources/ and set the following properties:
 *
 * <pre>
 * {@code
 * nuxeo.recordlion.apiurl=THE_FULL_API_URL, like https://server.com/api/v1/
 * nuxeo.recordlion.login=THE_LOGIN
 * nuxeo.recordlion.password=THE_PASSWORD
 * }
 * </pre>
 * </ul>
 *
 * Whatever you choose, the properties will be loaded and set in the
 * environment
 *
 * @since   10.1
 */
public class SimpleFeatureCustom extends SimpleFeature {

	public static final String TEST_CONF_FILE = "private-test.conf";

	protected static Properties props = null;

	public static String getLocalProperty(String key) {

		if (props != null) {
			return props.getProperty(key);
		}

		return null;
	}

	public static boolean hasLocalTestConfiguration() {
		return props != null;
	}

	@Override
	public void initialize(FeaturesRunner runner) throws Exception {

		File file = null;
		FileInputStream fileInput = null;
		try {
			file = FileUtils.getResourceFileFromContext(TEST_CONF_FILE);
			fileInput = new FileInputStream(file);
			props = new Properties();
			props.load(fileInput);

		} catch (Exception e) {
			props = null;
		} finally {
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
					// Ignore
				}
				fileInput = null;
			}
		}

		if (props == null) {
			// Try to get environment variables
			addEnvironmentVariable(Constants.CONF_KEY_BASE_URL);
			addEnvironmentVariable(Constants.CONF_KEY_LOGIN);
			addEnvironmentVariable(Constants.CONF_KEY_PASSWORD);
		}

		if (props != null) {

			Properties systemProps = System.getProperties();
			systemProps.setProperty(Constants.CONF_KEY_BASE_URL,
					props.getProperty(Constants.CONF_KEY_BASE_URL));
			systemProps.setProperty(Constants.CONF_KEY_LOGIN,
					props.getProperty(Constants.CONF_KEY_LOGIN));
			systemProps.setProperty(Constants.CONF_KEY_PASSWORD,
					props.getProperty(Constants.CONF_KEY_PASSWORD));

		}
	}

	@Override
	public void stop(FeaturesRunner runner) throws Exception {

		Properties p = System.getProperties();
		p.remove(Constants.CONF_KEY_BASE_URL);
		p.remove(Constants.CONF_KEY_LOGIN);
		p.remove(Constants.CONF_KEY_PASSWORD);
	}

	protected void addEnvironmentVariable(String key) {
		String value = System.getenv(key);
		if(value != null) {
			if(props == null) {
				props = new Properties();
			}
			props.put(key, value);
		}
	}

}
