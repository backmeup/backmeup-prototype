package org.backmeup.logic.impl.helper;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class URLReader {
	public static String waitForResult() {
		try {
			ServerSocket ss = new ServerSocket(8080);
			Socket client = ss.accept();
			InputStream is = client.getInputStream();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			bw.write("HTTP");
			String result = br.readLine();			
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
