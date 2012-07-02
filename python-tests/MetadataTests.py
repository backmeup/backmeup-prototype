# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
from TestConfig import *
import httplib

import logging

logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestMetadata(TestCase):
  def setUp(self):
    self.tearDown()

  def tearDown(self):
    delete_user("TestUser")

  def test_get_specific_metadata(self):
    register_user("TestUser", "pass", "pass1", "email@someone.at")
    result = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "pass1")
    profileId = result.data["profileId"]

    result2 = update_profile(profileId, 
        {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET})
    
    # get metadata for unknown user; should not work!
    result = get_specific_metadata("UnknownUser", profileId, "META_BACKUP_FREQUENCY")
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    
    # get metadata for unknown profile; should not work!
    result = get_specific_metadata("UnknownUser", 99999, "META_BACKUP_FREQUENCY")
    self.assertEquals(result.code, httplib.BAD_REQUEST)

    # get BACKUP_FREQUENCY
    result = get_specific_metadata("TestUser", profileId, "META_BACKUP_FREQUENCY")
    self.assertIn("metadata", result.data)
    self.assertIn("META_BACKUP_FREQUENCY", result.data["metadata"])
    self.assertEquals(result.data["metadata"]["META_BACKUP_FREQUENCY"], METADATA_TEST_EXPECTED_FREQUENCY)

    # get metadata for known user; should work 
    result = get_specific_metadata("TestUser", profileId, METADATA_TEST_SPECIFIC_META)
    self.assertEquals(result.code, httplib.OK)
    self.assertIn(METADATA_TEST_SPECIFIC_META, result.data["metadata"])

  def test_get_metadata(self):
    register_user("TestUser", "pass", "pass1", "email@someone.at")
    
    result = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "pass1")
    profileId = result.data["profileId"]

    result2 = update_profile(profileId, 
        {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET})
   
    # get metadata for unknown user; should not work!
    result = get_metadata("UnknownUser", profileId)
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    
    # get metadata for unknown profile; should not work!
    result = get_metadata("UnknownUser", 99999)
    self.assertEquals(result.code, httplib.BAD_REQUEST)

    # get BACKUP_FREQUENCY
    result = get_metadata("TestUser", profileId)
    self.assertIn("metadata", result.data)
    self.assertIn("META_BACKUP_FREQUENCY", result.data["metadata"])
    self.assertEquals(result.data["metadata"]["META_BACKUP_FREQUENCY"], METADATA_TEST_EXPECTED_FREQUENCY)

    # get metadata for known user; should work 
    result = get_metadata("TestUser", profileId)
    self.assertEquals(result.code, httplib.OK)
    self.assertIn(METADATA_TEST_SPECIFIC_META, result.data["metadata"])

