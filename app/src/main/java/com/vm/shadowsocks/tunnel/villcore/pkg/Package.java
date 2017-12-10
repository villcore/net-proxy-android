package com.vm.shadowsocks.tunnel.villcore.pkg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/12/10.
 */

public class Package {
    private byte[] len = new byte[4];
    private byte[] headerLen = new byte[4];
    private byte[] bodyLen = new byte[4];
    private byte[] header = new byte[4];
    private byte[] body = new byte[4];

    public void valueOf(byte[] header, byte[] body) {
        int len = Integer.valueOf(header.length + body.length);
        int headerLen = header.length;
        int bodyLen = body.length;

        this.len = intToBytes(len);
        this.headerLen = intToBytes(headerLen);
        this.bodyLen = intToBytes(bodyLen);
        this.header = header;
        this.body = body;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(len);
        bos.write(headerLen);
        bos.write(bodyLen);
        bos.write(header);
        bos.write(body);
        return bos.toByteArray();
    }


    private byte[] intToBytes(int val) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4]);
        byteBuffer.putInt(val);
        return byteBuffer.array();
    }


    public byte[] getLen() {
        return len;
    }

    public byte[] getHeaderLen() {
        return headerLen;
    }

    public byte[] getBodyLen() {
        return bodyLen;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }
}
