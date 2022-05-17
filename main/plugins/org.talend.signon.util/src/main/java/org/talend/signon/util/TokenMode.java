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
package org.talend.signon.util;

import java.util.Date;

public class TokenMode{
    
    private String clientId;
    private String adminURL;
    private String tokenUser;
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private String idToken;
    private String scope;
    private String tokenType;
    private long lastRefreshTime = System.currentTimeMillis();
    
    public TokenMode() {
        
    }
    
    public TokenMode(String accessToken, String expiresIn, String idToken, String refreshToken, String scope, String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.idToken = idToken;
        this.refreshToken = idToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }
    
    public String getClientId() {
        return clientId;
    }

    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    
    public String getAdminURL() {
        return adminURL;
    }

    
    public void setAdminURL(String adminURL) {
        this.adminURL = adminURL;
    }

    
    public String getTokenUser() {
        return tokenUser;
    }

    
    public void setTokenUser(String tokenUser) {
        this.tokenUser = tokenUser;
    }

    
    public String getAccessToken() {
        return accessToken;
    }

    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    
    public String getRefreshToken() {
        return refreshToken;
    }

    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    
    public String getExpiresIn() {
        return expiresIn;
    }

    
    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    
    public String getIdToken() {
        return idToken;
    }

    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    
    public String getScope() {
        return scope;
    }

    
    public void setScope(String scope) {
        this.scope = scope;
    }

    
    public String getTokenType() {
        return tokenType;
    }

    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    
    public void setLastRefreshTime(long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }
    
    
}
