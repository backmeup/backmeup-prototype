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
    delete_user("ChangeUser2")
    delete_user("ChangeUser3")
    delete_user("ChangeUser4")
    delete_user("GetUser")
    delete_user("LoginUser")
    delete_user("LoginUser2")
    delete_user("PropUser")

  def test_register_user(self):
    logging.debug("================== register_user ======================")
    result = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertIn("verificationKey", result.data, result)
    result = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result) #already created
    result = register_user("TestUser", "pass", "password", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)
    result = register_user("TestUser", "password", "pass", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)

  def test_verify_email(self):
    logging.debug("================== verify_email =======================")
    res = verify_email("asdf")
    self.assertEquals(res.code, httplib.BAD_REQUEST, res)
    res = register_user("TestUser", "password2000", "password2000", "TestUser@trash-mail.com")
    res = verify_email(res.data["verificationKey"])
    self.assertEquals(res.code, httplib.OK, res)
    self.assertIn("username", res.data, res)
    self.assertEquals(res.data["username"], "TestUser", res)
    res = get_user("TestUser")
    self.assertEquals(res.code, httplib.OK, res)
    res = new_verification_email("TestUser")
    self.assertEquals(res.code, httplib.BAD_REQUEST, res)
    res = register_user("TestUser2", "password2000", "password2000", "TestUser2@trash-mail.com")
    res = new_verification_email("TestUser2")
    self.assertEquals(res.code, httplib.OK, res)
    self.assertIn("verificationKey", res.data, res)
    res = verify_email(res.data["verificationKey"])
    self.assertEquals(res.code, httplib.OK, res)
    self.assertIn("username", res.data, res)
    self.assertEquals(res.data["username"], "TestUser2", res)


  def test_delete_user(self):    
    logging.debug("================== delete_user ========================")
    register_user("DeleteTestUser", "password", "password", "TestUser@trash-mail.com")
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertIn("messages", result.data, result)
    self.assertIn("type", result.data, result)
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.NOT_FOUND, result) #user already deleted

  def test_change_user(self):
    logging.debug("================== change_user ========================")
    res = register_user("ChangeUser", "p1####++*%192?`", "abcdefgh", "TestUser@trash-mail.com")
    register_user("ChangeUser3", "p1####++*%192?`", "abcdefgh", "ChangeUser3@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = change_user("ChangeUser", "ChangeUser", "p1####++*%192?`", "pwneu123", "TestUser@trash-mail.com")

    self.assertEquals(result.code, httplib.OK, result)
    self.assertIn("messages", result.data, result)
    self.assertIn("type", result.data, result)
    result = change_user("ChangeUser", "ChangeUser", "p1####++*%192?`", "pwneu123", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.UNAUTHORIZED, result)
    result = change_user("ChangeUser", "ChangeUser", "pwneu123", "pwneu123", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.OK, result)
    result = change_user("ChangeUser", "ChangeUser", "pwneu123", "pwneu123", "emailmail.at")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)
    result = change_user("ChangeUser", "ChangeUser2", "pwneu123", "pwneu123", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.OK, result)
    result = get_user("ChangeUser2")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertEquals(result.data["username"], "ChangeUser2", result)
    self.assertEquals(result.data["email"], "TestUser@trash-mail.com", result)
    result = change_user("ChangeUser2", "ChangeUser", "pwneu123", "pwneu123", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.OK, result)
    # should already be in use
    result = change_user("ChangeUser", "ChangeUser3", "pwneu123", "pwneu123", "TestUser@trash-mail.com")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)
    self.assertIn("errorType", result.data, result)
    self.assertEquals("org.backmeup.model.exceptions.AlreadyRegisteredException", result.data["errorType"], result)
    # test changing the keyring
    res = register_user("ChangeUser4", "p1####++*%192?`", "abcdefgh", "ChangeUser4@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = change_user("ChangeUser4", "ChangeUser4", "p1####++*%192?`", "p1####++*%192?`",
        "ChangeUser4@trash-mail.com", "abcdefgh", "abcdefgh2")
    self.assertEquals(result.code, httplib.OK, result)
    result = change_user("ChangeUser4", "ChangeUser4", "p1####++*%192?`", "p1####++*%192?`",
        "ChangeUser4@trash-mail.com", "abcdefgh", "abcdefgh2")
    # has been changed to abcdefgh2 before! error
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)
    

  def test_get_user(self):
    logging.debug("================== get_user ========================")
    res = register_user("GetUser", "12345678", "12345678", "GetUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertEquals(result.data["username"], "GetUser", result)
    self.assertEquals(result.data["email"], "GetUser@trash-mail.com", result)
    change_user("GetUser", "GetUser", "12345678", "123456789101112", "GetUser@trash-mail.com")

    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertEquals(result.data["username"], "GetUser", result)
    self.assertEquals(result.data["email"], "GetUser@trash-mail.com", result)

  def test_login_user(self):
    logging.debug("================== login_user ========================")
    res = register_user("LoginUser", "abcdefgh", "abcdefgh", "LoginUser@trash-mail.com")    
    verify_email(res.data["verificationKey"])
    result = login_user("LoginUser", "abcdefgh")
    self.assertEquals(result.code, httplib.OK, result)
    result = login_user("LoginUser", "ab23")
    self.assertEquals(result.code, httplib.UNAUTHORIZED, result)
    res = register_user("LoginUser2", "abcdefgh", "abcdefgh", "LoginUser2@trash-mail.com")    
    result = login_user("LoginUser2", "abcdefgh")
    self.assertEquals(result.code, httplib.OK, result)

  def test_get_user_property(self):
    logging.debug("================== get_user_property ========================")
    result = get_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND, result)
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = get_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.BAD_REQUEST, result)
    set_user_property("PropUser", "Property", "Value")
    result = get_user_property("PropUser", "Property")
    self.assertEquals(result.code, httplib.OK, result)

  def test_set_user_property(self):
    logging.debug("================== set_user_property ========================")
    result = set_user_property("Unknwn", "Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND, result)
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = set_user_property("PropUser", "Unknwn", "Unknwn")    
    self.assertEquals(result.code, httplib.OK, result)
    self.assertIn("messages", result.data, result)
    self.assertIn("type", result.data, result)
    self.assertEquals(result.data["type"], "success", result)
    result = get_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertEquals("Unknwn", result.data, result)

  def test_delete_user_property(self):
    logging.debug("================== delete_user_property ========================")
    result = delete_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND, result)    
    res = register_user("PropUser", "abcdefgh", "abcdefgh", "PropUser@trash-mail.com")
    verify_email(res.data["verificationKey"]);
    result = set_user_property("PropUser", "Unknwn", "Unknwn")    
    result = delete_user_property("PropUser", "Unknwn")
    self.assertEquals(result.code, httplib.OK, result)
    self.assertIn("messages", result.data, result)
    self.assertIn("type", result.data, result)
    self.assertEquals("success", result.data["type"], result)
    result = delete_user_property("Unknwn", "Unknwn")
    self.assertEquals(result.code, httplib.NOT_FOUND, result)
    

