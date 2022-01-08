package org.example.netty.servlet;

import org.example.netty.http.MyRequest;
import org.example.netty.http.MyResponse;
import org.example.netty.http.MyServlet;

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
