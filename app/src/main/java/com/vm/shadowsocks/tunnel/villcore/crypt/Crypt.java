package com.vm.shadowsocks.tunnel.villcore.crypt;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.exception.ExtIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by villcore on 2017/12/10.
 */

public class Crypt {
    private static final int IV_LEN = 16;

    private StreamCipher enc;
    private StreamCipher dec;
    private byte[] key;
    private byte[] iv;

    public byte[] generateKey(String password) {
        MessageDigest md = null;
        byte[] keys = new byte[IV_LEN];
        byte[] temp = null;
        byte[] hash = null;
        byte[] passwordBytes = null;
        int i = 0;

        try {
            md = MessageDigest.getInstance("MD5");
            passwordBytes = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            return null;
        }

        while (i < keys.length) {
            if (i == 0) {
                hash = md.digest(passwordBytes);
                temp = new byte[passwordBytes.length + hash.length];
            } else {
                System.arraycopy(hash, 0, temp, 0, hash.length);
                System.arraycopy(passwordBytes, 0, temp, hash.length, passwordBytes.length);
                hash = md.digest(temp);
            }
            System.arraycopy(hash, 0, keys, i, hash.length);
            i += hash.length;
        }

        byte[] keysl = new byte[IV_LEN];
        System.arraycopy(keys, 0, keysl, 0, IV_LEN);
        return keysl;
    }

    public byte[] generateIv() {
        byte[] bytes = new byte[IV_LEN];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public byte[] getIv() {
        return this.iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getKey() {
        return this.key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public void initEncrypt() {
        CipherParameters cipherParameters = getCipherParameters(key, iv);
        try {
            enc = getCipher();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        enc.init(true, cipherParameters);
}

    public void initDecrypt() {
        CipherParameters cipherParameters = getCipherParameters(key, iv);
        try {
            dec = getCipher();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        dec.init(false, cipherParameters);
    }

    public byte[] encrypt(byte[] src) {
        byte[] dst = new byte[src.length];
        int processed = enc.processBytes(src, 0, src.length, dst, 0);
        return dst;
    }

    public byte[] decrypt(byte[] src) {
        byte[] dst = new byte[src.length];
        int processed = dec.processBytes(src, 0, src.length, dst, 0);
        return dst;
    }

    private CipherParameters getCipherParameters(byte[] key, byte[] iv) {
        return new ParametersWithIV(new KeyParameter(key), iv);
    }

    private StreamBlockCipher getCipher() throws InvalidAlgorithmParameterException {
        AESFastEngine engine = new AESFastEngine();
        StreamBlockCipher cipher;

        cipher = new CFBBlockCipher(engine, IV_LEN * 8);

        return cipher;
    }

    public static void main(String[] args) {
        Crypt crypt = new Crypt();

        byte[] key = crypt.generateKey("villcore");
        byte[] iv = crypt.generateIv();

        try {
            byte[] readIv = read("d://iv.dat");
            byte[] readEncrypt = read("d://encrypt.dat");

            System.out.println(readIv.length);
            System.out.println(readEncrypt.length);
            crypt.setIv(readIv);
            crypt.setKey(key);
            crypt.initDecrypt();

            byte[] real = crypt.decrypt(readEncrypt);
            System.out.println("--" + new String(real));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static byte[] read(String path) throws IOException {
        File file = new File(path);
        byte[] bytes = new byte[(int) file.length()];

        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(bytes);
        }
        return bytes;
    }
}
