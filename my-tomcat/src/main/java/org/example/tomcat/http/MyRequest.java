package org.example.tomcat.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class MyRequest {

    private String url;
    private String method;

    public MyRequest(InputStream inputStream) {
        // 获取http内容
        String content = "";
        byte[] buff = new byte[1024];
        int len = 0;
        try {
            if ((len = inputStream.read(buff)) > 0) {
                content = new String(buff, 0, len);
            }

            String line = content.split("\\n")[0];
            String[] arr = line.split("\\s");

            this.method = arr[0];
            this.url = arr[1].split("\\?")[0];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
