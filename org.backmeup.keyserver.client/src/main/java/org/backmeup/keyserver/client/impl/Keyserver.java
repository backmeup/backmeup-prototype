package org.backmeup.keyserver.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.AuthUsrPwd;
import org.backmeup.keyserver.client.TokenRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Token;
import org.backmeup.model.exceptions.BackMeUpException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

@ApplicationScoped
public class Keyserver implements org.backmeup.keyserver.client.Keyserver {

  private static class Result {
    public HttpResponse response;
    public String content;
  }

  private enum ReqType {
    GET, DELETE, PUT, POST
  }

  @Inject
  @Named("keyserver.scheme")
  private String scheme;

  @Inject
  @Named("keyserver.host")
  private String host;

  @Inject
  @Named("keyserver.path")
  private String path;

  @Inject
  @Named("keyserver.keystore")
  private String keystore;

  @Inject
  @Named("keyserver.keystoreType")
  private String keystoreType;

  @Inject
  @Named("keyserver.keystorePwd")
  private String keystorePwd;

  @Inject
  @Named("keyserver.truststore")
  private String truststore;

  @Inject
  @Named("keyserver.truststoreType")
  private String truststoreType;

  @Inject
  @Named("keyserver.truststorePwd")
  private String truststorePwd;

  @Inject
  @Named("keyserver.allowAllHostnames")
  private boolean allowAllHostnames;

  public Keyserver() {

  }
  
  // use for http communication
  public Keyserver(String host, String path, boolean allowAllHostnames) {
    this("http", host, path, null, null, null, null, null, null, allowAllHostnames);    
  }
  
  // use for https communication
  public Keyserver(String host, String path, String keystore,
      String keystoreType, String keystorePwd, String truststore,
      String truststoreType, String truststorePwd, boolean allowAllHostnames) {
    this("https", host, path, keystore, keystoreType, keystorePwd, truststore, truststoreType, truststorePwd, allowAllHostnames);
  }

  private Keyserver(String scheme, String host, String path, String keystore,
      String keystoreType, String keystorePwd, String truststore,
      String truststoreType, String truststorePwd, boolean allowAllHostnames) {
    this.scheme = scheme;
    this.host = host;
    this.path = path;
    this.keystore = keystore;
    this.keystoreType = keystoreType;
    this.keystorePwd = keystorePwd;
    this.truststore = truststore;
    this.truststoreType = truststoreType;
    this.truststorePwd = truststorePwd;
    this.allowAllHostnames = allowAllHostnames;
  }



  private SchemeRegistry schemeRegistry;

  private DefaultHttpClient createClient() {
    if (scheme.equals("http")) {
      return new DefaultHttpClient();
    } else if (schemeRegistry == null) {
      try {
        KeyStore keystore = KeyStore
            .getInstance(keystoreType != null ? keystoreType : KeyStore
                .getDefaultType());
        InputStream keystoreInput = getClass().getClassLoader()
            .getResourceAsStream(this.keystore);
        if (keystoreInput == null)
          throw new BackMeUpException(String.format("bl.properties: has been configured to use keystore \"%s\", but the file does not exist. Please copy the keystore to the resources folder of the project.", this.keystore));
        keystore.load(keystoreInput,
            keystorePwd != null ? keystorePwd.toCharArray() : null);
        keystoreInput.close();
  //TODO: Don't throw nullpoint if one of the keystore files couldn't be loaded
        // load the truststore, leave it null to rely on cacerts distributed
        // with the JVM
        KeyStore truststore = null;
        if (this.truststore != null) {
          truststore = KeyStore
              .getInstance(truststoreType != null ? truststoreType : KeyStore
                  .getDefaultType());
          InputStream truststoreInput = getClass().getClassLoader()
              .getResourceAsStream(this.truststore);
          if (truststoreInput == null)
            throw new BackMeUpException(String.format("bl.properties: has been configured to use truststore \"%s\", but the file does not exist. Please copy the truststore to the resources folder of the project.", this.truststore));
          truststore.load(truststoreInput,
              truststorePwd != null ? truststorePwd.toCharArray() : null);
          truststoreInput.close();
        }
        schemeRegistry = new SchemeRegistry();
        SSLSocketFactory lSchemeSocketFactory = new SSLSocketFactory(
            SSLSocketFactory.TLS, keystore, keystorePwd, truststore,
            (SecureRandom) null,
            allowAllHostnames ? SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
                : SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        schemeRegistry.register(new Scheme("https", 443, lSchemeSocketFactory));
      } catch (Exception e) {
        throw new BackMeUpException(e);
      }
    }
    final HttpParams httpParams = new BasicHttpParams();
    return new DefaultHttpClient(new SingleClientConnManager(schemeRegistry),
        httpParams);
  }

  private Result execute(String path, ReqType type) {
    return execute(path, type, null);
  }

  private Result execute(String path, ReqType type, String jsonParams) {

    HttpClient client = createClient();
    int port = "http".equals(scheme) ? 80 : 443;
    String rHost = host;
    if (host.contains(":")) {
      String[] sp = host.split(":");
      rHost = sp[0];
      try {
        port = Integer.parseInt(sp[1]);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    try {
      URI registerUri = new URI(scheme, null, rHost, port, path, null, null);
      HttpUriRequest request;
      switch (type) {
      case PUT:
        request = new HttpPut(registerUri);
        break;
      case DELETE:
        request = new HttpDelete(registerUri);
        break;
      case GET:
        request = new HttpGet(registerUri);
        break;
      default:
        HttpPost post;
        request = post = new HttpPost(registerUri);
        if (jsonParams != null) {
          StringEntity entity = new StringEntity(jsonParams, "UTF-8");
          BasicHeader header = new BasicHeader(HTTP.CONTENT_TYPE,
              "application/json");
          entity.setContentType(header);
          post.setEntity(entity);
        }
        break;
      }
      HttpResponse response = client.execute(request);
      Result r = new Result();
      r.response = response;
      if (response.getEntity() != null) {
        try {
          r.content = new Scanner(response.getEntity().getContent())
              .useDelimiter("\\A").next();
        } catch (NoSuchElementException nee) {
        }
      }
      return r;
    } catch (URISyntaxException e) {
      throw new BackMeUpException(e);
    } catch (ClientProtocolException e) {
      throw new BackMeUpException(e);
    } catch (IOException e) {
      throw new BackMeUpException(e);
    }
  }

  // User Operations
  @Override
  public void registerUser(Long userId, String password) {
    
      Result response = execute(path + "/users/" + userId + "/" + password
          + "/register", ReqType.POST);
      if (response.response.getStatusLine().getStatusCode() != 204) {
        throw new BackMeUpException("Error during user creation: error code was "
            + response.response.getStatusLine().getStatusCode() + "; message: "
            + response.response.getStatusLine().getReasonPhrase() + "; Data: "
            + response.content);
      }   
  }

  @Override
  public void deleteUser(Long userId) {
    Result response = execute(path + "/users/" + userId, ReqType.DELETE);
    if (response.response.getStatusLine().getStatusCode() != 204) {
      throw new BackMeUpException("Failed to delete user " + userId + " code: "
          + response.response.getStatusLine().getStatusCode() + "; message: "
          + response.response.getStatusLine().getReasonPhrase());
    }
  }

  @Override
  public boolean isUserRegistered(Long userId) {
    Result response = execute(path + "/users/" + userId, ReqType.GET);
    return response.response.getStatusLine().getStatusCode() == 200;
  }

  @Override
  public boolean validateUser(Long userId, String password) {
    Result response;
      response = execute(
          path + "/users/" + userId + "/"
              + password + "/validate", ReqType.GET);
      return response.response.getStatusLine().getStatusCode() == 204;
  }
  
  @Override
  public void changeUserPassword(Long userId, String oldPassword,
      String newPassword) {        
      Result response = execute(
            path + "/users/" + userId + "/"
                + oldPassword + "/" + newPassword + "/changeuserpwd", ReqType.GET);
      if (response.response.getStatusLine().getStatusCode() != 204)    
        throw new BackMeUpException(response.content);
  }
  
	@Override
	public void changeUserKeyRing(Long userId, String oldKeyRing,
			String newKeyRing) {
		// TODO Auto-generated method stub
	  Result response = execute(
        path + "/users/" + userId + "/"
            + oldKeyRing + "/" + newKeyRing + "/changeuserkeyringpwd", ReqType.GET);
  if (response.response.getStatusLine().getStatusCode() != 204)    
    throw new BackMeUpException(response.content);
  }


  // Service Operations
  @Override
  public void addService(Long serviceId) {
    Result response = execute(path + "/services/" + serviceId + "/register",
        ReqType.POST);
    if (response.response.getStatusLine().getStatusCode() != 204) {
      throw new BackMeUpException("Failed to add service " + serviceId
          + "; Data: " + response.content);
    }
  }

  @Override
  public boolean isServiceRegistered(Long serviceId) {
    Result response = execute(path + "/services/" + serviceId, ReqType.GET);
    return response.response.getStatusLine().getStatusCode() == 200;
  }

  @Override
  public void deleteService(Long serviceId) {
    Result response = execute(path + "/services/" + serviceId, ReqType.DELETE);
    if (response.response.getStatusLine().getStatusCode() != 204) {
      throw new BackMeUpException("Failed to delete service " + serviceId
          + "; Data: " + response.content);
    }
  }

  private class PropertiesSerializer implements JsonSerializer<Properties> {
    @Override
    public JsonElement serialize(Properties src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject parent = new JsonObject();
      for (Entry<Object, Object> entry : src.entrySet()) {
        parent.add((String) entry.getKey(),
            new JsonPrimitive((String) entry.getValue()));
      }
      return parent;
    }
  }

  // Authentication Operations
  @Override
  public void addAuthInfo(Long userId, String userPwd, Long serviceId,
      Long authInfoId, Properties keyValuePairs) {
    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(Properties.class, new PropertiesSerializer());
    Gson g = gb.create();
    String json = g.toJson(new AuthUsrPwd(userId, userPwd, serviceId,
        authInfoId, keyValuePairs));
    Result response = execute(path + "/authinfos/add", ReqType.POST, json);
    if (response.response.getStatusLine().getStatusCode() != 204) {
      throw new BackMeUpException("Failed to addAuthInfo! Data: "
          + response.content);
    }
  }

  @Override
  public boolean isAuthInformationAvailable(Long authInfoId, Long userId,
      Long serviceId, String userPwd) {
    // TODO: Determine if query parameters will be encrypted with SSL
    
      Result r = execute(path + "/authinfos/" + authInfoId + "/" + userId + "/"
          + serviceId + "/" + userPwd, ReqType.GET);
      return r.response.getStatusLine().getStatusCode() == 200;
  }

  @Override
  public void deleteAuthInfo(Long authInfoId) {
    Result r = execute(path + "/authinfos/" + authInfoId, ReqType.DELETE);
    if (r.response.getStatusLine().getStatusCode() != 204)
      throw new BackMeUpException("Failed to delete authinfo " + authInfoId
          + ". Data: " + r.content);
  }

  @Override
  public Token getToken(Long userId, String userPwd, Long[] services,
      Long[] authinfos, Long backupdate, boolean reusable, String encryptionPwd) {
    Gson g = new Gson();
    String json = g.toJson(new TokenRequest(userId, userPwd, services,
        authinfos, backupdate, reusable, encryptionPwd));
    //System.out.println("REQUESTING: " + json);
    Result r = execute(path + "/tokens/token", ReqType.POST, json);
    if (r.response.getStatusLine().getStatusCode() == 200) {
      return g.fromJson(r.content, Token.class);
    }
    throw new BackMeUpException("Failed to retrieve a token: " + r.content);
  }

  @Override
  public AuthDataResult getData(Token token) {
    Gson g = new Gson();
    String json = g.toJson(token);
    Result r = execute(path + "/tokens/data", ReqType.POST, json);
    //System.out.println("REQUESTING: " + json);
    if (r.response.getStatusLine().getStatusCode() == 200) {
      return g.fromJson(r.content, AuthDataResult.class);
    }
    throw new BackMeUpException("Failed to retrieve token data: " + r.content);
  }

  @Override
  public void addAuthInfo(Profile profile, String userPwd,
      Properties keyValuePairs) {
    addAuthInfo(profile.getUser().getUserId(), userPwd, profile.getProfileId(),
        profile.getProfileId(), keyValuePairs);
  }

  @Override
  public boolean isAuthInformationAvailable(Profile profile, String userPwd) {
    return isAuthInformationAvailable(profile.getProfileId(), profile.getUser()
        .getUserId(), profile.getProfileId(), userPwd);
  }

  @Override
  public Token getToken(Profile profile, String userPwd, Long backupdate,
      boolean reusable, String encryptionPwd) {
    return getToken(profile.getUser().getUserId(), userPwd,
        new Long[] { profile.getProfileId() },
        new Long[] { profile.getProfileId() }, backupdate, reusable, encryptionPwd);
  }

  @Override
  public Token getToken(BackupJob job, String userPwd, Long backupdate, boolean reusable, String encryptionPwd) {
    List<Long> usedServices = new ArrayList<Long>();
    List<Long> authenticationInfos = new ArrayList<Long>();
    usedServices.add(job.getSinkProfile().getProfileId());
    authenticationInfos.add(job.getSinkProfile().getProfileId());
    for (ProfileOptions p : job.getSourceProfiles()) {
      usedServices.add(p.getProfile().getProfileId());
      authenticationInfos.add(p.getProfile().getProfileId());
    }
    Long[] serviceIds = usedServices.toArray(new Long[]{});
    Long[] authIds = authenticationInfos.toArray(new Long[]{});   
    Token t = getToken(job.getUser().getUserId(), userPwd, serviceIds, authIds, new Date().getTime(), reusable, encryptionPwd);
    return t;
  }

	@Override
	public List<KeyserverLog> getLogs (BackMeUpUser user)
	{
		Result response = execute (path + "/logs/" + user.getUserId (), ReqType.GET);

		if (response.response.getStatusLine ().getStatusCode () == 200)
		{
			Gson g = new Gson ();
			Type listType = new TypeToken<ArrayList<KeyserverLog>> ()
			{
			}.getType ();
			
			List<KeyserverLog> klogs = g.fromJson (response.content, listType);
			
			return klogs;
		}

		throw new BackMeUpException ("Failed to retrieve logs: " + response.content);
	}
}
