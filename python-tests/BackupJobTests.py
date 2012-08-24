# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase, skip
from TestConfig import *
from datetime import datetime, timedelta
import httplib

import logging
logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class TestBackupJobs(TestCase):
  def setUp(self):
    self.tearDown()

  def tearDown(self):
    delete_user("TestUser")
    delete_user("TestUser2")
    delete_user("TestUser3")

  def test_get_backup_jobs(self):
    logger.debug("============ test_get_backup_job ============")
    # test with wrong user
    res = get_backup_jobs("UnknownUser")
    self.assertEquals(res.code, httplib.NOT_FOUND)

    res = register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
    verify_email(res.data["verificationKey"])
    # get them from TestUser; no entry must be available
    res = get_backup_jobs("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("backupJobs", res.data)
    self.assertEquals(0, len(res.data["backupJobs"]))


    # now we add two jobs; they must be available
    # then
    sourceId = auth_datasource("TestUser", SOURCE_PLUGIN_ID, "SrcProfile", "password").data["profileId"]
    
    sinkId = auth_datasink("TestUser", SINK_PLUGIN_ID, "SinkProfile", "password").data["profileId"]

    update_profile(sourceId, {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "pass2")
    
    update_profile(sinkId, {KEY_SINK_TOKEN : SINK_TOKEN, KEY_SINK_SECRET : SINK_SECRET}, "pass2")

    create_backup_job("TestUser", "password", [sourceId], [], sinkId, "daily")    
    
    create_backup_job("TestUser", "password", [sourceId], [], sinkId, "daily")    

    res = get_backup_jobs("TestUser")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("backupJobs", res.data)
    self.assertEquals(2, len(res.data["backupJobs"]))
    for e in res.data["backupJobs"]:
      self.assertIn("datasourceIds", e)
      self.assertIn("datasinkId", e)
      self.assertIn("backupJobId", e)
    self.assertEquals(str(sinkId), str(res.data["backupJobs"][0]["datasinkId"]))

  def test_create_backup_job(self):
    # prepare the tests
    logger.debug("============ test_create_backup_job ============")

    res = register_user("TestUser2", "password", "password", "TestUser2@trash-mail.com")
    verify_email(res.data["verificationKey"])

    
    sourceId = auth_datasource("TestUser2", SOURCE_PLUGIN_ID, "SrcProfile", "password").data["profileId"]
    
    sinkId = auth_datasink("TestUser2", SINK_PLUGIN_ID, "SinkProfile", "password").data["profileId"]

    update_profile(sourceId, {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
    
    update_profile(sinkId, {KEY_SINK_TOKEN : SINK_TOKEN, KEY_SINK_SECRET : SINK_SECRET}, "password")

    # test with unknown user
    res = create_backup_job("UnknownUser", "asdf", [sourceId], [], sinkId, "weekly")
    self.assertEquals(res.code, httplib.NOT_FOUND)

    # test with wrong sourceProfiles
    res = create_backup_job("TestUser2", "password", [9999], [], sinkId, "weekly")
    self.assertEquals(res.code, httplib.BAD_REQUEST)

    # test with wrong keyring
    res = create_backup_job("TestUser2", "kr", [sourceId], [], sinkId, "weekly")
    self.assertEquals(res.code, httplib.UNAUTHORIZED)

    # test with wrong sinkProfile
    res = create_backup_job("TestUser2", "password", [sourceId], [], 9999, "weekly")
    self.assertEquals(res.code, httplib.BAD_REQUEST)

    # test with empty profiles
    res = create_backup_job("TestUser2", "password", [], [], sinkId, "weekly")
    self.assertEquals(res.code, httplib.BAD_REQUEST)
    
    # successfully create a job
    res = create_backup_job("TestUser2", "password", [sourceId], [], sinkId, "weekly")
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("jobId", res.data)

  def test_validate_backup_job(self):
    # prepare validation

    res = register_user("TestUser3", "password", "password", "email@trash-mail.com")
    verify_email(res.data["verificationKey"])
    sourceId = auth_datasource("TestUser3", SOURCE_PLUGIN_ID, "SrcProfile", "password").data["profileId"]
    
    sinkId = auth_datasink("TestUser3", SINK_PLUGIN_ID, "SinkProfile", "password").data["profileId"]

    update_profile(sourceId, {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
    
    update_profile(sinkId, {KEY_SINK_TOKEN : SINK_TOKEN, KEY_SINK_SECRET : SINK_SECRET}, "password")
    
    res = create_backup_job("TestUser3", "password", [sourceId], [], sinkId, "weekly")
    jobId = res.data["jobId"]

    # test bad things
    res = validate_backup_job("UnknwnUser", 1234, "password");
    self.assertEquals(res.code, httplib.NOT_FOUND); # unknown user
    
    res = validate_backup_job("TestUser3", 1234, "password");
    self.assertEquals(res.code, httplib.BAD_REQUEST); # invalid job

    res = validate_backup_job("TestUser3", jobId, "password"); # valid user + job
    self.assertEquals(res.code, httplib.OK);
    self.assertEquals(res.data["hasErrors"], False);
    # TODO: More complex test cases which analyze hasErrors = true


  def test_delete_backup_job(self):
    res = register_user("TestUser3", "password", "password", "TestUser3@trash-mail.com")
    verify_email(res.data["verificationKey"])
    sourceId = auth_datasource("TestUser3", SOURCE_PLUGIN_ID, "SrcProfile", "password").data["profileId"]
    
    sinkId = auth_datasink("TestUser3", SINK_PLUGIN_ID, "SinkProfile", "password").data["profileId"]

    update_profile(sourceId, {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
    
    update_profile(sinkId, {KEY_SINK_TOKEN : SINK_TOKEN, KEY_SINK_SECRET : SINK_SECRET}, "password")
    
    res = create_backup_job("TestUser3", "password", [sourceId], [], sinkId, "weekly")
    jobId = res.data["jobId"]
    
    res = delete_backup_job("UnknownUser", jobId); # delete a job of an unknown user
    self.assertEquals(res.code, httplib.NOT_FOUND);
    
    res = delete_backup_job("TestUser3", 9999); # delete an unknown job
    self.assertEquals(res.code, httplib.BAD_REQUEST);

    res = delete_backup_job("TestUser3", jobId);
    self.assertEquals(res.code, httplib.NO_CONTENT); # delete a valid job

  def test_get_status(self):
    res = register_user("TestUser3", "password", "password", "TestUser3@trash-mail.com")    
    verify_email(res.data["verificationKey"])
    sourceId = auth_datasource("TestUser3", SOURCE_PLUGIN_ID, "SrcProfile", "password").data["profileId"]
    
    sinkId = auth_datasink("TestUser3", SINK_PLUGIN_ID, "SinkProfile", "password").data["profileId"]

    update_profile(sourceId, {KEY_SOURCE_TOKEN : SOURCE_TOKEN, KEY_SOURCE_SECRET : SOURCE_SECRET}, "password")
    
    update_profile(sinkId, {KEY_SINK_TOKEN : SINK_TOKEN, KEY_SINK_SECRET : SINK_SECRET}, "password")
    
    res = create_backup_job("TestUser3", "password", [sourceId], [], sinkId, "weekly")
    jobId = res.data["jobId"]

    res = get_backup_job_status("UnknownUser", jobId) # invalid user
    self.assertEquals(res.code, httplib.NOT_FOUND)

    res = get_backup_job_status("TestUser3", 5000)
    self.assertEquals(res.code, httplib.BAD_REQUEST) # invalid job

    res = get_backup_job_status("TestUser3", jobId) # valid user + job
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("backupStatus", res.data)
    
    ts = timedelta(seconds=10)
    res = get_backup_job_status("TestUser3", jobId, datetime.now() - ts, datetime.now()) # valid user + job + time range
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("backupStatus", res.data)

    #TODO: Create a complex test with backup jobs, status messages +++
    

  @skip("Not yet implemented")
  def test_get_protocol_details(self):    
    self.fail("Implement tests + server-side")

  @skip("Not yet implemented")
  def test_get_protocol_overview(self):
    self.fail("Implement tests + server-side")

  @skip("Not yet implemented")
  def test_get_status_without_job_id(self):
    self.fail("Implement tests + server-side")

