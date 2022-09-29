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
package org.talend.core.model.general;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.pendo.PendoTrackSender;
import org.talend.core.runtime.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;
import org.talend.signon.util.TMCRepositoryUtil;
import org.talend.signon.util.TokenMode;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ConnectionBean implements Cloneable {

    private static final String DYN_FIELDS_SEPARATOR = "="; //$NON-NLS-1$

    private static final String FIELDS_SEPARATOR = "#"; //$NON-NLS-1$

    private static final String ID = "id"; //$NON-NLS-1$

    private static final String DESCRIPTION = "description"; //$NON-NLS-1$

    private static final String NAME = "name"; //$NON-NLS-1$

    private static final String PASSWORD = "password"; //$NON-NLS-1$

    private static final String USER = "user"; //$NON-NLS-1$

    private static final String WORKSPACE = "workSpace"; //$NON-NLS-1$

    private static final String DYNAMICFIELDS = "dynamicFields"; //$NON-NLS-1$

    private static final String COMPLETE = "complete"; //$NON-NLS-1$

    private JSONObject conDetails = new JSONObject();

    private Map<String, String> dynamicFields = new HashMap<String, String>();

    private static final String TOKEN = "token"; //$NON-NLS-1$

    private static final String STORECREDENTIALS = "storeCredentials"; //$NON-NLS-1$

    private String credentials = ""; //$NON-NLS-1$

    public static final String CLOUD_TOKEN_ID ="cloud_token"; //$NON-NLS-1$
    
    private static final String LOGIN_VIA_CLOUD = "login_via_cloud"; //$NON-NLS-1$
    
    /**
     * DOC smallet ConnectionBean constructor comment.
     */
    public ConnectionBean() {
        super();
    }

    public static ConnectionBean getDefaultConnectionBean() {
        ConnectionBean newConnection = new ConnectionBean();
        newConnection.setName(Messages.getString("ConnectionBean.Local")); //$NON-NLS-1$
        newConnection.setDescription(Messages.getString("ConnectionBean.DefaultConnection")); //$NON-NLS-1$
        newConnection.setRepositoryId(RepositoryConstants.REPOSITORY_LOCAL_ID);
        newConnection.setPassword(""); //$NON-NLS-1$
        // newConnection.setUser("your@userName.here"); //$NON-NLS-1$
        return newConnection;
    }

    public static ConnectionBean getDefaultRemoteConnectionBean() {
        ConnectionBean newConnection = new ConnectionBean();
        newConnection.setName(Messages.getString("ConnectionBean.Remote")); //$NON-NLS-1$
        newConnection.setDescription(Messages.getString("ConnectionBean.DefaultConnection")); //$NON-NLS-1$
        newConnection.setRepositoryId(RepositoryConstants.REPOSITORY_REMOTE_ID);
        newConnection.setPassword(""); //$NON-NLS-1$
        return newConnection;
    }
    
    public static ConnectionBean getDefaultCloudConnectionBean(String dataCenter) {
        ConnectionBean newConnection = new ConnectionBean();
        newConnection.setName(Messages.getString("ConnectionBean.Cloud.name", TMCRepositoryUtil.getDisplayNameByDatacenter(dataCenter))); //$NON-NLS-1$
        newConnection.setDescription(Messages.getString("ConnectionBean.CloudConnection.description", TMCRepositoryUtil.getDisplayNameByDatacenter(dataCenter))); //$NON-NLS-1$
        newConnection.setRepositoryId(TMCRepositoryUtil.getRepositoryId(dataCenter));
        newConnection.setToken(true);
        newConnection.setStoreCredentials(true);
        newConnection.setComplete(true);
        newConnection.setLoginViaCloud(true);
        newConnection.setWorkSpace(getRecentWorkSpace());
        return newConnection;
    }
    
    protected static String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        return filePath;
    }

    /**
     * Getter for ID.
     *
     * @return the ID
     */
    public String getRepositoryId() {
        try {
            if (conDetails.has(ID)) {
                return conDetails.getString(ID);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the ID.
     *
     * @param id the id to set
     */
    public void setRepositoryId(String repositoryId) {
        try {
            conDetails.put(ID, repositoryId);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Getter for description.
     *
     * @return the description
     */
    public String getDescription() {
        try {
            if (conDetails.has(DESCRIPTION)) {
                return conDetails.getString(DESCRIPTION);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        try {
            conDetails.put(DESCRIPTION, description);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        try {
            if (conDetails.has(NAME)) {
                return conDetails.getString(NAME);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        try {
            conDetails.put(NAME, name);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Getter for password.
     *
     * @return the password
     */
    public String getPassword() {
        try {
            
            if (conDetails.has(PASSWORD)) {
                if (isStoreCredentials() && credentials != null) {
                    return this.credentials;
                }
                return conDetails.getString(PASSWORD);
            }  else if (conDetails.has(CLOUD_TOKEN_ID)){ 
                String object = conDetails.getString(CLOUD_TOKEN_ID);
                TokenMode token = TokenMode.parseFromJson(object, null);
                return token.getAccessToken();
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        try {
            conDetails.put(PASSWORD, password);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Getter for user.
     *
     * @return the user
     */
    public String getUser() {
        try {       
            if (conDetails.has(USER)) {
                String user = conDetails.getString(USER);
                if (isToken()) {
                    String url = getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
                    user = PendoTrackSender.getInstance().getTmcUser(url, getPassword());
                    if (StringUtils.isNotBlank(user)) {
                        setUser(user);
                    }
                }
                return user;
            } 
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the user.
     *
     * @param user the user to set
     */
    public void setUser(String user) {
        try {
            conDetails.put(USER, user);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Getter for workSpace.
     *
     * @return the workSpace
     */
    public String getWorkSpace() {
        try {
            if (conDetails.has(WORKSPACE)) {
                return conDetails.getString(WORKSPACE);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * Sets the workSpace.
     *
     * @param workSpace the workSpace to set
     */
    public void setWorkSpace(String workSpace) {
        try {
            conDetails.put(WORKSPACE, workSpace);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    public Map<String, String> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(Map<String, String> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public boolean isComplete() {
        try {
            if (conDetails.has(COMPLETE)) {
                return (Boolean) conDetails.get(COMPLETE);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public void setComplete(boolean complete) {
        try {
            conDetails.put(COMPLETE, complete);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isToken() {
        try {
            if (conDetails.has(TOKEN)) {
                return (Boolean) conDetails.get(TOKEN);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public void setToken(boolean token) {
        try {
            conDetails.put(TOKEN, token);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }
    
    public boolean isLoginViaCloud() {
        try {
            if (conDetails.has(LOGIN_VIA_CLOUD)) {
                return (Boolean) conDetails.get(LOGIN_VIA_CLOUD);
            }
        } catch (JSONException e) {
            // do nothing
        }
        return false;
    }
    
    public void setLoginViaCloud(boolean isLoginViaCloud) {
        try {
            conDetails.put(LOGIN_VIA_CLOUD, isLoginViaCloud);
        } catch (JSONException e) {
            // do nothing
        }
    }

    @Override
    public ConnectionBean clone() throws CloneNotSupportedException {
        return writeFromJSON(this.getConDetails());
    }

    @Override
    public String toString() {
        return this.getConDetails().toString();
    }

    public static ConnectionBean writeFromString(String s) {
        ConnectionBean toReturn = new ConnectionBean();
        try {
            String[] st = s.split(FIELDS_SEPARATOR, -1);
            int i = 0;
            toReturn.setRepositoryId(st[i++]);
            toReturn.setName(st[i++]);
            toReturn.setDescription(st[i++]);
            toReturn.setUser(st[i++]);
            toReturn.setPassword(st[i++]);
            toReturn.setWorkSpace(st[i++]);
            toReturn.setComplete(new Boolean(st[i++]));
            toReturn.setToken(new Boolean(st[i++]));
            toReturn.setStoreCredentials(new Boolean(st[i++]));
            JSONObject dynamicJson = new JSONObject();
            toReturn.getConDetails().put(DYNAMICFIELDS, dynamicJson);
            while (i < st.length) {
                String[] st2 = st[i++].split(DYN_FIELDS_SEPARATOR, -1);
                dynamicJson.put(st2[0], st2[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ExceptionHandler.process(e);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return toReturn;
    }

    public static ConnectionBean writeFromJSON(JSONObject json) {
        ConnectionBean toReturn = new ConnectionBean();
        try {
            toReturn.setConDetails(new JSONObject(json.toString()));
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return toReturn;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConnectionBean)) {
            return false;
        }
        ConnectionBean other = (ConnectionBean) obj;

        return this.getConDetails().toString().equals(other.getConDetails().toString());
    }

    public JSONObject getConDetails() {
        JSONObject dynamicJson = new JSONObject();
        try {
            for (String key : dynamicFields.keySet()) {
                dynamicJson.put(key, dynamicFields.get(key));
            }
            conDetails.put(DYNAMICFIELDS, dynamicJson);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }

        return conDetails;
    }

    public void setConDetails(JSONObject conDetails) {
        this.conDetails = conDetails;
        try {
            if (conDetails.has(DYNAMICFIELDS)) {
                Object object = conDetails.get(DYNAMICFIELDS);
                if (object instanceof JSONObject) {
                    JSONObject dynamicJson = (JSONObject) object;
                    Iterator sortedKeys = dynamicJson.sortedKeys();
                    while (sortedKeys.hasNext()) {
                        String key = (String) sortedKeys.next();
                        String value = dynamicJson.getString(key);
                        dynamicFields.put(key, value);
                    }
                }
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    public String getUrl() {
        try {
            if (dynamicFields.containsKey(RepositoryConstants.REPOSITORY_URL)) {
                return dynamicFields.get(RepositoryConstants.REPOSITORY_URL);
            }
            if (conDetails.has(RepositoryConstants.REPOSITORY_URL)) {
                return conDetails.getString(RepositoryConstants.REPOSITORY_URL);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }
    
    public void setUrl(String url) {
        dynamicFields.put(RepositoryConstants.REPOSITORY_URL, url);
    }

    public boolean isStoreCredentials() {
        try {
            if (conDetails.has(STORECREDENTIALS)) {
                return (Boolean) conDetails.get(STORECREDENTIALS);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public void setStoreCredentials(boolean store) {
        try {
            conDetails.put(STORECREDENTIALS, store);
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    public String getCredentials() {
        return this.credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    
    public TokenMode getConnectionToken() {
        try {
            if (conDetails.has(CLOUD_TOKEN_ID)) {
                String object = conDetails.getString(CLOUD_TOKEN_ID);
                return TokenMode.parseFromJson(object, null);
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    
    public void setConnectionToken(TokenMode connectionToken) {
        try {
            conDetails.put(CLOUD_TOKEN_ID, TokenMode.writeToJson(connectionToken));
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
    }
    
}
