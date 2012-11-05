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

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.plugin.spi.InputBased;

public class MailAuthenticator implements InputBased {
  private static final String PROP_SSL = "SSL";
  private static final String PROP_SSL_DESC = "Use SSL (encrypted connection)";
  private static final String PROP_PORT = "Port";
  private static final String PROP_PORT_DESC = "The port on which the plugin should connect to the E-Mail server";
  private static final String PROP_HOST = "Host";
  private static final String PROP_HOST_DESC = "The hostname of your E-Mail server";
  private static final String PROP_USERNAME = "Username";
  private static final String PROP_USERNAME_DESC = "The username of your E-Mail account";
  private static final String PROP_PASSWORD = "Password";
  private static final String PROP_PASSWORD_DESC = "The password of your E-Mail account";
  private static final String PROP_TYPE = "Type";
  private static final String PROP_TYPE_DESC = "IMAP or POP3";
  
  
  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.InputBased;
  }

  @Override
  public String postAuthorize(Properties inputProperties) {
    Properties newProps = convertInputPropertiesToAuthProperties(inputProperties);
    inputProperties.clear();
    inputProperties.putAll(newProps);
    return inputProperties.getProperty("mail.user");
  }

  @Override
  public List<RequiredInputField> getRequiredInputFields() {
    List<RequiredInputField> inputs = new ArrayList<RequiredInputField>();
    
    inputs.add(new RequiredInputField (PROP_USERNAME, PROP_USERNAME, PROP_USERNAME_DESC, true, 0, Type.String));
    inputs.add(new RequiredInputField (PROP_PASSWORD, PROP_PASSWORD, PROP_PASSWORD_DESC, true, 1, Type.Password));
    inputs.add(new RequiredInputField (PROP_TYPE, PROP_TYPE, PROP_TYPE_DESC, true, 2, Type.String));
    inputs.add(new RequiredInputField (PROP_HOST, PROP_HOST, PROP_HOST_DESC, true, 3, Type.String));
    inputs.add(new RequiredInputField (PROP_PORT, PROP_PORT, PROP_PORT_DESC, true, 4, Type.Number));
    inputs.add(new RequiredInputField (PROP_SSL, PROP_SSL, PROP_SSL_DESC, true, 5, Type.Bool));
    
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
      Session session = Session.getInstance(authProperties);
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
