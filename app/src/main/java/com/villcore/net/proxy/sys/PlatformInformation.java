package com.villcore.net.proxy.sys;

/**
 * Created by Administrator on 2017/7/6.
 */
public class PlatformInformation {
    public static String osName;
    public static String osArch;
    public static String osVersion;

    static {
        osName = System.getProperty("os.name"); //操作系统的名称
        osArch = System.getProperty("os.name"); //操作系统的架构
        osVersion = System.getProperty("os.version"); //操作系统的版本
    }
}
