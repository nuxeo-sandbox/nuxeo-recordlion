package nuxeo.recordlion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nuxeo.recordlion.operations.CreateRecord;
import nuxeo.recordlion.service.RecordLionService;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, SimpleFeatureCustom.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.recordlion.nuxeo-recordlion-core")
public class TestCreateRecordOperation {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected RecordLionService recordlionservice;

    @Ignore
    @Test
    public void shouldCreateARecord() throws Exception {

        Assume.assumeTrue("No custom configuration file => no test", SimpleFeatureCustom.hasLocalTestConfiguration());

        String title = "Test-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(1, 6);
        DocumentModel doc = session.createDocumentModel("/", title, "File");
        doc.setPropertyValue("dc:title", title);
        doc = session.createDocument(doc);
        session.save();

        String recordClassIdStr = SimpleFeatureCustom.getLocalProperty(SimpleFeatureCustom.TEST_KEY_RECORD_CLASS_ID);
        long recordClassId = Long.parseLong(recordClassIdStr);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap<>();
        params.put("resultVarName", "resultStr");
        params.put("isManuallyClassified", true);
        params.put("recordClassId", recordClassId);
        @SuppressWarnings("unused")
        DocumentModel ignore = (DocumentModel) automationService.run(ctx, CreateRecord.ID, params);

        String resultStr = (String) ctx.get("resultStr");
        assertTrue(StringUtils.isNotBlank(resultStr));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree(resultStr);

        String value = resultJson.get("result").asText();
        assertEquals("OK", value);

        value = resultJson.get("title").asText();
        assertEquals(title, value);

        value = resultJson.get("uri").asText();
        assertTrue(StringUtils.isNotBlank(value));

        value = resultJson.get("recordIdentifier").asText();
        assertTrue(StringUtils.isNotBlank(value));

    }

}
