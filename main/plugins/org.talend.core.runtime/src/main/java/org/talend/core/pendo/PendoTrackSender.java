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
package org.talend.core.pendo;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.service.IRemoteService;
import org.talend.core.service.IStudioLiteP2Service;
import org.talend.core.ui.IInstalledPatchService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.utils.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoTrackSender {
    
    private static final String PREFIX_API = "api";

    private static final String PENDO_INFO = "/monitoring/pendo/info";

    private static final String PENDO_TRACK = "/monitoring/pendo/track";

    private static final String HEAD_AUTHORIZATION = "Authorization";

    private static final String HEAD_CONTENT_TYPE = "Content-Type";

    private static final String HEAD_PENDO_KEY = "x-pendo-integration-key";

    private static final String FEATURE_PREFIX = "org.talend.lite.";

    private static final String FEATURE_TAIL = ".feature.feature.group";

    private static PendoTrackSender instance;

    private static String adminUrl;

    private static String apiBaseUrl;

    public static PendoTrackSender getInstance() {
        if (instance == null) {
            instance = new PendoTrackSender();
        }
        if (StringUtils.isBlank(adminUrl)) {
            adminUrl = getRepositoryContext().getFields().get(RepositoryConstants.REPOSITORY_URL);
        }
        return instance;
    }

    public void sendToPendo() {
        Job job = new Job("send pendo track") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    sendTrackData();
                } catch (Exception e) {
                    // warning only
                    ExceptionHandler.process(e, Level.WARN);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public void sendTrackData() throws Exception {
        if (!checkTokenUsed(adminUrl) || !NetworkUtil.isNetworkValid()) {
            return;
        }

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            String pendoInfo = getPendoInfo();
            if (StringUtils.isBlank(pendoInfo)) {
                throw new Exception("Pendo information is empty");
            }
            String pendoKey = getPendoKeyFromLicense();
            if (StringUtils.isBlank(pendoKey)) {
                throw new Exception("Pendo key is empty");
            }
            
            client = HttpClients.createDefault();
            String url = getBaseUrl() + PENDO_TRACK;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HEAD_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setHeader(HEAD_PENDO_KEY, pendoKey);
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setText(generateTrackData(pendoInfo)).setContentType(ContentType.APPLICATION_JSON);
            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            response = client.execute(httpPost, HttpClientContext.create());
            StatusLine statusLine = response.getStatusLine();
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (HttpURLConnection.HTTP_OK != statusLine.getStatusCode()) {
                throw new Exception(statusLine.toString() + ", server message: [" + responseStr + "]");
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    // TODO maybe we will have more event handler later
    private String generateTrackData(String pendoInfo) throws Exception {
        JSONObject infoJson = new JSONObject(pendoInfo);
        String visitorId = ((JSONObject) infoJson.get("visitor")).getString("id");
        String accountId = ((JSONObject) infoJson.get("account")).getString("id");

        String studioPatch = null;
        Date date = new Date();
        IInstalledPatchService installedPatchService = IInstalledPatchService.get();
        if (installedPatchService != null) {
            studioPatch = installedPatchService.getLatestInstalledPatchVersion();
        }
        List<String> enabledFeatures = new ArrayList<String>();
        IStudioLiteP2Service studioLiteP2Service = IStudioLiteP2Service.get();
        if (studioLiteP2Service != null) {
            List<String> enabledFeaturesList = studioLiteP2Service.getCurrentProjectEnabledFeatures();
            enabledFeaturesList.stream().forEach(feature -> {
                String result = feature;
                if (result.startsWith(FEATURE_PREFIX)) {
                    result = result.substring(FEATURE_PREFIX.toCharArray().length);
                }
                if (result.endsWith(FEATURE_TAIL)) {
                    result = result.substring(0, result.lastIndexOf(FEATURE_TAIL));
                }
                enabledFeatures.add(result);
            });
        }
        PendoLoginProperties loginEvent = new PendoLoginProperties();
        loginEvent.setStudioVersion(VersionUtils.getInternalMajorVersion());
        loginEvent.setStudioPatch(studioPatch);
        loginEvent.setEnabledFeatures(enabledFeatures);

        PendoEventEntity entity = new PendoEventEntity();
        entity.setType("track");
        entity.setEvent("Project Login");
        entity.setVisitorId(visitorId);
        entity.setAccountId(accountId);
        entity.setTimestamp(date.getTime());
        entity.setProperties(loginEvent);

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(entity);
        return content;
    }

    private String getPendoInfo() throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = HttpClients.createDefault();
            String url = getBaseUrl() + PENDO_INFO;

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HEAD_AUTHORIZATION, "Bearer " + getToken());
            response = client.execute(httpGet, HttpClientContext.create());
            StatusLine statusLine = response.getStatusLine();
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (HttpURLConnection.HTTP_OK != statusLine.getStatusCode()) {
                throw new Exception(statusLine.toString() + ", server message: [" + responseStr + "]");
            }
            return responseStr;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private boolean checkTokenUsed(String adminUrl) throws Exception {
        if (StringUtils.isNotBlank(adminUrl) && GlobalServiceRegister.getDefault().isServiceRegistered(IRemoteService.class)) {
            IRemoteService service = GlobalServiceRegister.getDefault().getService(IRemoteService.class);
            return service.isTokenUsed(adminUrl);
        }
        return false;
    }

    public String getBaseUrl() throws Exception {
        if (StringUtils.isNotBlank(apiBaseUrl)) {
            return apiBaseUrl;
        }

        try {
            URL url = new URL(adminUrl);
            // tmc.int.cloud.talend.com
            String authority = url.getAuthority();
            String regex = "(\\w*\\-*\\w*\\.?){2}\\.(talend.com)";
            Pattern  pattern= Pattern.compile(regex);
            Matcher match = pattern.matcher(authority);
            if (match.find()) {
                // int.cloud.talend.com
                authority = match.group(0);
                URL apiURL = new URL(url.getProtocol(), PREFIX_API + "." + authority, "");
                // https://api.int.cloud.talend.com
                apiBaseUrl = apiURL.toString();
            } else {
                throw new Exception("Can't match pendo url from " + adminUrl);
            }
        } catch (MalformedURLException e) {
            throw new Exception("Invalid url " + adminUrl, e.getCause());
        }

        return apiBaseUrl;
    }

    private String getToken() {
        return getRepositoryContext().getClearPassword();
    }

    private String getPendoKeyFromLicense() throws Exception {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRemoteService.class)) {
            IRemoteService service = GlobalServiceRegister.getDefault().getService(IRemoteService.class);
            return service.getPendoKeyFromLicense();
        }
        return null;
    }

    private static RepositoryContext getRepositoryContext() {
        RepositoryContext repositoryContext = (RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY);
        return repositoryContext;
    }

    public void setAdminUrl(String adminUrl) {
        PendoTrackSender.adminUrl = adminUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        PendoTrackSender.apiBaseUrl = apiBaseUrl;
    }

}
