from REST import Comm
from unittest import TestCase
from time import mktime
from datetime import datetime
import httplib

class Test(TestCase):
  def setUp(self):
    self.tearDown()

  def dt_to_long(self, dt):
    # http://stackoverflow.com/questions/2755573/convert-date-datetime-to-timestamp
    return int((mktime(dt.timetuple()) + dt.microsecond / 1000000.0) * 1000)


  def tearDown(self):
    com = Comm()
    com.request("DELETE", "/users/1")
    com.request("DELETE", "/users/2")
    com.request("DELETE", "/users/3")
    com.request("DELETE", "/users/4")
    com.request("DELETE", "/users/5")
    com.request("DELETE", "/users/6")
    com.request("DELETE", "/users/7")
    com.request("DELETE", "/users/8")
    com.request("DELETE", "/users/9")
    com.request("DELETE", "/users/10")
    com.request("DELETE", "/users/105")
    com.request("DELETE", "/services/106")
    com.request("DELETE", "/authinfos/107")

  def test_keyserver_userpwd(self):
    com = Comm()
    com.set_json_request_encoding(True) #encode requests with application/json
    # create a user
    res = com.request("POST", "/users/105/mypw/register")
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # register a new service, e.g. e-mail imap
    res = com.request("POST", "/services/106/register")
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # store e-mail account specific data 
    res = com.request("POST", "/authinfos/add/userpwd", 
        {
          "bmu_user_id" : 105, 
          "user_pwd" : "mypw",
          "bmu_service_id" : 106,
          "bmu_authinfo_id" : 107, # new authinfo id, i guess
          "ai_username" : "MyUsernameOfService106",
          "ai_pwd" : "MyPasswordOfService106"
        })
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # create access token for account
    res = com.request("POST", "/tokens/token",
        {
          "bmu_user_id" : 105, 
          "user_pwd" : "mypw",
          "bmu_service_ids" : [106],
          "bmu_authinfo_ids" : [107],
          "backupdate" : self.dt_to_long(datetime.now())
        })
    self.assertEquals(res.code, httplib.OK)
    token = res.data["token"]
    tokenId = res.data["bmu_token_id"]
    # access the data
    res = com.request("POST", "/tokens/data",
        {
          "token" : token,
          "bmu_token_id" : tokenId
        }
        )
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals("MyPasswordOfService106", res.data["authinfos"][0]["ai_pwd"])
    self.assertEquals("MyUsernameOfService106", res.data["authinfos"][0]["ai_username"])
  
  def test_keyserver_oauth(self):
    com = Comm()
    com.set_json_request_encoding(True) #encode requests with application/json
    # create a user
    res = com.request("POST", "/users/105/mypw/register")
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # register a new service, e.g. e-mail imap
    res = com.request("POST", "/services/106/register")
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # store e-mail account specific data 
    res = com.request("POST", "/authinfos/add/oauth", 
        {
          "bmu_user_id" : 105, 
          "user_pwd" : "mypw",
          "bmu_service_id" : 106,
          "bmu_authinfo_id" : 107, # new authinfo id, i guess
          "ai_oauth" : "MyOauthTokenOfService106"
        })
    self.assertEquals(res.code, httplib.NO_CONTENT)
    # create access token for account
    res = com.request("POST", "/tokens/token",
        {
          "bmu_user_id" : 105, 
          "user_pwd" : "mypw",
          "bmu_service_ids" : [106],
          "bmu_authinfo_ids" : [107],
          "backupdate" : self.dt_to_long(datetime.now())
        })
    self.assertEquals(res.code, httplib.OK)
    token = res.data["token"]
    tokenId = res.data["bmu_token_id"]
    # access the data
    res = com.request("POST", "/tokens/data",
        {
          "token" : token,
          "bmu_token_id" : tokenId
        }
        )
    self.assertEquals(res.code, httplib.OK)
    self.assertEquals("MyOauthTokenOfService106", res.data["authinfos"][0]["ai_oauth"])

