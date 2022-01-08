package org.example.chat;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author romic
 * @date 2022-01-09
 * @description
 */
public class NioChatClient {

    private Selector selector;

    private SocketChannel client;
    private String nickName = "";
    private Charset charset = Charset.forName("UTF-8");

    /**
     * 系统提示常量
     */
    private static String USER_EXISTS = "系统提示：该昵称已经存在，请换一个昵称";
    /**
     * 协议分隔符
     */
    private static String USER_CONTENT_SPLIT = "#@#";

    public NioChatClient() throws Exception {
        selector = Selector.open();
        client = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws Exception {
        new NioChatClient().session();
    }

    private void session() {
        // 从服务器读取数据的线程
        new Reader().start();
        // 往服务器写数据的线程
        new Writer().start();
    }

    private class Reader extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    int readyChannels = 0;

                    readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        process(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void process(SelectionKey key) {
            try {
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel)key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    StringBuilder content = new StringBuilder();
                    while (sc.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        content.append(charset.decode(byteBuffer));
                    }
                    if (USER_EXISTS.equals(content.toString())) {
                        nickName = "";
                    }
                    System.out.println(content);
                    key.interestOps(SelectionKey.OP_READ);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Writer extends Thread {
        @Override
        public void run() {
            Scanner scan = new Scanner(System.in);
            while (scan.hasNextLine()) {
                try {
                    String line = scan.nextLine();
                    if ("".equals(line)) {
                        continue;
                    }
                    if ("".equals(nickName)) {
                        nickName = line;
                        line = nickName + USER_CONTENT_SPLIT;
                    } else {
                        line = nickName + USER_CONTENT_SPLIT + line;
                    }
                    client.write(charset.encode(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            scan.close();
        }
    }
}
