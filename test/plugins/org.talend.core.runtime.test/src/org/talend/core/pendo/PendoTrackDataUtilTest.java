// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.pendo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.pendo.PendoTrackDataUtil.TrackEvent;
import org.talend.core.pendo.properties.PendoImportAPIproperties;
import org.talend.core.pendo.properties.PendoUseAPIProperties;
import org.talend.repository.ProjectManager;
import org.talend.utils.json.JSONObject;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoTrackDataUtilTest {

    @Test
    public void testGenerateTrackData() throws Exception {
        String pendoInfo = "{\"visitor\":{\"id\":\"test.talend.com@rd.aws.ap.talend.com\"},\"account\":{\"id\":\"rd.aws.ap.talend.com\"}}";
        // open in API Designer event
        String trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.OPEN_IN_APIDesigner, null);
        String timeString = getTimestampStringFromJson(trackData);
        String expect = "{\"type\":\"track\",\"event\":\"Open in API Designer\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":null}";
        assertEquals(expect, trackData);

        // Open in API Tester
        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.OPEN_IN_APITester, null);
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Open in API Tester\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":null}";
        assertEquals(expect, trackData);

        // Open API Documentation
        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.OPEN_API_DOCUMENTATION, null);
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Open API Documentation\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":null}";
        assertEquals(expect, trackData);

        // Use API Definition event
        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.USE_API_DEF,
                new PendoUseAPIProperties("tRESTRequest"));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Use API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"component\":\"tRESTRequest\"}}";
        assertEquals(expect, trackData);

        // Import API Definition
        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.IMPORT_API_DEF,
                new PendoImportAPIproperties(ESourceType.LOCAL_FILE.getSourceType()));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Import API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"source\":\"file\"}}";
        assertEquals(expect, trackData);

        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.IMPORT_API_DEF,
                new PendoImportAPIproperties(ESourceType.API_DESIGNER.getSourceType()));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Import API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"source\":\"API Designer\"}}";
        assertEquals(expect, trackData);

        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.IMPORT_API_DEF,
                new PendoImportAPIproperties(ESourceType.REMOTE_URL.getSourceType()));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Import API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"source\":\"Remote URL\"}}";
        assertEquals(expect, trackData);

        // Update API Definition
        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.UPDATE_API_DEF,
                new PendoImportAPIproperties(ESourceType.LOCAL_FILE.getSourceType()));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Update API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"source\":\"file\"}}";
        assertEquals(expect, trackData);

        trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, TrackEvent.UPDATE_API_DEF,
                new PendoImportAPIproperties(ESourceType.API_DESIGNER.getSourceType()));
        timeString = getTimestampStringFromJson(trackData);
        expect = "{\"type\":\"track\",\"event\":\"Update API Definition\",\"visitorId\":\"test.talend.com@rd.aws.ap.talend.com\",\"accountId\":\"rd.aws.ap.talend.com\",\"timestamp\":"
                + timeString + ",\"properties\":{\"source\":\"API Designer\"}}";
        assertEquals(expect, trackData);
    }

    private String getTimestampStringFromJson(String trackData) throws Exception {
        JSONObject trackDataJson = new JSONObject(trackData);
        long time = trackDataJson.getLong("timestamp");
        return String.valueOf(time);
    }

    // org.talend.repository.model.ESourceType
    enum ESourceType {

        LOCAL_FILE("LOCAL_FILE"), //$NON-NLS-1$
        API_DESIGNER("API_DESIGNER"), //$NON-NLS-1$
        REMOTE_URL("REMOTE_URL"); //$NON-NLS-1$

        private String sourceType;

        private ESourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceType() {
            return this.sourceType;
        }

    }

    @Test
    public void testFindReferencePorjectPath() {
        Project mainEmfProj = createTestProject("testMain");
        org.talend.core.model.general.Project mainProj = Mockito.mock(org.talend.core.model.general.Project.class);
        mainProj.setEmfProject(mainEmfProj);

        // Main -> Ref1 -> Ref2 -> Ref3
        Project refEmfProj1 = createTestProject("ref_project1");
        Project refEmfProj2 = createTestProject("ref_project2");
        Project refEmfProj3 = createTestProject("ref_project3");
        Map<String, org.talend.core.model.general.Project> teclabelProjectMap = new HashMap<String, org.talend.core.model.general.Project>();
        org.talend.core.model.general.Project refProj1 = Mockito.mock(org.talend.core.model.general.Project.class);
        mainProj.setEmfProject(refEmfProj1);
        org.talend.core.model.general.Project refProj2 = Mockito.mock(org.talend.core.model.general.Project.class);
        mainProj.setEmfProject(refEmfProj2);
        org.talend.core.model.general.Project refProj3 = Mockito.mock(org.talend.core.model.general.Project.class);
        mainProj.setEmfProject(refEmfProj3);
        teclabelProjectMap.put(refEmfProj1.getTechnicalLabel(), refProj1);
        teclabelProjectMap.put(refEmfProj2.getTechnicalLabel(), refProj2);
        teclabelProjectMap.put(refEmfProj3.getTechnicalLabel(), refProj3);

        ProjectReference projRef1 = createTestProjectReference(refEmfProj1);
        ProjectReference projRef2 = createTestProjectReference(refEmfProj2);
        ProjectReference projRef3 = createTestProjectReference(refEmfProj3);
        List<ProjectReference> refList4Main = new ArrayList<ProjectReference>();
        refList4Main.add(projRef1);
        List<ProjectReference> refList4Ref1 = new ArrayList<ProjectReference>();
        refList4Ref1.add(projRef2);
        List<ProjectReference> refList4Ref2 = new ArrayList<ProjectReference>();
        refList4Ref2.add(projRef3);
        Mockito.when(mainProj.getProjectReferenceList()).thenReturn(refList4Main);
        Mockito.when(refProj1.getProjectReferenceList()).thenReturn(refList4Ref1);
        Mockito.when(refProj2.getProjectReferenceList()).thenReturn(refList4Ref2);
        
        int[] refCount = new int[] {0};
        List<String> resultList = new ArrayList<String>();
        Map<String, String> desensitiveLabelMap = new HashMap<String, String>();
        PendoTrackDataUtil.findReferencePorjectPath(mainProj, "Main", resultList, refCount, desensitiveLabelMap,
                teclabelProjectMap);
        String[] expect = new String[] { "Main/Ref1", "Main/Ref1/Ref2", "Main/Ref1/Ref2/Ref3" };
        assertTrue(isResultMatch(expect, resultList));

        // Main -> Ref1 -> Ref2 -> Ref3
        // Main -> Ref4 -> Ref3
        Project refEmfProj4 = createTestProject("ref_project4");
        org.talend.core.model.general.Project refProj4 = Mockito.mock(org.talend.core.model.general.Project.class);
        mainProj.setEmfProject(refEmfProj4);
        teclabelProjectMap.put(refEmfProj4.getTechnicalLabel(), refProj4);
        ProjectReference projRef4 = createTestProjectReference(refEmfProj4);
        List<ProjectReference> refList4Ref4 = new ArrayList<ProjectReference>();
        refList4Ref4.add(projRef3);
        Mockito.when(refProj4.getProjectReferenceList()).thenReturn(refList4Ref4);
        refList4Main.add(projRef4);
        Mockito.when(mainProj.getProjectReferenceList()).thenReturn(refList4Main);

        int[] refCount1 = new int[] { 0 };
        List<String> resultList1 = new ArrayList<String>();
        Map<String, String> desensitiveLabelMap1 = new HashMap<String, String>();
        PendoTrackDataUtil.findReferencePorjectPath(mainProj, "Main", resultList1, refCount1, desensitiveLabelMap1,
                teclabelProjectMap);
        String[] expect1 = new String[] { "Main/Ref1", "Main/Ref1/Ref2", "Main/Ref1/Ref2/Ref3", "Main/Ref4", "Main/Ref4/Ref3" };
        assertTrue(isResultMatch(expect1, resultList1));
    }

    private boolean isResultMatch(String[] expect, List<String> resultList) {
        boolean match = expect.length == resultList.size();
        for (int i = 0; i < expect.length; i++) {
            String exp = expect[i];
            if (!resultList.contains(exp)) {
                match = false;
            }
        }
        return match;
    }

    private Project createTestProject(String label) {
        Project project = PropertiesFactory.eINSTANCE.createProject();
        project.setLabel(label);
        project.setTechnicalLabel(ProjectManager.getLocalTechnicalProjectName(project.getLabel()));
        return project;
    }

    private ProjectReference createTestProjectReference(Project project) {
        ProjectReference projReference = PropertiesFactory.eINSTANCE.createProjectReference();
        projReference.setReferencedBranch("master");
        projReference.setReferencedProject(project);
        return projReference;
    }

}
