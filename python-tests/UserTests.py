# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
import httplib

class TestUsers(TestCase):
  def setUp(self):
    self.tearDown()

  def tearDown(self):
    delete_user("TestUser")
    delete_user("DeleteTestUser")
    delete_user("ChangeUser")
    delete_user("GetUser")
    delete_user("LoginUser")

  def test_register_user(self):
    result = register_user("TestUser", "password", "keyRing", "email")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = register_user("TestUser", "password", "keyRing", "email")
    self.assertEquals(result.code, httplib.BAD_REQUEST) #already created

  def test_delete_user(self):    
    register_user("DeleteTestUser", "password", "keyRing", "email")
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.NO_CONTENT) #no content
    result = delete_user("DeleteTestUser")
    self.assertEquals(result.code, httplib.BAD_REQUEST) #user already deleted

  def test_change_user(self):
    register_user("ChangeUser", "p1####++*%192?`", "abc", "email")
    result = change_user("ChangeUser", "p1####++*%192?`", "pwneu", "abc", "abc")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = change_user("ChangeUser", "p1####++*%192?`", "pwneu", "abc", "abc")
    self.assertEquals(result.code, httplib.UNAUTHORIZED)
    result = change_user("ChangeUser", "pwneu", "pwneu", "abc", "abc")
    self.assertEquals(result.code, httplib.NO_CONTENT)

  def test_get_user(self):
    register_user("GetUser", "p", "p", "p")
    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK)
    self.assertEquals(result.data["username"], "GetUser")
    self.assertEquals(result.data["email"], "p")
    change_user("GetUser", "p", "p1", "p1", "p1")
    result = get_user("GetUser")
    self.assertEquals(result.code, httplib.OK)
    self.assertEquals(result.data["username"], "GetUser")
    self.assertEquals(result.data["email"], "p1")

  def test_login_user(self):
    register_user("LoginUser", "abc", "abc", "123")
    result = login_user("LoginUser", "abc")
    self.assertEquals(result.code, httplib.NO_CONTENT)
    result = login_user("LoginUser", "ab23")
    self.assertEquals(result.code, httplib.UNAUTHORIZED)



