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
    res = update_profile(100, {"A": "B"}, "p1")
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    
    # update existing profile
    register_user("TestUser", "p1", "p1", "TestUser")

    res = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "p1")
    print res.data
    profileId = res.data["profileId"]

    res = update_profile(profileId, {"AKey" : "AVal", "AnotherKey" : "AnotherValue", "B" : "A"}, "p1")
    self.assertEquals(res.code, httplib.NO_CONTENT)

