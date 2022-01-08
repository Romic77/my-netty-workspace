package org.example.rpc.provider;

import org.example.rpc.api.IRpcHelloService;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class RpcHelloServiceImpl implements IRpcHelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
