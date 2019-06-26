package com.hf.transferservice.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hf.transferservice.utils.TypeConversion;

/**
 * 异步tcp服务器
 * @author hefan
 * @date 2017/12/25 22:47
 */
public class TcpServer {

    private static Logger logger = LoggerFactory.getLogger(TcpServer.class);

    /**
     * 连接map
     */
    private static ConcurrentHashMap<String, SelectionKey> connectionMap = new ConcurrentHashMap<>();

    /**
     * macMap
     */
    private static ConcurrentHashMap<String, String> macMap = new ConcurrentHashMap<>();

    /**
     * 缓冲区大小
     */
    private static ByteBuffer buffer = ByteBuffer.allocate(1024 * 4);
    
    //其它相关定义 start
    private static Selector selector;
    private static ServerSocketChannel channel;
    private static ServerSocket socket;
    //其它相关定义 end

    /**
     * 默认非阻塞模式
     */
    private static boolean CONFIGURE_BLOCKING = false;

    public static TcpServer getInstance(){
        return Holder.instance;
    }

    private static class Holder {
        private static final TcpServer instance = new TcpServer();
    }

    public TcpServer() {
        try {
            /*初始化一个Selector*/
            selector = Selector.open();
            /*打开通道*/
            channel = ServerSocketChannel.open();
        }catch (Exception e){
            logger.error("TCP Server init error",e);
        }
    }

    /**
     * 开启阻塞模式
     * @return
     */
    public TcpServer block(){
        CONFIGURE_BLOCKING = true;
        return this;
    }

    /**
     * 开启监听
     * @author hefan
     * @date 创建时间：2017年6月19日 下午2:34:53
     * @param port 监听端口号
     * @throws Exception
     */
    public TcpServer start(Integer port) throws Exception {
        channel.configureBlocking(CONFIGURE_BLOCKING);
        /*本机IP*/
        //InetAddress ip = LocalIpUtils.getLocalHostIpv4();
        //logger.info(ip.toString());
        /*绑定IP和端口*/
        //InetSocketAddress address = new InetSocketAddress(ip,port);
        InetSocketAddress address = new InetSocketAddress(port);
        socket = channel.socket();
        socket.bind(address);
        /*启动监听*/
        logger.debug("TCP服务器开始监听...");
        listen();
        return this;
    }

    /**
     * 停止
     * @author hefan
     * @date 创建时间：2017年6月19日 下午2:29:57
     * @throws Exception
     */
    public TcpServer stop() throws IOException {
        channel.close();
        selector.close();
        logger.debug("TCP服务器停止监听...");
        return this;
    }

    /**
     * 监听
     * @author hefan
     * @date 创建时间：2017年6月19日 下午2:30:01
     * @throws Exception
     */
    private void listen() throws Exception {
        /*注册接收事件*/
        channel.register(selector,SelectionKey.OP_ACCEPT);
        /*无限循环*/
        while (true) {
            selector.select();
            /*轮询事件*/
            Iterator iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key =  (SelectionKey)iter.next();
                iter.remove();
                /*事件分类处理*/
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    logger.debug("新终端已连接:"+ sc.getRemoteAddress());
                }else if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel)key.channel();
                    String remoteAddress = sc.getRemoteAddress().toString();
                    int recvCount = 0;
                    try {
                        recvCount = sc.read(buffer);
                    } catch (Exception e){
                        //key.cancel();
                        continue;
                    }

                    if (recvCount > 0) {
                        byte[] arr = buffer.array();
                        byte[] res = new byte[recvCount];
                        for (int i=0;i < recvCount;i++){
                            res[i] = arr[i];
                        }
                        if(ConnectCode.BIND == arr[0]){
                            byte[] res2 = new byte[recvCount-1];
                            for (int i=0;i < recvCount-1;i++){
                                res2[i] = arr[i+1];
                            }
                            String mac = TypeConversion.bytes2HexString(res2);
                            logger.debug("mac：" + mac);
                            if(macMap.containsKey(remoteAddress)){
                                connectionMap.remove(macMap.get(remoteAddress));
                                macMap.remove(remoteAddress);
                            }
                            if(!connectionMap.containsKey(mac)){
                                connectionMap.put(mac,key);
                                macMap.put(remoteAddress,mac);
                                //绑定成功
                                logger.debug("绑定成功");
                            }else{
                                connectionMap.put(mac,key);
                                macMap.put(remoteAddress,mac);
                                logger.debug("绑定成功，存在相同key");
                            }
                        }else if(ConnectCode.SEND_DATA == arr[0]){
                            int macLength = arr[1];
                            int dataLength = arr[2] + arr[3];
                            int startFlag = 4;
                            byte[] res3 = new byte[macLength];
                            for (int i=0;i < macLength;i++){
                                res3[i] = arr[i+startFlag];
                            }
                            byte[] res4 = new byte[dataLength];
                            for (int i=0;i < dataLength;i++){
                                res4[i] = arr[i + startFlag + macLength];
                            }
                            String mac = TypeConversion.bytes2HexString(res3);
                            String data = TypeConversion.bytes2HexString(res4);
                            logger.debug("mac："+mac);
                            logger.debug("data："+data);
                            logger.debug("macLength:" + macLength);
                            logger.debug("dataLength:" + dataLength);
                            if(connectionMap.containsKey(mac) && connectionMap.get(mac).isValid()){
                                send(connectionMap.get(mac),res4);
                            }
                        }else if(ConnectCode.CHECK == arr[0]) {
                            String data = TypeConversion.bytes2HexString(res);
                            if("20202020".equals(data)){
                                send(key, ConnectCode.CHECK_RESPONSE);
                                logger.debug("check status：{}", data);
                            }
                        }else{

                        }
                        logger.debug(sc.getRemoteAddress() + "发来数据: "+ new String(res));
                        buffer.flip();
                    }
                    else {
                        release(remoteAddress);
                        logger.debug("终端已断开连接:"+ sc.getRemoteAddress());
                        sc.close();
                    }
                    buffer.clear();
                }else {
                	
                }
            }
        }
    }

    /**
     * 释放
     * @author hefan
     * @date 2017/12/26 0:15
     */
    public void release(String address){
        if(macMap.containsKey(address)){
            connectionMap.remove(macMap.get(address));
            macMap.remove(address);
        }
    }

    /**
     * 发送
     * @author hefan
     * @date 2017/12/26 0:15
     */
    public void send(SelectionKey key,byte[] data) {
        if (key == null)
            return;
        //ByteBuffer buff = (ByteBuffer) key.attachment();
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            sc.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            logger.error("数据发送异常",e);
        }
    }
}
