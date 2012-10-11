package org.backmeup.discmailing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class DiscmailingHelper {
	
	private String host;

	private String user;
	
	private String target;
	
	private String ticketpath;
	
	private int port;
	
	private String sshkey;
	
	public DiscmailingHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("discmailing.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: discmailing.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load discmailing.properties: " + e.getMessage());
		}
		
		host = properties.getProperty("remote.host");
		user = properties.getProperty("remote.user");
		target = properties.getProperty("remote.target");
		ticketpath = properties.getProperty("remote.ticketpath");
		port = Integer.parseInt(properties.getProperty("remote.port"));
		sshkey = properties.getProperty("ssh.key");
		
	}
	
	public static DiscmailingHelper getInstance() {
		return new DiscmailingHelper();
	}
	
	public Session getSshSession() {
		try {
			JSch jsch = new JSch();
	        jsch.addIdentity(sshkey);
	        Session session = jsch.getSession(user, host, port);
	        session.setConfig("StrictHostKeyChecking", "no");
	        return session;
		}
        catch (JSchException e) {
        	System.out.println("no connection");
        	return null;
        }
	}
	
	public ChannelSftp getSftpChannel(Session session) {
		try {
			session.connect();
	        Channel channel = session.openChannel("sftp");
	        channel.connect();
	        ChannelSftp sftpChannel =  (ChannelSftp) channel;
	        return sftpChannel;
		}
        catch (JSchException e) {
        	System.out.println("no connection");
        	return null;
        }
	}
	
	public InputStream generateTicket(Properties items, String path) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		    InputStream is = getClass().getClassLoader().getResourceAsStream("ticket.xml");
			doc = docBuilder.parse(is);
			
			Element el = (Element) doc.getElementsByTagName("values").item(0);
			el.appendChild(addValue(doc, "ticket_name", items.getProperty("Firstname") + " " + items.getProperty("Surname")));
			el.appendChild(addValue(doc, "firstname", items.getProperty("Firstname")));
			el.appendChild(addValue(doc, "surname", items.getProperty("Surname")));
			el.appendChild(addValue(doc, "street", items.getProperty("Street")));
			el.appendChild(addValue(doc, "city", items.getProperty("City")));
			el.appendChild(addValue(doc, "postcode", items.getProperty("Postcode")));
			
			el = (Element) doc.getElementsByTagName("mapping").item(0);
			el.appendChild(addElement(doc, "path", path));
			el.appendChild(addElement(doc, "destination", "/"));
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Source source = new DOMSource(doc);
			Result result = new StreamResult(outputStream);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
			transformer.transform(source, result);
			
			InputStream ticket = new ByteArrayInputStream(outputStream.toByteArray());
			return ticket;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Element addValue(Document doc, String value, String text) {
		Element e = doc.createElement("value");
		if (text == null) {
			text = "";
		}
		Text t = doc.createTextNode(text);
		e.appendChild(t);
		e.setAttribute("name", value);
		return e;
	}
	
	private Element addElement(Document doc, String tag, String text) {
		Element e = doc.createElement(tag);
		Text t = doc.createTextNode(text);
		e.appendChild(t);
		return e;
	}
	
	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public String getTarget() {
		return target;
	}

	public String getTicketpath() {
		return ticketpath;
	}

	public int getPort() {
		return port;
	}

	public String getSshkey() {
		return sshkey;
	}
}
