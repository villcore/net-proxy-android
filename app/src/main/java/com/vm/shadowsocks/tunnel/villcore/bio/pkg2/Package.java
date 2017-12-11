package com.vm.shadowsocks.tunnel.villcore.bio.pkg2;

import com.vm.shadowsocks.tunnel.villcore.bio.util.ByteArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/18.
 *
 * Package struct
 *
 * [SIZE (INT)] + [HEADER_SIZE (INT)] + [BODY_SIZE (INT)] + [HEADER (BYTE ARRAY)] + [BODY (BYTE ARRAY)]
 */
public class Package {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[]{};

    public static Package buildPackage(byte[] header, byte[] body) {
        Package pkg = new Package();

        int size = header.length + body.length;
        pkg.setHeader(header);
        pkg.setBody(body);
        return pkg;
    }

    public static byte[] toBytes(Package pkg) {
        byte[] sizeBytes =
                ByteBuffer.wrap(new byte[PkgConf.getPackageMetaLen()]).putInt(pkg.getSize()).putInt(pkg.getHeaderLen()).putInt(pkg.getBodyLen()).array();
        byte[] bytes = new byte[sizeBytes.length + pkg.getHeaderLen() + pkg.getBodyLen()];

        ByteArrayUtils.cpyToNew(sizeBytes, bytes, 0, 0, sizeBytes.length);
        ByteArrayUtils.cpyToNew(pkg.getHeader(), bytes, 0, sizeBytes.length, pkg.getHeaderLen());
        ByteArrayUtils.cpyToNew(pkg.getBody(), bytes, 0, sizeBytes.length + pkg.getHeaderLen(), pkg.getBodyLen());

        return bytes;
    }

    private byte[] header = EMPTY_BYTE_ARRAY;
    private byte[] body = EMPTY_BYTE_ARRAY;

    public int getSize() {
        return header.length + body.length;
    }

    public int getBodyLen() {
        return body.length;
    }

    public int getHeaderLen() {
        return header.length;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
        //this.headerLen = headerLen;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
        //this.bodyLen = body.length;
    }

    public void readPackageWithHeader(InputStream inputStream) throws IOException {
        byte[] metaBytes = new byte[PkgConf.getPackageMetaLen()];
        readFully(inputStream, metaBytes);

        ByteBuffer byteBuffer = ByteBuffer.wrap(metaBytes);
        int size = byteBuffer.getInt();
        int headerLen = byteBuffer.getInt();
        int bodyLen = byteBuffer.getInt();

        if(size < 0 || size > 10 * 1024 * 1024){
            throw new IOException("illegal byte size...");
        }

        byte[] all = new byte[headerLen + bodyLen];
        readFully(inputStream, all);

        byte[] header = new byte[headerLen];
        byte[] body = new byte[bodyLen];
        ByteArrayUtils.cpyToNew(all, header, 0, 0, headerLen);
        ByteArrayUtils.cpyToNew(all, body, headerLen, 0, bodyLen);

        setHeader(header);
        setBody(body);
        //LOG.debug("read page without header, header size = {}, body size = {} ", getHeader().length, getBody().length);
    }

    public void readPackageWithoutHeader(InputStream inputStream) throws IOException {
        byte[] bytes = readFully(inputStream);
        if(bytes.length == 0) {
            return;
        }

        setBody(bytes);
        setHeader(newHeader());
        //LOG.debug("read page without header, header size = {}, body size = {} ", getHeader().length, getBody().length);
    }

    public void writePackageWithHeader(OutputStream outputStream) throws IOException {
        //LOG.debug("writePackageWithHeader  header = {}, body = {}", getHeader() == null, getBody() == null);
        writeFully(outputStream, toBytes(this));
    }

    public void writePackageWithoutHeader(OutputStream outputStream) throws IOException {
        if(getBody().length != 0) {
            writeFully(outputStream, getBody());
        }
    }

    public byte[] readFully(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1 * 1024 * 1024];
        int pos = inputStream.read(bytes);
        if(pos == -1) {
            throw new IOException("socket closed");
        }
        if(pos == bytes.length) {
            return bytes;
        } else {
            return ByteArrayUtils.trimByteArray(bytes, 0, pos);
        }
    }

    public void readFully(InputStream inputStream, byte[] bytes) throws IOException {
        int pos = -1;
        int readSize = 0;

        while(true) {
            if((pos = inputStream.read(bytes, readSize, bytes.length - readSize)) > 0) {
                //LOG.debug("pso = {}", pos);
                readSize += pos;
                if(readSize >= bytes.length) {
                    break;
                }
            }
            else if(pos == -1) {
                throw new IOException("socket closed");
            } else {
                return;
            }
        }
        //LOG.debug("read full size = {}", readSize);
    }

    private void writeFully(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    public byte[] newHeader() {
        return new byte[]{};
    }
}
