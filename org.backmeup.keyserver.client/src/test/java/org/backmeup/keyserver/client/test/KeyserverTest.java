package org.backmeup.keyserver.client.test;

import java.util.Date;
import java.util.Properties;

import org.backmeup.keyserver.client.AuthData;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.keyserver.client.Token;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeyserverTest {
  private static Keyserver ks;

  @BeforeClass
  public static void setUp() {
    try {
      /*
       * ApplicationContext context = new ClassPathXmlApplicationContext( new
       * String[] { "spring.xml" }); logic =
       * context.getBean(BusinessLogic.class);
       */
      Weld weld = new Weld();
      WeldContainer container = weld.initialize();
      ks = container.instance().select(Keyserver.class).get();

    } catch (Throwable e) {
      do {
        e.printStackTrace();
        e = e.getCause();
      } while (e.getCause() != e && e.getCause() != null);
    }
  }

  @Test
  public void testRegisterUser() {
    if (!ks.isUserRegistered(200L))
      ks.registerUser(200L, "apassword");
    
    assert ks.validateUser(200L, "apassword");    
  }
  
  @Test
  public void testRegisterService() {   
    if (!ks.isServiceRegistered(201L))
      ks.addService(201L);
    assert ks.isServiceRegistered(201L);
    if (!ks.isServiceRegistered(202L))
      ks.addService(202L);
    assert ks.isServiceRegistered(202L);
  }
  
  @Test
  public void testAddAuthInfo() {
    
    Properties p = new Properties();
    p.setProperty("oauthtoken", "asdfasdf");
    p.setProperty("oauthpassword", "asdfasdf2");
    
    Properties p2 = new Properties();
    p2.setProperty("mailaddress", "something@somewhere.at");
    p2.setProperty("kind", "smtp");
    
    Properties p3 = new Properties();
    p3.setProperty("moredata", "as-dfadjsfasldf#++*\"");
    p3.setProperty("yep", "tests");
    
    if (ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword")) {
      ks.deleteAuthInfo(202L);
    }
        
    if (ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword")) {
      ks.deleteAuthInfo(203L);
    }
       
    if (ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword")) {
      ks.deleteAuthInfo(204L);
    }
    
    if (!ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 202L, p);
    
    if (!ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 202L, 203L, p2);
       
    if (!ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 204L, p3);
    
    assert ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword");
    assert ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword");
    assert ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword");
    
  }
    
  @Test
  public void testGetToken() {
    // add authentication data first
    if (!ks.isUserRegistered(200L))
      ks.registerUser(200L, "apassword");
    
    if (!ks.isServiceRegistered(201L))
      ks.addService(201L);
    if (!ks.isServiceRegistered(202L))
      ks.addService(202L);
    
    Properties p = new Properties();
    p.setProperty("oauthtoken", "asdfasdf");
    p.setProperty("oauthpassword", "asdfasdf2");
    
    Properties p2 = new Properties();
    p2.setProperty("mailaddress", "something@somewhere.at");
    p2.setProperty("kind", "smtp");
    
    Properties p3 = new Properties();
    p3.setProperty("moredata", "as-dfadjsfasldf#++*\"");
    p3.setProperty("yep", "tests");
    
    if (!ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 202L, p);
    
    if (!ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 202L, 203L, p2);
       
    if (!ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 204L, p3);
    
    // get a token and test 
    Token t = ks.getToken(200L, "apassword", new Long[]{201L}, new Long[]{202L}, new Date().getTime());
    System.out.println("Token: " + t.getToken() + " / " + t.getBmu_token_id());
    assert t.getToken() != null;
    assert t.getBmu_token_id() != null;
    AuthDataResult authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      assert ad.getAi_data().containsKey("oauthtoken");
      assert ad.getAi_data().get("oauthtoken").equals("asdfasdf");
      assert ad.getAi_data().containsKey("oauthpassword");
      assert ad.getAi_data().get("oauthpassword").equals("asdfasdf2");      
    }
    
    t = ks.getToken(200L, "apassword", new Long[]{201L, 202L}, new Long[]{202L, 204L}, new Date().getTime());
    System.out.println("Token: " + t.getToken() + " / " + t.getBmu_token_id());
    authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      if (ad.getAi_data().containsKey("oauthtoken")) {
        assert ad.getAi_data().containsKey("oauthtoken");
        assert ad.getAi_data().get("oauthtoken").equals("asdfasdf");
        assert ad.getAi_data().containsKey("oauthpassword");
        assert ad.getAi_data().get("oauthpassword").equals("asdfasdf2");
      } else {
        assert ad.getAi_data().containsKey("moredata");
        assert ad.getAi_data().get("moredata").equals("as-dfadjsfasldf#++*\"");
        assert ad.getAi_data().containsKey("yep");
        assert ad.getAi_data().get("yep").equals("tests");
      }
    }
    
    if (ks.isUserRegistered(200L))
      ks.deleteUser(200L);
  }
}
