package com.vm.shadowsocks.tunnel.villcore;

import com.vm.shadowsocks.tunnel.shadowsocks.AesCrypt;
import com.vm.shadowsocks.tunnel.shadowsocks.CryptFactory;
import com.vm.shadowsocks.tunnel.shadowsocks.ICrypt;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by villcore on 2017/12/9.
 */

public class CryptTest {
    public static void main(String[] args) {
        System.out.println("test...");
        File file = new File("d://encrypt.dat");
        System.out.println(file.length());

        ICrypt crypt = CryptFactory.get(AesCrypt.CIPHER_AES_128_CFB, "villcore");
    }
}
