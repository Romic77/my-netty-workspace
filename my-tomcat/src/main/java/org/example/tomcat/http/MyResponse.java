package org.example.tomcat.http;

import java.io.OutputStream;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class MyResponse {
    private OutputStream outputStream;

    public MyResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String s) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 ok\n").append("Content-Type: text/html;\n").append("\r\n").append(s);
        outputStream.write(sb.toString().getBytes());

    }

}
