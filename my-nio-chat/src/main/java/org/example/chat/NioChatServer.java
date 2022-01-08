package org.example.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author romic
 * @date 2022-01-09
 * @description 1.java NIO服务端程序，支持多个客户端同时连接 2.客户端初次连接，服务提示客户端输入昵称，如果昵称被占用 提示重新输入。如果昵称唯一，则登陆成功； 按照一定的规则组织数据(协议)
 *              3.有新的客户端登陆之后，群发欢迎信息同时统计在线人数，并且这些信息要通知所有的在线客户 4.服务器收到已登录客户端送的内容，转发给其他所有在线的客户端
 */
public class NioChatServer {
    private int port;

    private Charset charset = Charset.forName("UTF-8");

    /**
     * 记录在线人数和昵称
     */
    private static Set<String> users = new HashSet<String>();

    /**
     * 系统提示常量
     */
    private static String USER_EXISTS = "系统提示：该昵称已经存在，请换一个昵称";
    /**
     * 协议分隔符
     */
    private static String USER_CONTENT_SPLIT = "#@#";
    private Selector selector;

    public NioChatServer(int port) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务已启动,端口是:" + port);
    }

    public static void main(String[] args) throws Exception {
        new NioChatServer(8080).listen();
    }

    private void listen() throws Exception {
        while (true) {
            int wait = selector.select();
            if (wait == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                process(key);
            }
        }
    }

    private void process(SelectionKey key) throws Exception {
        if (key.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
            SocketChannel client = serverSocketChannel.accept();

            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            key.interestOps(SelectionKey.OP_ACCEPT);
            client.write(charset.encode("请输入你的昵称"));
        }
        if (key.isReadable()) {
            SocketChannel client = (SocketChannel)key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder content = new StringBuilder();
            try {
                while (client.read(byteBuffer) > 0) {
                    byteBuffer.flip();
                    content.append(charset.decode(byteBuffer));
                }
                key.interestOps(SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
                key.cancel();
                if (key.channel() != null) {
                    key.channel().close();
                }
            }
            if (content.length() > 0) {
                String[] onlineContent = content.toString().split(USER_CONTENT_SPLIT);
                if (onlineContent.length == 1) {
                    String nickName = onlineContent[0];
                    if (users.contains(nickName)) {
                        client.write(charset.encode(USER_EXISTS));
                    }
                    users.add(nickName);
                    int onlineCount = onlineCount();
                    String message = "欢迎 " + nickName + " 进入聊天室！当前在线人数：" + onlineCount;
                    broadCast(null, message);
                } else if (onlineContent.length > 1) {
                    String nickName = onlineContent[0];
                    String message = content.substring(nickName.length() + USER_CONTENT_SPLIT.length());
                    message = nickName + "说 " + message;
                    if (users.contains(nickName)) {
                        broadCast(client, message);
                    }
                }
            }
        }
    }

    private void broadCast(SocketChannel client, String message) {
        selector.keys().forEach(key -> {
            Channel targetChannel = key.channel();

            if (targetChannel instanceof SocketChannel && targetChannel != client) {
                SocketChannel target = (SocketChannel)targetChannel;
                try {
                    target.write(charset.encode(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int onlineCount() {
        int result = (int)selector.keys().stream().map(SelectionKey::channel)
            .filter(target -> target instanceof SocketChannel).count();
        return result;
    }
}
