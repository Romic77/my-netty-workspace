package org.example.netty;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.example.netty.http.MyRequest;
import org.example.netty.http.MyResponse;
import org.example.netty.http.MyServlet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class NettyStart {

    private int port = 8080;

    private Properties properties = new Properties();
    private Map<String, MyServlet> servletMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        new NettyStart().start();
    }

    /**
     * tomcat 启动入口
     *
     * @return void
     * @author romic
     * @date 2022-01-08 13:10
     */
    private void start() throws Exception {
        EventLoopGroup bossGroup = null;
        EventLoopGroup workGroup = null;
        try {
            // 1.加载web-nio.properties文件，解析配置
            init();

            // boss线程
            bossGroup = new NioEventLoopGroup();
            // worker线程
            workGroup = new NioEventLoopGroup();

            // 创建netty服务端对象
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(workGroup, workGroup);
            // 配置主线程处理逻辑
            serverBootstrap.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    // 处理回调逻辑

                    // 响应数据编码
                    channel.pipeline().addLast(new HttpResponseEncoder());
                    // 请求数据解码
                    channel.pipeline().addLast(new HttpRequestDecoder());
                    // 处理业务逻辑
                    channel.pipeline().addLast(new MyTomcatHandler());

                }
            }).option(ChannelOption.SO_BACKLOG, 128)
                // 保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 启动服务
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("tomcat 已经启动，监听端口是：" + port);

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
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
            fis = new FileInputStream(webInfPath + "web-nio.properties");
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

    public class MyTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest)msg;

                MyRequest request = new MyRequest(ctx, req);
                MyResponse response = new MyResponse(ctx, req);

                String url = request.getUrl();
                if (servletMap.containsKey(url)) {
                    servletMap.get(url).service(request, response);
                    return;
                }
                response.write("404 - NOT FOUND");
            }
        }
    }
}
