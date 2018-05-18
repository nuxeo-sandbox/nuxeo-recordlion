package nuxeo.recordlion;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.recordlion.client.RecordLionConnection;
import nuxeo.recordlion.operations.CreateRecord;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.recordlion.nuxeo-recordlion-core")
public class TestNuxeoRecordLion {

    public static final String TEST_CONF_FILE = "private-test.conf";

    public static final String TEST_CONF_KEY_NAME_USERNAME = "test.recordlion.username";

    public static final String TEST_CONF_KEY_NAME_PASSWORD = "test.recordlion.password";

    protected String username = null;

    protected String password = null;

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    /*
     * QUICK TEST: Read from properties file (added to .gitignore)
     * @TBD Add try...catch etc.
     */
    protected void loadTestConfig() throws Exception {

        if (username != null && password != null) {
            return;
        }

        File file = null;
        FileInputStream fileInput = null;

        file = FileUtils.getResourceFileFromContext(TEST_CONF_FILE);
        fileInput = new FileInputStream(file);

        Properties props = new Properties();
        props.load(fileInput);

        username = props.getProperty(TEST_CONF_KEY_NAME_USERNAME);
        password = props.getProperty(TEST_CONF_KEY_NAME_PASSWORD);
    }

    @Test
    public void shouldAuthenticate() throws Exception {

        loadTestConfig();

        RecordLionConnection rlc = new RecordLionConnection(username, password);

        rlc.testConnection();

    }

    @Test
    public void shouldCreateRecordLionRecord() throws OperationException {
        final String path = "/default-domain";
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        DocumentModel doc = (DocumentModel) automationService.run(ctx, CreateRecord.ID, params);
        assertEquals(path, doc.getPathAsString());
    }
}
