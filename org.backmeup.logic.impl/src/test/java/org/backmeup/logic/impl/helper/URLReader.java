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

public class URLReader {
  
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
			e.printStackTrace();
		}
		return null;
	}
	
	public static void launchBrowser(String url) {
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Action.BROWSE)) {
			System.err.println("no browser support");
			System.out.println("Start manually: " + url);
		}
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
