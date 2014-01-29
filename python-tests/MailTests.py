# -*- coding: ISO-8859-1 -*-

from RESTBackMeUp import *
from unittest import TestCase
import httplib

HTML_TEST_MAIL = """
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
    "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>title</title>
  </head>
  <body>
    <div style="
        color: red;
            font-family: cursive;
            ">Hello World!</div>
  </body>
</html>
"""

class TestMails(TestCase):
  def setUp(self):
    pass

  def tearDown(self):
    pass

  def test_send_text_mail(self):
    res = send_text_mail("backmeup71@gmx.at", "Test-Text", "This is a very simple text message")
    print res.data
    self.assertEquals(res.code, httplib.NO_CONTENT)
    
  def test_send_html_mail(self):
    res = send_html_mail("backmeup71@gmx.at", "Test-Html", HTML_TEST_MAIL)
    print res.data
    self.assertEquals(res.code, httplib.NO_CONTENT)
  
