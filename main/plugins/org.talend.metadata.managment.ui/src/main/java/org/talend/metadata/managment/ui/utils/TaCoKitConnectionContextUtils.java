// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.metadata.managment.ui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.ExtendedNodeConnectionContextUtils.EHadoopParamName;

/**
 * created by ldong on Dec 18, 2014 Detailled comment
 *
 */
public class TaCoKitConnectionContextUtils {

	public enum ETaCoKitParamName implements IConnParamName {

		// netsit
//    	ApiVersion,
//    	LoginType,
		Account, Email, Password, RoleId, ApplicationId, TokenId, ConsumerKey, ConsumerSecret, TokenSecret,

		// workday
		ClientIdentifier, ClientSecret, TenantAlias, AuthEndpoint, Endpoint,

		// neo4j
		ConnectionUri,
//        Password,
		Username,

		// azure
		EndpointSuffix, SharedKey, AccountName, Sas, TenantId,
//        ClientSecret,
		ClientId,

		// rabbitMQ
		Port, Hostname,
//        Password,
		UserName,

		// zendesk
		AuthenticationLogin, ServerUrl, ApiToken,

		// samba
		Host, Domain,
//		Password,Username

		// cyberark
		CredProviderServicePort,

		// tableau
		Site, BasicAuthUsername, BasicAuthPassword, AccessTokenAuthTokenSecret, AccessTokenAuthTokenName,
//		ServerUrl,

		// analytics
		JsonCredentials,

		// kudu
		MasterAddresses,

	}

	static List<IContextParameter> getContextVariables(final String prefixName, Connection conn,
			Set<IConnParamName> paramSet) {
		List<IContextParameter> varList = new ArrayList<IContextParameter>();
		for (IRepositoryContextHandler handler : RepositoryContextManager.getHandlers()) {
			if (handler.isRepositoryConType(conn)) {
				varList = handler.createContextParameters(prefixName, conn, paramSet);
			}
		}
		return varList;
	}

	static void setConnectionPropertiesForContextMode(String prefixName, Connection conn,
			Set<IConnParamName> paramSet) {
		if (conn == null || prefixName == null) {
			return;
		}
		for (IRepositoryContextHandler handler : RepositoryContextManager.getHandlers()) {
			if (handler.isRepositoryConType(conn)) {
				handler.setPropertiesForContextMode(prefixName, conn, paramSet);
			}
		}
	}

	static void setConnectionPropertiesForExistContextMode(Connection conn, Set<IConnParamName> paramSet,
			Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
		if (conn == null) {
			return;
		}
		for (IRepositoryContextHandler handler : RepositoryContextManager.getHandlers()) {
			if (handler.isRepositoryConType(conn)) {
				handler.setPropertiesForExistContextMode(conn, paramSet, adaptMap);
			}
		}
	}

	static void revertPropertiesForContextMode(Connection conn, ContextType contextType) {
		if (conn == null) {
			return;
		}
		for (IRepositoryContextHandler handler : RepositoryContextManager.getHandlers()) {
			if (handler.isRepositoryConType(conn)) {
				handler.revertPropertiesForContextMode(conn, contextType);
			}
		}
	}

	static Set<String> getAdditionalPropertiesVariablesForExistContext(Connection conn) {
		Set<String> varList = new HashSet<String>();
		if (conn == null) {
			return Collections.emptySet();
		}
		for (IRepositoryContextHandler handler : RepositoryContextManager.getHandlers()) {
			if (handler.isRepositoryConType(conn)) {
				varList = handler.getConAdditionPropertiesForContextMode(conn);
			}
		}
		return varList;
	}

	public static String getReplicaParamName(EHadoopParamName param, int number) {
		if (param == EHadoopParamName.ReplicaHost || param == EHadoopParamName.ReplicaPort) {
			return param.name() + ConnectionContextHelper.LINE + number;
		}
		return null;
	}

}
