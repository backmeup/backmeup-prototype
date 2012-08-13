# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
import httplib

import logging

logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestDatasinks(TestCase):
  def setUp(self):
    res = delete_user("TestUser")
    logger.debug("Deleted TestUser: " + str(res.code))
    res = delete_user("TestUser2")
    logger.debug("Deleted TestUser2: " + str(res.code))
    res = delete_user("TestUser3")
    logger.debug("Deleted TestUser3: " + str(res.code))
    res = delete_user("TestUser4")
    logger.debug("Deleted TestUser4: " + str(res.code))
    delete_user("NotExistingUser")
    res = register_user("TestUser", "key", "key", "TestUser")
    logger.debug("Added TestUser: " + str(res.code))
    res = register_user("TestUser2", "key", "key", "TestUser2")
    logger.debug("Added TestUser2: " + str(res.code))
    res = register_user("TestUser3", "key", "key", "TestUser3")
    logger.debug("Added TestUser3: " + str(res.code))
    res = register_user("TestUser4", "t4", "t4", "TestUser4")
    logger.debug("Added TestUser4: " + str(res.code))
  
  def test_get_datasinks(self):
    res = get_datasinks();
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("sinks", res.data)
    sinks = res.data["sinks"]
    self.assertIsInstance(sinks, list)
    for entry in sinks:
      self.assertIn("imageURL", entry)
      self.assertIn("datasinkId", entry)
      self.assertIn("title", entry)

  def test_get_datasink_profiles(self):
    res = get_datasink_profiles("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals(len(res.data["sinkProfiles"]), 0)
    res = auth_datasink("TestUser", "org.backmeup.dropbox", "Dropbox", "key")
    logger.debug("After auth_datasink")
    logger.debug(str(res))
    #post_auth_datasink("TestUser", res.data["profileId"], "key", {})
    res = get_datasink_profiles("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals(len(res.data["sinkProfiles"]), 1)
    self.assertIn("title", res.data["sinkProfiles"][0])
    self.assertIn("datasinkProfileId", res.data["sinkProfiles"][0])

  def test_delete_datasink_profile(self):
    res = delete_datasink_profile("NotExistingUser", 12345)
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    res = delete_datasink_profile("TestUser2", 12345)
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    
    # perform auth only, delete before post
    res = auth_datasink("TestUser2", "org.backmeup.dropbox", "Dropbox", "key")
    logger.debug("After auth_datasink...")
    logger.debug(str(res))
    res = delete_datasink_profile("TestUser2", res.data["profileId"])
    self.assertEquals(res.code, httplib.NO_CONTENT)

    # perform auth + post + delete
    res = auth_datasink("TestUser2", "org.backmeup.dropbox", "Dropbox", "key")
    #post_auth_datasink("TestUser2", res.data["profileId"], "key", {})
    res = delete_datasink_profile("TestUser2", res.data["profileId"])
    self.assertEquals(res.code, httplib.NO_CONTENT)
    res = get_datasink_profiles("TestUser2")
    # make sure all profiles have been deleted
    self.assertEquals(len(res.data["sinkProfiles"]), 0)

  def test_change_datasink_profile(self):
    # Must be implemented on the server side aswell
    pass

  def test_auth_datasink(self):
    # unknown user
    res = auth_datasink("NotExistingUser", "not-existing-plug-in", "any name will do", "t4")  
    self.assertEquals(res.code, httplib.BAD_REQUEST)    
    # unknown plug-in
    res = auth_datasink("TestUser4", "not-existing-plug-in", "any name will do", "t4")  
    self.assertEquals(res.code, httplib.BAD_REQUEST)    
    # wrong password
    res = auth_datasink("TestUser4", "org.backmeup.dropbox", "Dropbox", "jalsdf")
    self.assertEquals(res.code, httplib.UNAUTHORIZED)    
    res = auth_datasink("TestUser4", "org.backmeup.dropbox", "Dropbox", "t4")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("profileId", res.data)
    self.assertIn("type", res.data)
    if (res.data["type"] == "OAuth"):
      self.assertIn("redirectURL", res.data)
    elif (res.data["type"] == "Input"):
      self.assertIn("requiredInputs", res.data)








    
