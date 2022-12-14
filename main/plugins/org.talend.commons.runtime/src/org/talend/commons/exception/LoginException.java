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
package org.talend.commons.exception;

/**
 * DOC matthieu class global comment. Detailled comment
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven, 29 sep 2006) nrousseau $
 */
public class LoginException extends BusinessException {

    public static final String RESTART = "restart";
    
    private int errCode;
    

    public LoginException(String key, Throwable cause) {
        super(key, cause);
    }

    public LoginException(String message) {
        super(message);
    }
    
    public LoginException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }
    
    public int getErrCode() {
        return this.errCode;
    }
}
