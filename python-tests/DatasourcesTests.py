# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
import httplib

import logging

logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestDatasources(TestCase):
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
  
  def test_get_datasources(self):
    res = get_datasources();
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("sources", res.data)
    sources = res.data["sources"]
    self.assertIsInstance(sources, list)
    for entry in sources:
      self.assertIn("imageURL", entry)
      self.assertIn("datasourceId", entry)
      self.assertIn("title", entry)

  def test_get_datasource_profiles(self):
    res = get_datasource_profiles("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals(len(res.data["sourceProfiles"]), 0)
    res = auth_datasource("TestUser", "org.backmeup.dropbox", "Dropbox", "key")
    logger.debug("After auth_datasource")
    logger.debug(str(res))
    #post_auth_datasource("TestUser", res.data["profileId"], "key", {})
    res = get_datasource_profiles("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals(len(res.data["sourceProfiles"]), 1)
    self.assertIn("title", res.data["sourceProfiles"][0])
    self.assertIn("datasourceProfileId", res.data["sourceProfiles"][0])

  def test_delete_datasource_profile(self):
    res = delete_datasource_profile("NotExistingUser", 12345)
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    res = delete_datasource_profile("TestUser2", 12345)
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    
    # perform auth only, delete before post
    res = auth_datasource("TestUser2", "org.backmeup.dropbox", "Dropbox", "key")
    logger.debug("After auth_datasource...")
    logger.debug(str(res))
    res = delete_datasource_profile("TestUser2", res.data["profileId"])
    self.assertEquals(res.code, httplib.NO_CONTENT)

    # perform auth + post + delete
    res = auth_datasource("TestUser2", "org.backmeup.dropbox", "Dropbox", "key")
    #post_auth_datasource("TestUser2", res.data["profileId"], "key", {})
    res = delete_datasource_profile("TestUser2", res.data["profileId"])
    self.assertEquals(res.code, httplib.NO_CONTENT)
    res = get_datasource_profiles("TestUser2")
    # make sure all profiles have been deleted
    self.assertEquals(len(res.data["sourceProfiles"]), 0)

  def test_generate_datasource_options(self):
    res = auth_datasource("TestUser3", "org.backmeup.dropbox", "Dropbox", "key")
    logger.debug("After auth_datasource")
    logger.debug(str(res))
    #post_auth_datasource("TestUser3", res.data["profileId"], "key", {})
    # note that is specific for user and plug-in
    res = generate_datasource_options("TestUser3", res.data["profileId"], "key")
    self.assertEquals(res.code, httplib.OK)
    # Note: this must implemented on the server side first
    self.assertIn("sourceOptions", res.data)
    self.assertTrue(len(res.data["sourceOptions"]) > 0)
    
  def test_change_datasource_profile(self):
    self.fail("Implement on server side!")

  def test_auth_datasource(self):
    # unknown user
    res = auth_datasource("NotExistingUser", "not-existing-plug-in", "any name will do", "t4")  
    self.assertEquals(res.code, httplib.BAD_REQUEST)    
    # unknown plug-in
    res = auth_datasource("TestUser4", "not-existing-plug-in", "any name will do", "t4")  
    self.assertEquals(res.code, httplib.BAD_REQUEST)    
    # wrong password
    res = auth_datasource("TestUser4", "org.backmeup.dropbox", "Dropbox", "jalsdf")
    self.assertEquals(res.code, httplib.UNAUTHORIZED)    
    res = auth_datasource("TestUser4", "org.backmeup.dropbox", "Dropbox", "t4")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("profileId", res.data)
    self.assertIn("type", res.data)
    if (res.data["type"] == "OAuth"):
      self.assertIn("redirectURL", res.data)
    elif (res.data["type"] == "Input"):
      self.assertIn("requiredInputs", res.data)








    
