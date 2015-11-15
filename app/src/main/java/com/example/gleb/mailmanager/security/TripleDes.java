package com.example.gleb.mailmanager.security;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Gleb on 30.10.2015.
 */
public class TripleDes {
    public static int MAX_KEY_LENGTH = DESedeKeySpec.DES_EDE_KEY_LEN;
    private static String ENCRYPTION_KEY_TYPE = "DESede";
    private static String ENCRYPTION_ALGORITHM = "DESede/CBC/PKCS5Padding";
    private final SecretKeySpec keySpec;
    private IvParameterSpec iv;

    public TripleDes(String passphrase, String email) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] hash;
        byte[] key;
        // get bytes representation of the password
        key = passphrase.getBytes("UTF-8");

        key = pushKeyToLength(key, MAX_KEY_LENGTH);
        keySpec = new SecretKeySpec(key, ENCRYPTION_KEY_TYPE);

        //initialize vector initialisation with md5 hash
        MessageDigest digest = MessageDigest.getInstance("MD5");
        hash = digest.digest(email.getBytes("UTF-8"));
        iv = new IvParameterSpec(Arrays.copyOfRange(hash, 0, 8));
    }

    /*
    * Push key to length of 192 bits
    * @param byte[] key        Text key
    * @param int len           Length of key
    * @param byte[]            Key to length
    * */
    private byte[] pushKeyToLength(byte[] key, int len) {
        byte[] newKey = new byte[len];
        System.arraycopy(key, 0, newKey, 0, Math.min(key.length, len));
        return newKey;
    }

    // standard stuff
    public byte[] encrypt(byte[] unencrypted) throws GeneralSecurityException {
            return doCipher(unencrypted, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        return doCipher(encrypted, Cipher.DECRYPT_MODE);
    }

    private byte[] doCipher(byte[] original, int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(mode, keySpec, iv);
        return cipher.doFinal(original);
    }

    public static String generateString(String characters) {
        Random r = new Random();
        Random rng = new Random();
        int length = r.nextInt(50);
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}
