package com.example.gleb.mailmanager.security;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Gleb on 27.09.2015.
 */
public class RSA {
    private static KeyPairGenerator kpg;
    private static KeyPair kp;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static byte[] encryptedBytes, decryptedBytes;
    private static Cipher cipher, cipher1;
    private static String encrypted, decrypted;

    public RSA() throws NoSuchAlgorithmException {
        this.kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        kp = kpg.genKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();
    }

    public static byte[] encryptRSA(byte[] plain, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = cipher.doFinal(plain);

        encrypted = bytesToString(encryptedBytes);

        return encryptedBytes;

    }

    public static byte[] decryptRSA(byte[] result, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = cipher1.doFinal(result);
        decrypted = new String(decryptedBytes);
        return decryptedBytes;

    }

    public static String createFile(String name, byte[] value, String email) throws IOException {
        File gpxfile = new File(Environment.getExternalStorageDirectory() + "/" + email + "/Keys", name);
        FileOutputStream fos = new FileOutputStream(gpxfile);
        fos.write(value);
        fos.close();
        return gpxfile.getAbsolutePath();
    }

    public static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
