package com.vm.shadowsocks.tunnel.v1.bio.client;

import com.vm.shadowsocks.core.TcpProxyServer;
import com.vm.shadowsocks.tunnel.v1.bio.common.BytesToPackageTask;
import com.vm.shadowsocks.tunnel.v1.bio.common.Connection;
import com.vm.shadowsocks.tunnel.v1.bio.common.PackageToBytesTask;
import com.vm.shadowsocks.tunnel.v1.bio.handler.DecryptHandler;
import com.vm.shadowsocks.tunnel.v1.bio.handler.EncryptHandler;
import com.vm.shadowsocks.tunnel.v1.bio.handler.Handler;
import com.vm.shadowsocks.tunnel.v1.crypt.Crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by villcore on 2017/7/17.
 */
public class ClientConnection extends Connection {

    private String password;

    public ClientConnection(TcpProxyServer tcpProxyServer, Socket readSocket, Socket writeSocket, String password) {
        super(tcpProxyServer, readSocket, writeSocket);
        this.password = password;
    }

    @Override
    public void initTask(InputStream inputStream, OutputStream outputStream, InputStream inputStream2, OutputStream outputStream2, BytesToPackageTask bytesToPackageTask, PackageToBytesTask packageToBytesTask, Connection connection) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Crypt crypt = new Crypt();
        byte[] key = crypt.generateKey(this.password);
        byte[] iv = crypt.generateIv();

        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initDecrypt();
        crypt.initEncrypt();

        Handler encryptHander = new EncryptHandler(crypt);
        Handler decryptHander = new DecryptHandler(crypt);

        super.bytesToPackageTask = new BytesToPackageTask(connection, inputStream, outputStream2);
        super.bytesToPackageTask.addHandler("encrypt", encryptHander);

        super.packageToBytesTask = new PackageToBytesTask(connection, inputStream2, outputStream);
        super.packageToBytesTask.addHandler("decrypt", decryptHander);
    }
}
