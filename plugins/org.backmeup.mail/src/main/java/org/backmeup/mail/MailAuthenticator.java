package org.backmeup.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.backmeup.plugin.spi.InputBased;

public class MailAuthenticator implements InputBased {
  private static final String PROP_SSL = "SSL";
  private static final String PROP_PORT = "Port";
  private static final String PROP_HOST = "Host";
  private static final String PROP_USERNAME = "Username";
  private static final String PROP_PASSWORD = "Password";
  private static final String PROP_TYPE = "Type";
  
  
  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.InputBased;
  }

  @Override
  public void postAuthorize(Properties inputProperties) {
    Properties newProps = convertInputPropertiesToAuthProperties(inputProperties);
    inputProperties.clear();
    inputProperties.putAll(newProps);
  }

  @Override
  public List<String> getRequiredInputFields() {
    List<String> inputs = new ArrayList<String>();
    inputs.add(PROP_USERNAME);
    inputs.add(PROP_PASSWORD);
    inputs.add(PROP_TYPE);
    inputs.add(PROP_HOST);
    inputs.add(PROP_PORT);
    inputs.add(PROP_SSL);
    return inputs;
  }

  @Override
  public Map<String, Type> getTypeMapping() {
    Map<String, Type> typeMapping = new HashMap<String, Type>();
    typeMapping.put(PROP_SSL, Type.Bool);
    typeMapping.put(PROP_PORT, Type.Number);
    typeMapping.put(PROP_HOST, Type.String);
    typeMapping.put(PROP_USERNAME, Type.String);
    typeMapping.put(PROP_PASSWORD, Type.Password);
    //TODO: Add choices imap/pop3... also provide default values
    typeMapping.put(PROP_TYPE, Type.String); 
    return typeMapping;
  }
  
  private Properties convertInputPropertiesToAuthProperties(Properties inputs) {
    Properties authProperties = new Properties();
    String storeType = inputs.getProperty(PROP_TYPE);
    String prefix = "mail." + storeType + ".";
    if (inputs.get(PROP_SSL) != null && inputs.get(PROP_SSL).toString().equalsIgnoreCase("true")) {
      authProperties.put(prefix + "socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      authProperties.put(prefix + "socketFactory.fallback", "false");
    }
    authProperties.put(prefix + "port", inputs.getProperty(PROP_PORT));
    authProperties.put("mail.user", inputs.getProperty(PROP_USERNAME));
    authProperties.put("mail.password", inputs.getProperty(PROP_PASSWORD));
    authProperties.put("mail.host", inputs.getProperty(PROP_HOST));
    authProperties.put(prefix + "connectiontimeout", "5000");
    authProperties.put(prefix + "timeout", "5000");
    authProperties.put("mail.store.protocol", storeType);
    
    return authProperties;
  }

  @Override
  public boolean isValid(Properties inputs) {
    try {
      Properties authProperties = convertInputPropertiesToAuthProperties(inputs);    
      Session session = Session.getDefaultInstance(authProperties);
      Store store = session.getStore();
      store.connect(authProperties.getProperty("mail.host"),
          authProperties.getProperty("mail.user"),
          authProperties.getProperty("mail.password"));      
      store.close();
      return true;
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    }
    return false;
  }
}
