package com.vm.shadowsocks.tunnel.villcore.bio.pkg2;

public class PkgConf {

    public static int getEndryptPackageHeaderLen() {
        return 4 + 4 + 8 + getIvBytesLen();
    }

    public static int getIvBytesLen() {
        //encrypt size + iv + normal header len
        return 16;
    }

    public static int getDefaultPackageHeaderLen() {
        return 4 + 4 + 8 + 4;
    }

    public static float getInterferenceFactor() {
        return 0.0f;
    }

    public static int getTransferPackageHeaderLen() {
        return 4;
    }

    public static int getPackageMetaLen() {
        return 4 + 4 + 4;
    }

    public static int getUserPackageHeaderLen() {
        return 4 + 8;
    }
}
