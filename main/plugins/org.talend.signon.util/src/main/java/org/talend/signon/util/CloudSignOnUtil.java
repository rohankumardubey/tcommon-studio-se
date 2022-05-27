package org.talend.signon.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.talend.utils.json.JSONObject;

public class CloudSignOnUtil{

    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    
    public static String getCodeChallenge(String seed) throws Exception {
        byte[] bytes = seed.getBytes("US-ASCII");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
    
    

}
