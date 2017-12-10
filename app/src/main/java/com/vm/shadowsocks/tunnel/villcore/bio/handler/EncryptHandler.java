package com.vm.shadowsocks.tunnel.villcore.bio.handler;

import com.vm.shadowsocks.tunnel.villcore.bio.pkg2.Package;
import com.vm.shadowsocks.tunnel.villcore.crypt.Crypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;


public class EncryptHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(EncryptHandler.class);

    private Crypt crypt;
    private boolean ivSend;

    public EncryptHandler(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    public Package handle(Package pkg) throws Exception {
        byte[] bytes = pkg.getBody();
        System.out.println("before encrypt = " + new String(bytes));

        if(!ivSend) {
            System.out.println("iv not send ...");

            byte[] iv = crypt.getIv();
            byte[] encryptHeader;
            byte[] encryptBody = crypt.encrypt(bytes);

            ByteBuffer tmp = ByteBuffer.wrap(new byte[4 + iv.length]);
            tmp.putInt(iv.length);
            tmp.put(iv);

            encryptHeader = tmp.array();

            Package newPkg = Package.buildPackage(encryptHeader, encryptBody);
            ivSend = true;
            return newPkg;
        } else {
            System.out.println("iv send ...");
            byte[] encryptHeader = new byte[0];
            byte[] encryptBody = crypt.encrypt(bytes);

            Package newPkg = Package.buildPackage(encryptHeader, encryptBody);
            return newPkg;
        }
    }
}