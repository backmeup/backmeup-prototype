package org.backmeup.discmailing.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.discmailing.DiscmailingAuthenticator;
import org.junit.Test;

public class DiscmailingAuthenticatorTest {
	
	@Test
	public void testDiscmailing() {
	    // fail("Not yet implemented");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		DiscmailingAuthenticator auth = new DiscmailingAuthenticator();
		Properties props = new Properties();
		do {
			props.clear();
//			for (String input : auth.getRequiredInputFields()) {
//				System.out.print("Enter " + input + ": ");
//				String entered = readLine();
//				switch (auth.getTypeMapping().get(input)) {
//					case Bool:
//						Boolean.parseBoolean(entered); break;
//					case Number:
//						Integer.parseInt(entered); break;  		  
//					default: break;
//				}
//				props.setProperty(input, URLEncoder.encode(entered, "UTF-8"));
//			}	
		}
		while (!auth.isValid(props));
		auth.postAuthorize(props);
		for (Entry<Object, Object> entry: props.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		props.store(new FileWriter(new File("/tmp/auth.props")), null);
	}
	
	public static String readLine() {
	    try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    	return reader.readLine();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return "";
	}

}
