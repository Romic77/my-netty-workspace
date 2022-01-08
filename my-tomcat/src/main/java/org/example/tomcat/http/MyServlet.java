package org.example.tomcat.http;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public abstract class MyServlet {
    public abstract void doGet(MyRequest myRequest, MyResponse myResponse) throws Exception;

    public abstract void doPost(MyRequest myRequest, MyResponse myResponse) throws Exception;

    public void service(MyRequest myRequest, MyResponse myResponse) throws Exception {
        if ("GET".equalsIgnoreCase(myRequest.getMethod())) {
            doGet(myRequest, myResponse);
        } else {
            doPost(myRequest, myResponse);
        }
    }
}
