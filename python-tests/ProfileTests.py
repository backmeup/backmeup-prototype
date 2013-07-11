# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
from TestConfig import *
import httplib
import logging

logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestProfiles(TestCase):
  def setUp(self):
    self.tearDown() 

  def tearDown(self):
    delete_user("TestUser")

  def test_update_profile_entries(self):
    # update non existing profile
    res = update_profile(100, {"A": "B"}, "password")
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    
    # update existing profile
    res = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    verify_email(res.data["verificationKey"])

    res = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "password")
    print res.data
    profileId = res.data["profileId"]

    res = update_profile(profileId, {"AKey" : "AVal", "AnotherKey" : "AnotherValue", "B" : "A"}, "password")
    self.assertEquals(res.code, httplib.NO_CONTENT)

