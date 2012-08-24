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
    res = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    result = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "password")
    profileId = result.data["profileId"]

    result2 = update_profile(profileId, 
        {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
    
    # get metadata for unknown user; should not work!
    result = get_specific_metadata("UnknownUser", profileId, "META_BACKUP_FREQUENCY", "password")
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    
    # get metadata for unknown profile; should not work!
    result = get_specific_metadata("UnknownUser", 99999, "META_BACKUP_FREQUENCY", "password")
    self.assertEquals(result.code, httplib.BAD_REQUEST)

    # get BACKUP_FREQUENCY
    result = get_specific_metadata("TestUser", profileId, "META_BACKUP_FREQUENCY", "password")
    self.assertIn("metadata", result.data)
    self.assertIn("META_BACKUP_FREQUENCY", result.data["metadata"])
    self.assertEquals(result.data["metadata"]["META_BACKUP_FREQUENCY"], METADATA_TEST_EXPECTED_FREQUENCY)

    # get metadata for known user; should work 
    result = get_specific_metadata("TestUser", profileId, METADATA_TEST_SPECIFIC_META, "password")
    self.assertEquals(result.code, httplib.OK)
    self.assertIn(METADATA_TEST_SPECIFIC_META, result.data["metadata"])

  def test_get_metadata(self):
    res = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    
    result = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "MetaTestProfile", "password")
    profileId = result.data["profileId"]

    result2 = update_profile(profileId, 
        {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
   
    # get metadata for unknown user; should not work!
    result = get_metadata("UnknownUser", profileId, "password")
    self.assertEquals(result.code, httplib.BAD_REQUEST)
    
    # get metadata for unknown profile; should not work!
    result = get_metadata("UnknownUser", 99999, "password")
    self.assertEquals(result.code, httplib.BAD_REQUEST)

    # get BACKUP_FREQUENCY
    result = get_metadata("TestUser", profileId, "password")
    self.assertIn("metadata", result.data)
    self.assertIn("META_BACKUP_FREQUENCY", result.data["metadata"])
    self.assertEquals(result.data["metadata"]["META_BACKUP_FREQUENCY"], METADATA_TEST_EXPECTED_FREQUENCY)

    # get metadata for known user; should work 
    result = get_metadata("TestUser", profileId, "password")
    self.assertEquals(result.code, httplib.OK)
    self.assertIn(METADATA_TEST_SPECIFIC_META, result.data["metadata"])

