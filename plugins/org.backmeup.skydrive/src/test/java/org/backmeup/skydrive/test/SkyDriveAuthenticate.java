package org.backmeup.skydrive.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.skydrive.Authenticator;

public class SkyDriveAuthenticate {
	public static void main(String[] args) throws IOException {
		// first authenticate a user to skydrive; store his profile as auth.props file 
		Authenticator auth = new Authenticator();
		Properties props = new Properties();
		props.setProperty("callback", "http://www.localhost.at:9998");
		String url = auth.createRedirectURL(props, "http://www.localhost.at:9998");
		System.out.println(url);
		System.out.print("key: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String code = reader.readLine();
		props.setProperty("code", code);
		auth.postAuthorize(props);
		for (Entry<Object, Object> entry: props.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		props.store(new FileWriter(new File("auth.props")), null);
	}
}
