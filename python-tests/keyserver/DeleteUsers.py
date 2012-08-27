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
    for i in range(0, 500):
      com.request("DELETE", "/users/" + str(i))

  def test_delete_users(self):
    pass

