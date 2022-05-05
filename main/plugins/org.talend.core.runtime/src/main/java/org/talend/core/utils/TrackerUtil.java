package org.talend.core.utils;

import java.util.List;
import java.util.Map;

import org.talend.commons.utils.Version;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.services.IGitInfoService;
import org.talend.core.services.IGITProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryService;

//============================================================================
//
//Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
public class TrackerUtil {

	public static String getAWSTracker() {
		String strVersion = VersionUtils.getDisplayVersion();
		Version version = new Version(strVersion);
		IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
				.getService(IBrandingService.class);
		String productName = brandingService.getProductName();
		StringBuffer sb = new StringBuffer();
		sb.append("APN/1.0 Talend/").append(getStrVersion(version)).append(" Studio/").append(getStrVersion(version)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(" (").append(productName).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}

	private static String getStrVersion(Version version) {
		StringBuffer sb = new StringBuffer();
		sb.append(version.getMajor()).append(".").append(version.getMinor()); //$NON-NLS-1$
		return sb.toString();
	}

	public static String getGoogleTracker() {
		return "GPN:Talend"; //$NON-NLS-1$
	}

	public static String getVersion() {
		String strVersion = VersionUtils.getDisplayVersion();
		Version version = new Version(strVersion);
		return getStrVersion(version);
	}
	
	public static String getRedshiftTracker() {
		return "--Talend -v " + getVersion();
	}
	
	private static void addRepositoryInfo(IProcess process) throws Exception {
		GlobalServiceRegister serviceRegister = GlobalServiceRegister.getDefault();
		
        IRepositoryNode node = ProjectRepositoryNode.getInstance().getRootRepositoryNode(
                ERepositoryObjectType.GIT_ROOT);
        System.out.println("node:"+node);
        
        
        process.getPropertyValue(getAWSTracker());
		IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
        List<IRepositoryViewObject> list = factory.getAll(ERepositoryObjectType.GIT_ROOT, false, true);
        System.out.println("IRepositoryViewObject list:");
        list.forEach(System.out::println);
        
        
        
		System.out.println("serviceRegister:"+serviceRegister);
        IGITProviderService gitProviderService = null;
        if (serviceRegister.isServiceRegistered(IGITProviderService.class)) {
            gitProviderService = (IGITProviderService) GlobalServiceRegister.getDefault().getService(IGITProviderService.class);
            System.out.println("gitProviderService:"+gitProviderService);
        }
        IGitInfoService gitInfoService = serviceRegister.getService(IGitInfoService.class);
        Map<String, String> gitInfo = gitInfoService.getGitInfo(node.getObject().getProperty());
        
        
        
	}
}
