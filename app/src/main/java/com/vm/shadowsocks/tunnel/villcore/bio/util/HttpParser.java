package com.vm.shadowsocks.tunnel.villcore.bio.util;


import java.net.InetSocketAddress;

public class HttpParser {

    public static InetSocketAddress parseAddress3(byte[] httpRequest) {
        for (int i = 0; i < httpRequest.length; i++) {
            if (httpRequest[i] == 10 || httpRequest[i] == 13) {
                String firstLine = new String(httpRequest, 0, i);

                int start = 0, end = 0;
                for (int j = 0; j < i; j++) {
                    if (httpRequest[j] == 32) {
                        start = j;
                        break;
                    }
                }

                for (int j = i - 1; j > 0; j--) {
                    if (httpRequest[j] == 32) {
                        end = j;
                        break;
                    }
                }

                String addressInfo = new String(httpRequest, start, end - start).trim();
                URL url = new URL(addressInfo);

                return new InetSocketAddress(url.getIP(), url.getPort());
            }
        }
        return null;
    }

    public static InetSocketAddress parseAddress2(byte[] httpRequest, int off, int len) {
        byte[] arr = new byte[len];
        System.arraycopy(httpRequest, off, arr, 0, len);
        return parseAddress2(arr);
    }

    public static InetSocketAddress parseAddress2(byte[] httpRequest) {

        for (int i = 0; i < httpRequest.length; i++) {
            if (httpRequest[i] == 10 || httpRequest[i] == 13) {
                String firstLine = new String(httpRequest, 0, i);
//                System.out.println("request content ========== " + new String(httpRequest));
//                System.out.println("http first line ========== " + firstLine);

                int start = 0, end = 0;
                for (int j = 0; j < i; j++) {
                    if (httpRequest[j] == 32) {
                        start = j;
                        break;
                    }
                }

                for (int j = i - 1; j > 0; j--) {
                    if (httpRequest[j] == 32) {
                        end = j;
                        break;
                    }
                }

                String addressInfo = new String(httpRequest, start, end - start).trim();

                int preSplitIndex = 0;
                int postSplitIndex = addressInfo.length();
                String correctAddress = "";
                String protocal = "";

                preSplitIndex = addressInfo.indexOf("://");

                if (preSplitIndex > 0) {
                    preSplitIndex = preSplitIndex + "://".length();
                } else {
                    preSplitIndex = 0;
                }

                postSplitIndex = addressInfo.indexOf("/", preSplitIndex);
                if (postSplitIndex > 0) {

                } else {
                    postSplitIndex = addressInfo.length();
                }

                protocal = addressInfo.substring(0, preSplitIndex).replace("://", "");
                correctAddress = addressInfo.substring(preSplitIndex, postSplitIndex).trim();

//                            System.out.println(correctAddress);


                String[] addressAndPortArr = correctAddress.split(":");

                String address = addressAndPortArr[0];
                int port = 80;

                String portStr = addressAndPortArr.length > 1 ? addressAndPortArr[1] : "";
                if (portStr != null) {
                    port = 80;

                    if (protocal != null && protocal.length() != 0) {
                        if ("ftp".equalsIgnoreCase(protocal)) {
                            port = 21;
                        }
                    }
                }

                if (portStr != null && !portStr.isEmpty()) {
                    port = Integer.valueOf(portStr);
                }

                //System.out.printf("protocal = %s, address: %s, port = %d\n", protocal, address, port);
                return new InetSocketAddress(address, port);
            }
        }
        return null;
    }

    public static String[] parseAddress4(byte[] httpRequest) {
        for (int i = 0; i < httpRequest.length; i++) {
            if (httpRequest[i] == 10 || httpRequest[i] == 13) {
                String firstLine = new String(httpRequest, 0, i);
//                System.out.println("request content ========== " + new String(httpRequest));
//                System.out.println("http first line ========== " + firstLine);

                int start = 0, end = 0;
                for (int j = 0; j < i; j++) {
                    if (httpRequest[j] == 32) {
                        start = j;
                        break;
                    }
                }

                for (int j = i - 1; j > 0; j--) {
                    if (httpRequest[j] == 32) {
                        end = j;
                        break;
                    }
                }

                String addressInfo = new String(httpRequest, start, end - start).trim();

                int preSplitIndex = 0;
                int postSplitIndex = addressInfo.length();
                String correctAddress = "";
                String protocal = "";

                preSplitIndex = addressInfo.indexOf("://");

                if (preSplitIndex > 0) {
                    preSplitIndex = preSplitIndex + "://".length();
                } else {
                    preSplitIndex = 0;
                }

                postSplitIndex = addressInfo.indexOf("/", preSplitIndex);
                if (postSplitIndex > 0) {

                } else {
                    postSplitIndex = addressInfo.length();
                }

                protocal = addressInfo.substring(0, preSplitIndex).replace("://", "");
                correctAddress = addressInfo.substring(preSplitIndex, postSplitIndex).trim();

                String[] addressAndPortArr = correctAddress.split(":");

                String address = addressAndPortArr[0];
                int port = 80;

                String portStr = addressAndPortArr.length > 1 ? addressAndPortArr[1] : "80";
                if (portStr != null) {
                    port = 80;

                    if (protocal != null && protocal.length() != 0) {
                        if ("ftp".equalsIgnoreCase(protocal)) {
                            port = 21;
                        }
                    }
                }

                if (portStr != null && !portStr.isEmpty()) {
                    port = Integer.valueOf(portStr);
                }

                //System.out.printf("protocal = %s, address: %s, port = %d\n", protocal, address, port);
                return new String[]{address, portStr};
            }
        }
        return new String[]{};
    }

    public static InetSocketAddress parseAddress(byte[] httpRequest) {
        System.out.println(">>>" + new String(httpRequest));

        for (int i = 0; i < httpRequest.length; i++) {
            int a = httpRequest[i];
            char c = (char) a;
            System.out.print(a + "-");
        }

        System.out.println();

//        for (int i = 0; i < httpRequest.length; i++) {
//            int a = httpRequest[i];
//            char c = (char) a;
//            System.out.print(c + "-");
//        }
        int a = 0;
        if (a == 0)
            return new InetSocketAddress("www.baidu.com", 443);

        for (int i = 0; i < httpRequest.length; i++) {
            System.out.print(httpRequest[i] + "(" + (char) httpRequest[i] + ")" + "-");
            if (i + 4 > httpRequest.length) {
                throw new IllegalArgumentException("http request content error...");
            }

            if (httpRequest[i] == 13 && httpRequest[i + 1] == 10 && httpRequest[i + 2] == 13 && httpRequest[i + 3] == 10) {
                String header = new String(httpRequest, 0, i);

                System.out.println("http header = \n" + header);
            }
//            if (httpRequest[i] == 10 || httpRequest[i] == 13) {
//
//                System.out.println(httpRequest[i] + " - " + httpRequest[i+1]);
//
//                String firstLine = new String(httpRequest, 0, i);
//                //System.out.println("request content ========== " + new String(httpRequest));
//                System.out.println("http first line ========== " + firstLine);
//
//                int start = 0, end = 0;
//                for (int j = 0; j < i; j++) {
//                    if (httpRequest[j] == 32) {
//                        start = j;
//                        break;
//                    }
//                }
//
//                for (int j = i - 1; j > 0; j--) {
//                    if (httpRequest[j] == 32) {
//                        end = j;
//                        break;
//                    }
//                }
//
//                String addressInfo = new String(httpRequest, start, end - start).trim();
//
//                int preSplitIndex = 0;
//                int postSplitIndex = addressInfo.length();
//                String correctAddress = "";
//                String protocal = "";
//
//                preSplitIndex = addressInfo.indexOf("://");
//
//                if (preSplitIndex > 0) {
//                    preSplitIndex = preSplitIndex + "://".length();
//                } else {
//                    preSplitIndex = 0;
//                }
//
//                postSplitIndex = addressInfo.indexOf("/", preSplitIndex);
//                if (postSplitIndex > 0) {
//
//                } else {
//                    postSplitIndex = addressInfo.length();
//                }
//
//                protocal = addressInfo.substring(0, preSplitIndex).replace("://", "");
//                correctAddress = addressInfo.substring(preSplitIndex, postSplitIndex).trim();
//
////                            System.out.println(correctAddress);
//
//
//                String[] addressAndPortArr = correctAddress.split(":");
//
//                String address = addressAndPortArr[0];
//                int port = 80;
//
//                String portStr = addressAndPortArr.length > 1 ? addressAndPortArr[1] : "";
//                if (portStr != null) {
//                    port = 80;
//
//                    if (protocal != null && protocal.length() != 0) {
//                        if ("ftp".equalsIgnoreCase(protocal)) {
//                            port = 21;
//                        }
//                    }
//                }
//
//                if (portStr != null && !portStr.isEmpty()) {
//                    port = Integer.valueOf(portStr);
//                }
//
//                System.out.printf("protocal = %s, address: %s, port = %d\n", protocal, address, port);
            //return new InetSocketAddress(address, port);

            return new InetSocketAddress("www.baidu.com", 443);
        }
        return new InetSocketAddress("www.baidu.com", 443);
    }
}



