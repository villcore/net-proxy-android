package com.vm.shadowsocks.tunnel.villcore.bio.handler;

import com.vm.shadowsocks.tunnel.villcore.bio.pkg2.Package;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

/**
 * Created by villcore on 2017/7/17.
 */
public interface Handler {
    public Package handle(Package pkg) throws Exception;
}
