package nuxeo.recordlion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
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
 * Also, IMPORTANT - IMPORTANT: This test assumes there is a "Claim" RecordClass in the distant server. Make sure you
 * have this type of RecordClass
 *
 * @since TODO
 */

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, SimpleFeatureCustom.class })
@Deploy("nuxeo.recordlion.nuxeo-recordlion-core")
public class TestRecordLionService {

    public static String TEST_RECORDCLASS = "Claim";

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
        }

    }

    @Test
    public void testServiceIsDeployed() {
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

    @Test
    public void testConnectionWithSimpleGET() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        JsonNode mainNode = recordlionservice.callGET(Constants.GET_RECORDCLASSES_ALL);

        assertNotNull(mainNode);

    }

    @Test
    public void testHasExpectedRecordClass() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        //JsonNode mainNode = recordlionservice.callGET("recordclasses?all=true&page=0&pageSize=10");
        String endPoint = String.format(Constants.GET_RECORDCLASSES_CONTAINING_TITLEORCODE, TEST_RECORDCLASS);
        JsonNode mainNode = recordlionservice.callGET(endPoint);
        assertNotNull(mainNode);

        JsonNode items = mainNode.get("Items");
        assertEquals(items.size(), 1);

    }

    @Test
    public void testCreateRecord() throws Exception {

        DocumentModel doc = session.getDocument(new PathRef("/default-domain"));
        assertNotNull(doc);

        JsonNode result = recordlionservice.createRecordForDocument(doc, null);

        String z = "";
        if(zz != null) {

        }

    }

}
