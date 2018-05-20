package nuxeo.recordlion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import nuxeo.recordlion.service.RecordLionDescriptor;
import nuxeo.recordlion.service.RecordLionService;

/**
 * By essence, it is hard to test RecordLion beside simple GET, because creating a record, for example, can be done in
 * one call, but then the RecordLion server may take time to create it for real, with its rules, retentions, lifecycle,
 * ... and there is then a requirement to pull infos until the record is ready in the system.
 * <p>
 * Same for retention, deletion, etc. => need to check it's ready, then tell the system Nuxeo did its counter part (like
 * lock it, delete it, ...)
 * <p>
 * So, these tests are more for test while developing, click the button and wait and follow in debugger, etc.
 * <p>
 * Also, IMPORTANT - IMPORTANT: The test check for the calssRecordClassName set in the test configuration (see
 * SeimpleFeatureCustom)
 * <p>
 * Also super <b>IMPORTANT - IMPORTANT - IMPORTANT - IMPORTANT</b><br />
 * Most, if not all the unit tests are @Ignore, because we don't want to test the remote server built just the time of a
 * demo, we don't want to create a lot of records there while writing this proklugin, just when needed.
 *
 * @since 10.1
 */

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, SimpleFeatureCustom.class })
@Deploy("nuxeo.recordlion.nuxeo-recordlion-core")
public class TestRecordLionService {

    public static final long TIMEOUT_SECONDS = 180;

    @Inject
    protected RecordLionService recordlionservice;

    @Inject
    protected CoreSession session;

    @Before
    public void setup() throws Exception {

        if (SimpleFeatureCustom.hasLocalTestConfiguration()) {
            // Sanity check
            String value = SimpleFeatureCustom.getLocalProperty(Constants.CONF_KEY_BASE_URL);
            assertTrue("Missing " + Constants.CONF_KEY_BASE_URL, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(Constants.CONF_KEY_LOGIN);
            assertTrue("Missing " + Constants.CONF_KEY_LOGIN, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(Constants.CONF_KEY_PASSWORD);
            assertTrue("Missing " + Constants.CONF_KEY_PASSWORD, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_NAME);
            assertTrue("Missing " + SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_NAME, StringUtils.isNotBlank(value));

            value = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_ID);
            assertTrue("Missing " + SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_ID, StringUtils.isNotBlank(value));
        }

    }

    @Test
    public void testServiceIsDeployed() {
        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());
        assertNotNull(recordlionservice);
    }

    @Test
    public void shouldBeConfigured() {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        RecordLionDescriptor desc = recordlionservice.getDescriptor();
        assertNotNull(desc);

        assertTrue(StringUtils.isNotBlank(desc.getBaseUrl()));
        assertTrue(StringUtils.isNotBlank(desc.getLogin()));
        assertTrue(StringUtils.isNotBlank(desc.getPassword()));

    }

    @Ignore
    @Test
    public void testConnectionWithSimpleGET() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        JsonNode mainNode = recordlionservice.callGET(Constants.GET_RECORDCLASSES_ALL);

        assertNotNull(mainNode);

    }

    @Ignore
    @Test
    public void testHasExpectedRecordClass() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        String classNameToTest = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_NAME);
        // JsonNode mainNode = recordlionservice.callGET("recordclasses?all=true&page=0&pageSize=10");
        String endPoint = String.format(Constants.GET_RECORDCLASSES_CONTAINING_TITLEORCODE, classNameToTest);
        JsonNode mainNode = recordlionservice.callGET(endPoint);
        assertNotNull(mainNode);

        JsonNode items = mainNode.get("Items");
        assertEquals(items.size(), 1);

    }

    @Ignore
    @Test
    public void testCreateRecord() throws Exception {

        DocumentModel doc = session.getDocument(new PathRef("/default-domain"));
        assertNotNull(doc);

        // WE MUST MANUALLY CLASSIFY in this example, we have the class Id
        String recordClassIdStr = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_ID);
        long recordClassId = Long.parseLong(recordClassIdStr);
        JsonNode result = recordlionservice.recordizeDocument(doc, recordClassId, true, null);
        assertNotNull(result);
        String z = "";
        if (z != null) {

        }

    }

    @Ignore
    @Test
    public void testPullPendingActions() throws Exception {

        // String forceUri = "https://gartner2018.nuxeo.com/ui/#!/doc/9b0fefdb-54f0-4dd3-abeb-7c9d9c59401c-test54154";
        String forceUri = "DOMAIN-54154";
        List<Constants.LifecyclePhaseAction> actions = recordlionservice.pullActions(null, forceUri);
        assertNotNull(actions);

    }

    @Ignore
    @Test
    public void testCreateRecordAllCycle() throws Exception {

        // DocumentModel doc = session.getDocument(new PathRef("/default-domain"));
        // assertNotNull(doc);

        String title = "Test-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(1, 6);
        DocumentModel doc = session.createDocumentModel("/", title, "File");
        doc.setPropertyValue("dc:title", title);
        doc = session.createDocument(doc);
        session.save();

        // WE MUST MANUALLY CLASSIFY in this example, we have the class Id
        String recordClassIdStr = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_ID);
        long recordClassId = Long.parseLong(recordClassIdStr);
        JsonNode result = recordlionservice.createRecord(doc, recordClassId, true, TIMEOUT_SECONDS);
        assertNotNull(result);
        String z = "";
        if (z != null) {

        }

    }

}
