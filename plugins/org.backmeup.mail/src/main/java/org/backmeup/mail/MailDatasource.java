package org.backmeup.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * The DropboxDatasource is capable of listing all directories and files of a
 * certain directory and of downloading certain files from Dropbox.
 * 
 * @author fschoeppl
 */
public class MailDatasource implements Datasource {
  private static final String MAIL = "mail";
  private static final SimpleDateFormat sdf = new SimpleDateFormat(
      "yyyy/MM/dd/HH_mm");
  private static final String DEFAULT_CONTENT_TYPE = "TEXT/PLAIN";
  private static final String DEFAULT_ENCODING = "UTF-8";

  @Override
  public String getStatistics(Properties items) {
    return null;
  }

  private String getCharset(Part bp) throws MessagingException {
    Pattern pattern = Pattern.compile(".*?charset=(.*?)$");
    Matcher matcher = pattern.matcher(bp.getContentType());
    if (matcher.find()) {
      String result = matcher.group(1).toUpperCase();
      if (result.contains(";"))
        result = result.split(";")[0];
      return result;
    }
    return "UTF-8";
  }
  
  private static class Attachment {
    public InputStream stream;
    public String filename;
  }

  private static class TextContent {
    public String text;
    public boolean isHtml;
    public String charset;
  }

  private TextContent getText(Part part) throws MessagingException, IOException {
    String charset = getCharset(part);
    if (part.isMimeType("text/*")) {
      Object o = part.getContent();
      TextContent tc = new TextContent();
      if (o instanceof String) {        
        tc.isHtml = part.isMimeType("text/html");
        tc.charset = charset;
        tc.text = (String) o;
        return tc;
      } else if (o instanceof InputStream) {
        InputStream i = (InputStream) o;
        try {          
          tc.text = new Scanner(i, getCharset(part)).useDelimiter("\\A").next();
          tc.charset = charset;
          tc.isHtml = part.isMimeType("text/html");
          return tc;
        } catch (NoSuchElementException nee) {
          return null;
        }
      }      
    } else if (part.isMimeType("multipart/alternative")) {
      Multipart mp = (Multipart) part.getContent();
      TextContent text = new TextContent();
      for (int i = 0; i < mp.getCount(); i++) {
        Part bp = mp.getBodyPart(i);
        if (bp.isMimeType("text/plain")) {
          if (text == null)
            text = getText(bp);
          continue;
        } else if (bp.isMimeType("text/html")) {
          TextContent s = getText(bp);
          if (s != null)
            return s;
        } else {
          return getText(bp);
        }
      }
      return text;
    } else if (part.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) part.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        TextContent s = getText(mp.getBodyPart(i));
        if (s != null)
          return s;
      }
    } 
    return null;
  }

  public List<Attachment> getAttachments(Part part) throws MessagingException,
      IOException {
    List<Attachment> attachments = new ArrayList<Attachment>();
    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
      Attachment a = new Attachment();
      a.filename = MimeUtility.decodeText(part.getFileName());
      a.stream = part.getInputStream();
      attachments.add(a);
    } else if (part.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) part.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        attachments.addAll(getAttachments(mp.getBodyPart(i)));
      }
    }
    return attachments;
  }

  public List<Part> getNestedMessages(Part p) throws MessagingException,
      IOException {
    List<Part> nested = new ArrayList<Part>();
    if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      int count = mp.getCount();
      for (int i = 0; i < count; i++)
        nested.addAll(getNestedMessages(mp.getBodyPart(i)));
    } else if (p.isMimeType("message/rfc822")) {
      nested.add(p);
    }
    return nested;
  }   
  
  private void handlePart(Part m, String folderName, Storage storage, Set<String> alreadyInspected) throws StorageException, MessagingException, IOException {
    if (alreadyInspected.contains(folderName + "content.html"))
      return;
    
    StringBuilder messageDetails = new StringBuilder("<div class=\"bmu-message-details\">");
    if (m instanceof Message) {
      Message mesg = (Message)m;
      if (mesg.getFrom() != null)
        for (Address a : mesg.getFrom()) 
          messageDetails.append("<p>From: ").append(a.toString()).append("</p>");
      if (mesg.getRecipients(Message.RecipientType.TO) != null)
        for (Address a : mesg.getRecipients(Message.RecipientType.TO)) 
          messageDetails.append("<p>To: ").append(a.toString()).append("</p>");
      messageDetails.append("<p>Subject: ").append(mesg.getSubject()).append("</p>");
      if (mesg.getSentDate() != null)
        messageDetails.append("<p>Sent at: ").append(mesg.getSentDate()).append("</p>");
      if (mesg.getReceivedDate() != null)
        messageDetails.append("<p>Received at: ").append(mesg.getReceivedDate()).append("</p>");
    }
    messageDetails.append("</div>");
    
    TextContent text = getText(m);
    // nothing to do; message is empty
    if (text == null)
      return;
    
    if (!text.isHtml) {
      text.text = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n"
          + "  \"http://www.w3.org/TR/html4/strict.dtd\">"
          + "<html>"
          + "  <head>"
          + "    <meta http-equiv=\"content-type\" content=\"text/html; charset="
          + text.charset + "\"</meta>" + "  </head>" + "  <body>"          
          + "    <p>" + text.text.replace("\r\n", "<br/>").replace("\n", "<br/>").replace("\r", "<br/>") + "    <p>"
          + "  </body>" + "</html>";
    }

    StringBuilder htmlText = new StringBuilder(text.text);
    int ind = htmlText.indexOf("<body>");
    if (ind != -1) {
      htmlText.insert(ind, messageDetails.toString());
    } else {
      htmlText.insert(0, messageDetails.toString());
    }

    List<Attachment> attachments = getAttachments(m);
    StringBuilder attachmentLinks = new StringBuilder(
        "<div class=\"bmu-attachments\">");
    for (Attachment a : attachments) {
      storage.addFile(a.stream, folderName + "attachments/" + a.filename, new MetainfoContainer());
      attachmentLinks
          .append("<a class=\"bmu-attachment\" href=\"./attachments/"
              + a.filename + "\">" + a.filename + "</a>");
    }
    attachmentLinks.append("</div>");
    if (attachments.size() > 0) {
      ind = htmlText.indexOf("</body>");
      if (ind == -1)
        htmlText.append(attachmentLinks);
      else
        htmlText.insert(ind, attachmentLinks);
    }
    alreadyInspected.add(folderName + "content.html");
    storage
        .addFile(
            new ByteArrayInputStream(htmlText.toString().getBytes(
                text.charset)), folderName + "content.html", new MetainfoContainer());
    
    List<Part> nested = getNestedMessages(m);
    for (int i=0; i < nested.size(); i++) {
      handlePart(nested.get(i), folderName + "/nested/" + i + "/", storage, alreadyInspected);
    }
  }

  private void handleFolder(Folder folder, Storage storage, Set<String> alreadyInspected)
      throws IOException, MessagingException, StorageException {
    try {
      folder.open(Folder.READ_ONLY);
      
      Message[] messages = folder.getMessages();
      System.out.println("Folder: " + folder.getFullName());
      double prev = 0;
      for (int i=0; i < messages.length; i++) {      
        String folderName = folder.getFullName() + "/"
            + sdf.format(messages[i].getReceivedDate()) + "/" + messages[i].getMessageNumber()
            + "/";
        
        handlePart(messages[i], folderName, storage, alreadyInspected);
        double percent = i * 100 / (double)messages.length;
        if (percent - 10 > prev) {          
          System.out.format("%3.2f%%\n", percent);
          prev = percent;
        }
      }
      folder.close(false);
    } catch (MessagingException me) {
      me.printStackTrace();
    }
  }

  public void handleDownloadAll(Folder current, Properties accessData,
      Storage storage, Set<String> alreadyInspected) throws IOException, MessagingException,
      StorageException {
    if (alreadyInspected.contains(current.getFullName()))
      return;
          
    handleFolder(current, storage, alreadyInspected);
    alreadyInspected.add(current.getFullName());

    Folder[] subFolders = current.list("*");
    for (Folder sub : subFolders) {
      handleDownloadAll(sub, accessData, storage, alreadyInspected);
    }
  }

  @Override
  public void downloadAll(Properties accessData, Storage storage,
      Progressable progressor) throws DatasourceException, StorageException {
    try {
      Session session = Session.getInstance(accessData);
      Store store = session.getStore();
      store.connect(accessData.getProperty("mail.host"),
          accessData.getProperty("mail.user"),
          accessData.getProperty("mail.password"));
      Set<String> alreadyInspected = new HashSet<String>();
      Folder[] folders = store.getDefaultFolder().list("*");
      for (Folder folder : folders) {
        handleDownloadAll(folder, accessData, storage, alreadyInspected);
      }
      store.close();
    } catch (NoSuchProviderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public List<String> getAvailableOptions(Properties accessData) {
    List<String> availOpts = new ArrayList<String>();
    try {
      Session session = Session.getInstance(accessData);
      Store store = session.getStore();
      store.connect(accessData.getProperty("mail.host"),
          accessData.getProperty("mail.user"),
          accessData.getProperty("mail.password"));
      Folder[] folders = store.getDefaultFolder().list("*");
      for (Folder folder : folders) {
        availOpts.add(folder.getName());
      }
      store.close();
    } catch (NoSuchProviderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return availOpts;
  }
}
