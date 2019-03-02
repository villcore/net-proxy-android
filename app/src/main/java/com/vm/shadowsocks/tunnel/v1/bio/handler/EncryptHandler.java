package com.vm.shadowsocks.tunnel.v1.bio.handler;

import com.vm.shadowsocks.tunnel.v1.bio.pkg2.Package;
import com.vm.shadowsocks.tunnel.v1.crypt.Crypt;

import java.nio.ByteBuffer;


public class EncryptHandler implements Handler {

    private Crypt crypt;
    private boolean ivSend;

    public EncryptHandler(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    public Package handle(Package pkg) throws Exception {
        byte[] bytes = pkg.getBody();

        if(!ivSend) {
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
            byte[] encryptHeader = new byte[0];
            byte[] encryptBody = crypt.encrypt(bytes);

            Package newPkg = Package.buildPackage(encryptHeader, encryptBody);
            return newPkg;
        }
    }
}
