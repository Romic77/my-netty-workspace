package org.example.rpc.consumer;

import org.example.rpc.api.IRpcHelloService;
import org.example.rpc.api.IRpcService;
import org.example.rpc.consumer.proxy.RpcProxy;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService rpcHelloService = RpcProxy.create(IRpcHelloService.class);
        System.out.println(rpcHelloService.hello("zz"));

        IRpcService rpcService = RpcProxy.create(IRpcService.class);
        System.out.println("8+2=" + rpcService.add(8, 2));
        System.out.println("8-2=" + rpcService.sub(8, 2));
        System.out.println("8*2=" + rpcService.mult(8, 2));
        System.out.println("8/2=" + rpcService.div(8, 2));
    }
}
