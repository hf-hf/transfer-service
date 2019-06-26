package com.hf.transferservice;

import com.hf.transferservice.tcp.TcpServer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransferServiceApplication implements CommandLineRunner {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(TransferServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TransferServiceApplication.class, args);
	}

	@Value("${tcp.server.port}")
	private Integer port;

	@Override
	public void run(String... arg0) throws Exception {

		try {
			TcpServer.getInstance().start(port);
		} catch (Exception e) {
			logger.error("tcpServer启动异常！",e);
		}

	}

    //@Bean
    //@Profile({"prod","test"})
    //public TcpConfig tcpConfig() {
    //    return new TcpConfig();
    //}
}
