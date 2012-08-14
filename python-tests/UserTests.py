# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
from urllib import quote
import httplib
import logging
import logging.config

logging.config.fileConfig('logging.conf')
logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestUsers(TestCase):
  def setUp(self):
    self.tearDown()

  def tearDown(self):
    delete_user("TestUser")
    delete_user("TestUser2")
    delete_user("DeleteTestUser")
    delete_user("ChangeUser")
    delete_user("GetUser")
    delete_user("LoginUser")
    delete_user("PropUser")

  def test_register_user(self):
    logging.debug("================== register_user ======================")
    result = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.OK)
    self.assertIn("verificationKey", result.data)
    result = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST) #already created
    result = register_user("TestUser", "pass", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    result = register_user("TestUser", "password", "pass", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST)

  def test_verify_email(self):
    logging.debug("================== verify_email =======================")
    res = verify_email("asdf")
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    res = register_user("TestUser", "password2000", "password2000", "TestUser@trash-mail.com")
    res = verify_email(res.data["verificationKey"])
    self.assertEquals(res.code, httplib.NO_CONTENT)
    res = get_user("TestUser")
    self.assertEquals(res.code, httplib.OK)
    res = new_verification_email("TestUser")
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    res = register_user("TestUser2", "password2000", "password2000", "TestUser2@trash-mail.com")
    res = new_verification_email("TestUser2")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("verificationKey", res.data)
    res = verify_email(res.data["verificationKey"])
    self.assertEquals(res.code, httplib.NO_CONTENT)


  def test_delete_user(self):    
    logging.debug("================== delete_user ========================")
    register_user("DeleteTestUser", "password", "password", "TestUser@trash-mail.com")
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.NO_CONTENT) #no content
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.NOT_FOUND) #user already deleted

  def test_change_user(self):
    logging.debug("================== change_user ========================")
    res = register_user("ChangeUser", "p1####++*%192?`", "abcdefgh", "TestUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = change_user("ChangeUser", "p1####++*%192?`", "pwneu123", "abcdefgh", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = change_user("ChangeUser", "p1####++*%192?`", "pwneu123", "abcdefgh", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.UNAUTHORIZED)
    result = change_user("ChangeUser", "pwneu123", "pwneu123", "abcdefgh", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = change_user("ChangeUser", "pwneu123", "pwneu123", "abcdefgh", "emailmail.at")
    self.assertEquals(result.code, httplib.BAD_REQUEST)

  def test_get_user(self):
    logging.debug("================== get_user ========================")
    res = register_user("GetUser", "12345678", "12345678", "GetUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK)
    self.assertEquals(result.data["username"], "GetUser")
    self.assertEquals(result.data["email"], "GetUser@trash-mail.com")
    change_user("GetUser", "12345678", "123456789101112", "12345678", "GetUser@trash-mail.com")
    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK)
    self.assertEquals(result.data["username"], "GetUser")
    self.assertEquals(result.data["email"], "GetUser@trash-mail.com")

  def test_login_user(self):
    logging.debug("================== login_user ========================")
    res = register_user("LoginUser", "abcdefgh", "abcdefgh", "LoginUser@trash-mail.com")    
    verify_email(res.data["verificationKey"])
    result = login_user("LoginUser", "abcdefgh")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = login_user("LoginUser", "ab23")
    self.assertEquals(result.code, httplib.UNAUTHORIZED)

  def test_get_user_property(self):
    logging.debug("================== get_user_property ========================")
    result = get_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND)
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = get_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    set_user_property("PropUser", "Property", "Value")
    result = get_user_property("PropUser", "Property")
    self.assertEquals(result.code, httplib.OK)

  def test_set_user_property(self):
    logging.debug("================== set_user_property ========================")
    result = set_user_property("Unknwn", "Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND)
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = set_user_property("PropUser", "Unknwn", "Unknwn")    
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = get_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.OK)

  def test_delete_user_property(self):
    logging.debug("================== delete_user_property ========================")
    result = delete_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND)    
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"]);
    result = set_user_property("PropUser", "Unknwn", "Unknwn")    
    result = delete_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = delete_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND)
    

