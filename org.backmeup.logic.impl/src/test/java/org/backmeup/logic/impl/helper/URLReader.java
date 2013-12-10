package org.backmeup.logic.impl.helper;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLReader {
  private static final Logger logger = LoggerFactory.getLogger(URLReader.class);	
  
  public static String waitForResult() {
		try {
			ServerSocket ss = new ServerSocket(9998, 1);
			Socket client = ss.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter bw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
			String result = br.readLine();
			if (result == null) {
			  client.close();
			  ss.close();
			  return waitForResult();
			}
			bw.write("HTTP/1.0 200 OK\r\n\r\nHello World!"); bw.flush();
			client.close();
			String[] results = result.split(" ");
			String res = results[1];
			ss.close();
			if (res.contains("?"))
				res = res.split("\\?")[1];
			return res;
		} catch (IOException e) {
			logger.error("", e);
		}
		return null;
	}
	
	public static void launchBrowser(String url) {
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Action.BROWSE)) {
			logger.debug("no browser support");
			logger.debug("Start manually: " + url);
		}
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			logger.error("", e);
		} catch (URISyntaxException e) {
			logger.error("", e);
		}
	}
}
