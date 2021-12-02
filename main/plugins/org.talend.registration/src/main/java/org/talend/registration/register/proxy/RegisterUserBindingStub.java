/**
 * RegisterUserBindingStub.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.talend.registration.register.proxy;

import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

public class RegisterUserBindingStub extends org.apache.axis2.client.Stub implements
        org.talend.registration.register.proxy.RegisterUserPortType {

    private String cachedEndpoint;

    private String cachedUsername;

    private String cachedPassword;

    private boolean manageSession;

    private static final String BASE_URL = "https://product-routes--talend-www.netlify.app/TalendRegisterWS/registerws.php";

    private static final String WSDL_URI = "http://product-routes--talend-www.netlify.app/TalendRegisterWS/wsdl";

    public RegisterUserBindingStub(String endpointURL) throws org.apache.axis2.AxisFault {
        cachedEndpoint = endpointURL;
    }

    protected org.apache.axis2.rpc.client.RPCServiceClient createCall() throws java.rmi.RemoteException {
        try {
            Options options = new Options();
            RPCServiceClient client = new RPCServiceClient();
            client.setOptions(options);

            if (manageSession) {
                options.setManageSession(manageSession);
            }
            if (cachedUsername != null) {
                options.setUserName(cachedUsername);
            }
            if (cachedPassword != null) {
                options.setPassword(cachedPassword);
            }
            if (cachedEndpoint != null) {
                options.setTo(new EndpointReference(cachedEndpoint));
            }

            return client;
        } catch (java.lang.Throwable _t) {
            throw new org.apache.axis2.AxisFault("Failure trying to get the client object", _t);
        }
    }

    public boolean registerUser(java.lang.String email, java.lang.String country, java.lang.String designerversion)
            throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/RegisterUser");

        QName method = new QName(WSDL_URI, "RegisterUser");
        Object[] parameters = { email, country, designerversion };
        Class[] returnTypes = new Class[] { boolean.class };

        // Invoking the method
        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return ((Boolean) response[0]).booleanValue();
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }

        return false;
    }

    public boolean registerUserWithProductName(java.lang.String email, java.lang.String country,
            java.lang.String designerversion, java.lang.String productname) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/RegisterUserWithProductName");
        QName method = new QName(WSDL_URI, "RegisterUserWithProductName");
        Object[] parameters = { email, country, designerversion, productname };
        Class[] returnTypes = new Class[] { boolean.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return ((Boolean) response[0]).booleanValue();
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return false;
    }

    public boolean registerUserWithAllUserInformations(java.lang.String email, java.lang.String country,
            java.lang.String designerversion, java.lang.String productname, java.lang.String projectLanguage,
            java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion, java.lang.String totalMemory,
            java.lang.String memRAM, java.lang.String nbProc) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/RegisterUserWithAllUserInformations");
        QName method = new QName(WSDL_URI, "RegisterUserWithAllUserInformations");
        Object[] parameters = { email, country, designerversion, productname, projectLanguage, osName, osVersion, javaVersion,
                totalMemory, memRAM, nbProc };
        Class[] returnTypes = new Class[] { boolean.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return ((Boolean) response[0]).booleanValue();
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return false;
    }

    public java.math.BigInteger registerUserWithAllUserInformationsAndReturnId(java.lang.String email, java.lang.String country,
            java.lang.String designerversion, java.lang.String productname, java.lang.String projectLanguage,
            java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion, java.lang.String totalMemory,
            java.lang.String memRAM, java.lang.String nbProc) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(
                BASE_URL + "/RegisterUserWithAllUserInformationsAndReturnId");
        QName method = new QName(WSDL_URI, "RegisterUserWithAllUserInformationsAndReturnId");
        Object[] parameters = { email, country, designerversion, productname, projectLanguage, osName, osVersion, javaVersion,
                totalMemory, memRAM, nbProc };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.registeruser.proxy.RegisterUserPortType#
     * registerUserWithAllUserInformationsUniqueIdAndReturnId(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public BigInteger registerUserWithAllUserInformationsUniqueIdAndReturnId(String email, String country,
            String designerversion, String productname, String projectLanguage, String osName, String osVersion,
            String javaVersion, String totalMemory, String memRAM, String nbProc, String uniqueId)
            throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(
                BASE_URL + "/RegisterUserWithAllUserInformationsUniqueIdAndReturnId");
        QName method = new QName(WSDL_URI,
                "RegisterUserWithAllUserInformationsUniqueIdAndReturnId");
        Object[] parameters = { email, country, designerversion, productname, projectLanguage, osName, osVersion, javaVersion,
                totalMemory, memRAM, nbProc, uniqueId };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    public org.talend.registration.register.proxy.UserRegistration[] listUsers() throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/ListUsers");
        QName method = new QName(WSDL_URI, "ListUsers");
        Object[] parameters = {};
        Class[] returnTypes = new Class[] { UserRegistration.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (UserRegistration[]) response;
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new UserRegistration[0];
    }

    public java.lang.String checkUser(java.lang.String email) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(
                BASE_URL + "/CheckUser");
        QName method = new QName(WSDL_URI, "CheckUser");
        Object[] parameters = { email };
        Class[] returnTypes = new Class[] { String.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (String) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return "";
    }

    public java.math.BigInteger createUser(java.lang.String email, java.lang.String pseudo, java.lang.String password,
            java.lang.String firstname, java.lang.String lastname, java.lang.String country, java.lang.String designerversion,
            java.lang.String productname, java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion,
            java.lang.String totalMemory, java.lang.String memRAM, java.lang.String nbProc) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/CreateUser");
        QName method = new QName(WSDL_URI, "CreateUser");
        Object[] parameters = { email, pseudo, password, firstname, lastname, country, designerversion, productname, osName,
                osVersion, javaVersion, totalMemory, memRAM, nbProc };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    public java.math.BigInteger updateUser(java.lang.String email, java.lang.String pseudo, java.lang.String passwordOld,
            java.lang.String passwordNew, java.lang.String firstname, java.lang.String lastname, java.lang.String country,
            java.lang.String designerversion, java.lang.String productname, java.lang.String osName, java.lang.String osVersion,
            java.lang.String javaVersion, java.lang.String totalMemory, java.lang.String memRAM, java.lang.String nbProc)
            throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/UpdateUser");
        QName method = new QName(WSDL_URI, "UpdateUser");
        Object[] parameters = { email, pseudo, passwordOld, passwordNew, firstname, lastname, country, designerversion,
                productname, osName, osVersion, javaVersion, totalMemory, memRAM, nbProc };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    public java.math.BigInteger createUser50(java.lang.String pseudo, java.lang.String password, java.lang.String firstname,
            java.lang.String lastname, java.lang.String country, java.lang.String designerversion, java.lang.String productname,
            java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion, java.lang.String totalMemory,
            java.lang.String memRAM, java.lang.String nbProc) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/CreateUser50");
        QName method = new QName(WSDL_URI, "CreateUser50");
        Object[] parameters = { pseudo, password, firstname, lastname, country, designerversion, productname, osName, osVersion,
                javaVersion, totalMemory, memRAM, nbProc };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    public java.math.BigInteger createUser53(java.lang.String email, java.lang.String pseudo, java.lang.String password,
            java.lang.String firstname, java.lang.String lastname, java.lang.String country, java.lang.String designerversion,
            java.lang.String productname, java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion,
            java.lang.String totalMemory, java.lang.String memRAM, java.lang.String nbProc, java.lang.String uniqueId)
            throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/CreateUser53");
        QName method = new QName(WSDL_URI, "CreateUser53");
        Object[] parameters = { email, pseudo, password, firstname, lastname, country, designerversion, productname, osName,
                osVersion, javaVersion, totalMemory, memRAM, nbProc, uniqueId };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

    public java.math.BigInteger updateUser53(java.lang.String pseudo, java.lang.String password, java.lang.String firstname,
            java.lang.String lastname, java.lang.String country, java.lang.String designerversion, java.lang.String productname,
            java.lang.String osName, java.lang.String osVersion, java.lang.String javaVersion, java.lang.String totalMemory,
            java.lang.String memRAM, java.lang.String nbProc, java.lang.String uniqueId) throws java.rmi.RemoteException {
        if (cachedEndpoint == null) {
            throw new AxisFault("No endpoints found in the WSDL");
        }
        RPCServiceClient client = createCall();
        Options options = client.getOptions();
        options.setAction(BASE_URL + "/UpdateUser53");
        QName method = new QName(WSDL_URI, "UpdateUser53");
        Object[] parameters = { pseudo, password, firstname, lastname, country, designerversion, productname, osName, osVersion,
                javaVersion, totalMemory, memRAM, nbProc, uniqueId };
        Class[] returnTypes = new Class[] { BigInteger.class };

        try {
            Object[] response = client.invokeBlocking(method, parameters, returnTypes);

            if (response.length > 0) {
                return (BigInteger) response[0];
            }
        } catch (org.apache.axis2.AxisFault axisFaultException) {
            throw axisFaultException;
        }
        return new BigInteger("-1");
    }

}
