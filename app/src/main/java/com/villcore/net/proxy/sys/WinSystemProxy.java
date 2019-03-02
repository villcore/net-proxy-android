package com.villcore.net.proxy.sys;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/7/6.
 * <p>
 * windows系统下进行IE代理配置的类
 */
public class WinSystemProxy {

    private String exeRootPath;
    private File sysproxyFile;

    public WinSystemProxy(String exeRootPath) {
        this.exeRootPath = exeRootPath;
        String exePath = this.exeRootPath + File.separator + "sysproxy.exe";
        sysproxyFile = new File(exePath);
        if (!sysproxyFile.exists()) {
            throw new IllegalArgumentException("sysproxy.exe doesn't exist -> " + sysproxyFile.getAbsoluteFile());
        }
    }

    public Process buildProcess(String... commandAndArgs) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
        return pb.start();
    }

    //设置代理
    //sysproxy.exe set 1 //取消代理
    public void clearProxy() throws IOException {
        String commond = sysproxyFile.getAbsolutePath();
        Process process = buildProcess(commond, "set", "1");
    }

    //sysproxy.exe query //查询状态
    //以下是返回状态
    /**
     * 1
     * 127.0.0.1:10080
     * <local>
     * (null)
     */

    //自动使用配置脚本
    //sysproxy.exe pac pac_url
    //query返回状态

    /**
     * 5
     * 127.0.0.1:10080
     * <local>
     * http://l27.0.0.1:10080/
     */

    //使用全局代理
    //sysproxy.exe global http://127.0.0.1:10080
    public void setGlobalProxy(String proxyUrl) throws IOException {
        String commond = sysproxyFile.getAbsolutePath();
        Process process = buildProcess(commond, "global", proxyUrl);
    }

    //恢复初始状态
    //sysproxy.exe set 1
    public static void main(String[] args) throws IOException {
        WinSystemProxy proxy = new WinSystemProxy("win_utils");
        proxy.clearProxy();
        proxy.setGlobalProxy("http://127.0.0.1:10080");
    }
}
