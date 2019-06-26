//package com.hf.transferservice.config;
//
//import com.hf.transferservice.tcp.TcpServer;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//
//@Order(3)
//public class TcpConfig implements CommandLineRunner {
//
//    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(TcpConfig.class);
//
//    @Value("${tcp.server.port}")
//    private Integer port;
//
//    @Override
//    public void run(String... strings) {
//        try {
//            TcpServer.getInstance().start(port);
//        } catch (Exception e) {
//            logger.error("tcpServer启动异常！",e);
//        }
//    }
//}
