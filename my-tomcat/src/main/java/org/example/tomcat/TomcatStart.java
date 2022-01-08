package org.example.tomcat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.example.tomcat.http.MyRequest;
import org.example.tomcat.http.MyResponse;
import org.example.tomcat.http.MyServlet;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class TomcatStart {
    private ServerSocket serverSocket;

    private int port = 8080;

    private Properties properties = new Properties();
    private Map<String, MyServlet> servletMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        new TomcatStart().start();
    }

    /**
     * tomcat 启动入口
     *
     * @return void
     * @author romic
     * @date 2022-01-08 13:10
     */
    private void start() throws Exception {
        // 1.加载web.properties文件，解析配置
        init();

        // 2.启动服务端socket，等待用户请求
        serverSocket = new ServerSocket(port);

        System.out.println("tomcat 已经启动，监听端口是：" + port);
        while (true) {
            Socket client = serverSocket.accept();

            process(client);
        }
    }

    /**
     * 加载配置文件
     *
     * @return void
     * @author romic
     * @date 2022-01-08 13:15
     */
    private void init() {
        String webInfPath = this.getClass().getResource("/").getPath();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(webInfPath + "web.properties");
            properties.load(fis);

            for (Object k : properties.keySet()) {
                String key = k.toString();
                if (key.endsWith(".url")) {
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = properties.getProperty(key);
                    String className = properties.getProperty(servletName + ".className");

                    MyServlet obj = (MyServlet)Class.forName(className).newInstance();

                    servletMap.put(url, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Socket client) throws Exception {
        InputStream inputStream = client.getInputStream();
        OutputStream outputStream = client.getOutputStream();

        MyRequest myRequest = new MyRequest(inputStream);
        MyResponse myResponse = new MyResponse(outputStream);

        String url = myRequest.getUrl();
        if (servletMap.containsKey(url)) {
            servletMap.get(url).service(myRequest, myResponse);
        } else {
            myResponse.write("404 - NOT FOUND!");
        }
        outputStream.flush();
        outputStream.close();

        inputStream.close();
        client.close();

    }
}
