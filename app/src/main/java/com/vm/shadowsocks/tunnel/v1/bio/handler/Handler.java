package com.vm.shadowsocks.tunnel.v1.bio.handler;

import com.vm.shadowsocks.tunnel.v1.bio.pkg2.Package;

/**
 * Created by villcore on 2017/7/17.
 */
public interface Handler {
    public Package handle(Package pkg) throws Exception;
}
