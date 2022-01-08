package org.example.tomcat.servlet;

import org.example.tomcat.http.MyRequest;
import org.example.tomcat.http.MyResponse;
import org.example.tomcat.http.MyServlet;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class FirstServlet extends MyServlet {
    @Override
    public void doGet(MyRequest myRequest, MyResponse myResponse) throws Exception {
        doPost(myRequest, myResponse);
    }

    @Override
    public void doPost(MyRequest myRequest, MyResponse myResponse) throws Exception {
        myResponse.write("FirstServlet");
    }
}
