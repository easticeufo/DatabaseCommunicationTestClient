package com.madongfang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.madongfang.util.CommonUtil;

@Component
public class AppStart implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		logger.info("AppStart start");
		
		final String sqlString = "insert into Test(value) values('easccc')";
		
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			byte[] head = new byte[40];
			byte[] body = sqlString.getBytes();
			System.arraycopy(commonUtil.intToByteArray(1), 0, head, 0, 4);
			System.arraycopy(commonUtil.intToByteArray(body.length), 0, head, 4, 4);
			System.arraycopy(commonUtil.md5(sqlString+password).getBytes(), 0, head, 8, 32);
			out.write(head);
			out.write(body);
			
			byte[] resp = new byte[260];
			int off = 0;
			int len = resp.length;
			while (len > 0)
			{
				int readlen = in.read(resp, off, len);
				if (readlen < 0)
				{
					logger.warn("in.read < 0");
					throw new IOException("in.read < 0");
				}
				off += readlen;
				len -= readlen;
			}
			
			int responseCode = commonUtil.byteArrayToInt(resp);
			logger.debug("responseCode={}", responseCode);
			if (responseCode < 0)
			{
				logger.debug("response string={}", new String(resp, 4, resp.length - 4, "UTF-8"));
			}
			else 
			{
				logger.debug("affectedRow={}, autoKey={}", commonUtil.byteArrayToInt(resp, 4), commonUtil.byteArrayToInt(resp, 8));
			}
		} finally {
			if (socket != null)
			{
				socket.close();
			}
		}
		
		logger.info("AppStart stop");
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${tcpServer.host}")
	private String host;
	
	@Value("${tcpServer.port}")
	private int port;
	
	@Value("${tcpServer.password}")
	private String password;
	
	@Autowired
	private CommonUtil commonUtil;
}
