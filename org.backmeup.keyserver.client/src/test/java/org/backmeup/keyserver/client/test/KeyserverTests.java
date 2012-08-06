package org.backmeup.keyserver.client.test;

import java.util.Date;

import org.backmeup.keyserver.client.AuthData;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.keyserver.client.Token;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class KeyserverTests {
  public static void main(String[] args) {    
    Weld weld = new Weld();
    WeldContainer container = weld.initialize();
    Keyserver ks = container.instance().select(Keyserver.class).get();
    //ks.deleteUser(200L);
    //ks.deleteAuthInfo(202L);
    //ks.deleteAuthInfo(203L);
    //ks.deleteAuthInfo(204L);
    if (!ks.isUserRegistered(200L))
      ks.registerUser(200L, "apassword");
    
    if (!ks.validateUser(200L, "apassword")) {
    	throw new RuntimeException("Password missmatch!");
    }
    
    if (!ks.isServiceRegistered(201L))
      ks.addService(201L);
    if (!ks.isServiceRegistered(202L))
      ks.addService(202L);
    
    if (!ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 202L, "oauthtoken");
    
    if (!ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 202L, 203L, "oauthtoken");
       
    if (!ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 204L, "username", "password");
    
    
        
    Token t = ks.getToken(200L, "apassword", new Long[]{201L}, new Long[]{202L}, new Date().getTime());
    System.out.println("Token: " + t.getToken() + " / " + t.getBmu_token_id());
    AuthDataResult authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      System.out.println(ad.getAi_oauth());
    }
    
    t = ks.getToken(200L, "apassword", new Long[]{201L, 202L}, new Long[]{202L, 204L}, new Date().getTime());
    System.out.println("Token: " + t.getToken() + " / " + t.getBmu_token_id());
    authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      System.out.println(ad.getAi_oauth());
      System.out.println(ad.getAi_username());
      System.out.println(ad.getAi_password());
    }
    
    if (ks.isUserRegistered(200L))
      ks.deleteUser(200L);

    System.out.println("miao");
  }
}
