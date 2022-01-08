package org.example.rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.example.rpc.protocol.InvokerProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class MyRegistryHandler extends ChannelInboundHandlerAdapter {
    private static Map<String, Object> registryMap = new ConcurrentHashMap<>();

    private List<String> className = new ArrayList<>();

    public MyRegistryHandler() {
        // 扫描所有需要注册的类
        scannerClass("org.example.rpc.provider");

        // 将扫描到的类注册到一个容器中
        doRegistry();
    }

    private void doRegistry() {
        if (className.size() == 0) {
            return;
        }
        className.forEach(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void scannerClass(String packagePath) {
        URL url = this.getClass().getClassLoader().getResource(packagePath.replaceAll("\\.", "/"));

        File dir = new File(url.getFile());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            // 如果是文件夹，递归处理
            if (file.isDirectory()) {
                scannerClass(packagePath + "." + file.getName());
            }
            className.add(packagePath + "." + file.getName().replace(".class", "").trim());
        }
    }

    /**
     * <p>
     * 1. netty将接受到请求数据，将msg转为自定义协议InvokerProtocol 2. consumer链接到注册中心，从注册中心{@code #registryMap}消费；
     * </p>
     * <p>
     * 注册中心数据接口key为类的全限定名称com.example.provider.RpcHelloServiceImpl； value为RpcHelloServiceImpl对象
     * </p>
     * 
     * @param ctx
     * @param msg
     * @return void
     * @author romic
     * @date 2022-01-08 21:58
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol)msg;

        if (registryMap.containsKey(request.getClassName())) {
            Object provider = registryMap.get(request.getClassName());
            Method method = provider.getClass().getMethod(request.getMethodName(), request.getParams());
            // 通过反射调用方法
            // method.invoke(Object obj,Object args[])的作用就是调用method类代表的方法，其中obj是对象名，args是传入实际参数
            result = method.invoke(provider, request.getValues());
        }

        ctx.writeAndFlush(result);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
