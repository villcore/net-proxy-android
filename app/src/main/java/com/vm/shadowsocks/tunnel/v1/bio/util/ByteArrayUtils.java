package com.vm.shadowsocks.tunnel.v1.bio.util;

/**
 * Created by villcore on 2017/7/17.
 */
public class ByteArrayUtils {
    public static byte[]  trimByteArray(byte[] bytes, int offset, int len) {
        byte[] newBytes = new byte[len];
        System.arraycopy(bytes, offset, newBytes, 0, len);
        return newBytes;
    }

    public static void cpyToNew(byte[] src, byte[] dst, int srcOff, int dstOff, int len) {
        System.arraycopy(src, srcOff, dst, dstOff, len);
    }
}
