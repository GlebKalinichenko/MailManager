package com.example.gleb.mailmanager.security;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gleb on 30.10.2015.
 */
public class SHA1 {
    public static int hexSha1(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
//            StringBuffer hexString = new StringBuffer();
//
//            for (int i = 0; i < hash.length; i++) {
//                String hex = Integer.toHexString(0xff & hash[i]);
//                if(hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
//            }
//
//            return hexString.toString();
            return Math.abs(ByteBuffer.wrap(hash).getInt() % 10);
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static byte[] hexSha1Byte(String base) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(base.getBytes("UTF-8"));
        return hash;
    }
}
