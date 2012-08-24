from REST import Comm
from unittest import TestCase
from time import mktime
from datetime import datetime
import httplib

class DeleteUsers(TestCase):
  def setUp(self):
    self.tearDown()


  def tearDown(self):
    com = Comm()
    com.request("DELETE", "/users/1")
    com.request("DELETE", "/users/2")
    com.request("DELETE", "/users/3")
    com.request("DELETE", "/users/4")
    com.request("DELETE", "/users/5")
    com.request("DELETE", "/users/6")
    com.request("DELETE", "/users/7")
    com.request("DELETE", "/users/8")
    com.request("DELETE", "/users/9")
    com.request("DELETE", "/users/10")
    com.request("DELETE", "/users/105")

  def test_delete_users(self):
    pass

