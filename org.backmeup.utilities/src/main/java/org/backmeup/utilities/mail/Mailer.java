package org.backmeup.utilities.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mailer {
  private static final Logger logger = LoggerFactory.getLogger(Mailer.class);
  private static ExecutorService service;
  static {
    service = Executors.newFixedThreadPool(4);
  }
  
  public static void send(final String to, final String subject, final String text) {
    send(to, subject, text, "text/plain");
  }
  
  public static void synchronousSend(final String to, final String subject, final String text, final String mimeType) {
    executeSend(to, subject, text, mimeType);
  }
  
  private static void executeSend(final String to, final String subject, final String text, final String mimeType) {
    final Properties props = getMailSettings();
    try {      
      // Get session
      Session session = Session.getDefaultInstance(props, new Authenticator() {      
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(props.getProperty("mail.user"), props.getProperty("mail.password"));
        }
      });
      // Define message
      MimeMessage message = new MimeMessage(session);

      message.setFrom(new InternetAddress(props.getProperty("mail.from")));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setContent(text, mimeType);

      // Send message
      Transport.send(message);
    } catch (Exception e) {
      //TODO: Log exception
      throw new RuntimeException(e);
      //logger.error("", e);
    } 
  }
  
  public static void send(final String to, final String subject, final String text, final String mimeType) {    
    // Get system properties
    service.submit(new Runnable() {
      public void run() {
        executeSend(to, subject, text, mimeType);
      }
    });    
  }
  
  private static Properties mailSettings;
  
  private static Properties getMailSettings() {
    if (mailSettings == null) {
      Properties props = new Properties();
      InputStream is = null;
      try {
        is = Mailer.class.getClassLoader().getResourceAsStream("mail.properties");
        props.load(is);          
        mailSettings = props;
      } catch (Exception e) {
      } finally {
        if (is != null)
          try {
            is.close();
          } catch (IOException e) {
        	  logger.error("", e);
          }
      }
    }
    return mailSettings;
  }
}
