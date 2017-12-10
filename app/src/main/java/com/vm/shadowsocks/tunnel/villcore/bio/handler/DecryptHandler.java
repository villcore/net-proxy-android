package com.vm.shadowsocks.tunnel.villcore.bio.handler;


import com.vm.shadowsocks.tunnel.villcore.bio.pkg2.Package;
import com.vm.shadowsocks.tunnel.villcore.crypt.Crypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecryptHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(DecryptHandler.class);

    private Crypt crypt;

    public DecryptHandler(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    public Package handle(Package pkg) throws Exception {
        byte[] bytes = pkg.getBody();
        byte[] decryptBytes = crypt.decrypt(bytes);

        Package newPkg = Package.buildPackage(new byte[0], decryptBytes);
        return newPkg;
    }
}
