# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase

class TestBackupSearch(TestCase):
  def setUp(self):
    res = register_user("TestSearchUser", "password", "password", "TestSearchUser@trash-mail.com")
    if res.code == 200:
      verify_email(res.data["verificationKey"])

  def tearDown(self):
    pass

  def test_search(self):
    res = request_search_index("UnknownUser", "12345678", "*:*")
    self.assertEquals(res.code, 400)
    res = request_search_index("TestSearchUser", "wrongPassword", "*:*")
    self.assertEquals(res.code, 400)

    res = request_search_index("TestSearchUser", "password", "*:*")
    self.assertEquals(res.code, 202)
    self.assertIn("searchId", res.data)

  def test_query(self):
    res = query_index("UnknownUser", 1)
    self.assertEquals(res.code, 404)
    res = query_index("TestSearchUser", 5000000)
    self.assertEquals(res.code, 400)
    searchId = request_search_index("TestSearchUser", "password", "*:*").data["searchId"]
    res = query_index("TestSearchUser", searchId)
    self.assertEquals(res.code, 200)
    print res.data

