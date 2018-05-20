package nuxeo.recordlion;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import nuxeo.recordlion.operations.CreateRecord;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, SimpleFeatureCustom.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.recordlion.nuxeo-recordlion-core")
public class TestNuxeoRecordLion {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Before
    public void setup() throws Exception {
/*
        if(SimpleFeatureCustom.hasLocalTestConfiguration()) {
            // Sanity check
            String value = SimpleFeatureCustom.getLocalProperty(RecordLionService.CONF_KEY_API_URL);
            assertTrue("Missing " + RecordLionService.CONF_KEY_API_URL, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(RecordLionService.CONF_KEY_LOGIN);
            assertTrue("Missing " + RecordLionService.CONF_KEY_LOGIN, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(RecordLionService.CONF_KEY_PASSWORD);
            assertTrue("Missing " + RecordLionService.CONF_KEY_PASSWORD, StringUtils.isNotBlank(value));
        }
*/
    }

    @Ignore
    @Test
    public void shouldAuthenticate() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        //RecordLionConnection rlc = new RecordLionConnection(username, password);

        //rlc.testConnection();

    }

    @Ignore
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
