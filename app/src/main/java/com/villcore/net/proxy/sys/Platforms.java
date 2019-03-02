package com.villcore.net.proxy.sys;

/**
 * Created by Administrator on 2017/7/6.
 */
public class Platforms {
    public static Platform getPlatform(String osName) {
        String enumName = osName.replace(" ", "_").toUpperCase();
        return Platform.valueOf(enumName);
    }
}
