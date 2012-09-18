# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase, skip
import httplib

# TODO: Implement tests
class TestActions(TestCase):
  def setUp(self):
    pass

  def tearDown(self):
    pass

  def test_get_actions(self):
    res = get_actions()
    self.assertEquals(res.code, httplib.OK)
    self.assertIn("actions", res.data)
    acts = res.data["actions"]
    for action in acts:
      self.assertIn("title", action)
      self.assertIn("actionId", action)
      self.assertIn("description", action)
    
  @skip("Not yet implemented")
  def test_get_action_options(self):
    self.fail("Implement tests + server-side")
  
