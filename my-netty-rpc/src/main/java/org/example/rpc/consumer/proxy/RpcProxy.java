package org.example.rpc.consumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.example.rpc.protocol.InvokerProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] interfaces = clazz.isInterface() ? new Class[] {clazz} : clazz.getInterfaces();
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy);
    }

    private static class MethodProxy implements InvocationHandler {
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                method.invoke(this, args);
            }
            return rpcInvoke(proxy, method, args);
        }

        private Object rpcInvoke(Object proxy, Method method, Object[] args) {
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());
            msg.setValues(args);

            RprProxyHandler consumerHandler = new RprProxyHandler();

            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap client = new Bootstrap();
                client.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 接口客户端请求的处理流程
                        ChannelPipeline pipeline = channel.pipeline();
                        // 通用的解码器设置
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        // 通用的编码器设置
                        pipeline.addLast(new LengthFieldPrepender(4));
                        // 对象编码器
                        pipeline.addLast("encoder", new ObjectEncoder());
                        // 对象解码器
                        pipeline.addLast("decoder",
                            new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                        pipeline.addLast("handler", consumerHandler);

                    }
                }).option(ChannelOption.TCP_NODELAY, true);

                ChannelFuture channelFuture = client.connect("127.0.0.1", 8080).sync();
                channelFuture.channel().writeAndFlush(msg).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }

            return consumerHandler.getResponse();
        }
    }
}
