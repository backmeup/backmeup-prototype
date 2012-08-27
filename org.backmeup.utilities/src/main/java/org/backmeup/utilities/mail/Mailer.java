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

public class Mailer {
  private static ExecutorService service;
  static {
    service = Executors.newFixedThreadPool(4);
  }
  
  public static void send(final String to, final String subject, final String text) {    
    // Get system properties
    service.submit(new Runnable() {
      public void run() {
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
          message.setText(text);

          // Send message
          Transport.send(message);
        } catch (Exception e) {
          //TODO: Log exception
          //e.printStackTrace();
        } 
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
            e.printStackTrace();
          }
      }
    }
    return mailSettings;
  }
}
