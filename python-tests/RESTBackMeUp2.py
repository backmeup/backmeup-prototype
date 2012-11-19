from REST import Comm
from time import mktime

class BMU:
  ##### User operations #####
  def __init__(self):
    self.com = Comm()
   
  def register_user(self, user, password, keyRingPassword, email):
    answer = self.com.request("POST", "/users/" + user + "/register", {"password" : password, "keyRing" : keyRingPassword, "email" : email})
    return answer
  
  def delete_user(self, user):
    answer = self.com.request("DELETE", "/users/" + user)
    return answer
  
  def change_user(self, oldUsername, newUsername, oldPassword, newPassword, newEmail):
    answer = self.com.request("PUT", "/users/" + oldUsername, 
        { "username" : newUsername, "oldPassword" : oldPassword, "password" : newPassword, "email" : newEmail })
    return answer
  
  def verify_email(self, verificationKey):
    answer = self.com.request("GET", "/users/" + verificationKey + "/verifyEmail");
    return answer
  
  def new_verification_email(self, user):
    answer = self.com.request("GET", "/users/" + user + "/newVerificationEmail");
    return answer
  
  def login_user(self, user, password):
    answer = self.com.request("POST", "/users/" + user + "/login", {"password" : password})
    return answer
  
  def get_user(self, user):
    answer = self.com.request("GET", "/users/" + user)
    return answer
  
  def get_user_property(self, user, prop):
    answer = self.com.request("GET", "/users/" + user + "/properties/" + prop)
    return answer
  
  def set_user_property(self, user, prop, value):
    answer = self.com.request("POST", "/users/" + user + "/properties/" + prop + "/"  + value)
    return answer
  
  def delete_user_property(self, user, prop):
    answer = self.com.request("DELETE", "/users/" + user + "/properties/" + prop)
    return answer
  
  ##### Datasource operations #####
  
  def get_datasources(self):
    return self.com.request("GET", "/datasources")
  
  def get_datasource_profiles(self, user):
    return self.com.request("GET", "/datasources/" + user + "/profiles")
  
  def delete_datasource_profile(self, user, profileId):
    return self.com.request("DELETE", "/datasources/" + user + "/profiles/" + str(profileId))
  
  def generate_datasource_options(self, user, profileId, keyRing):
    return self.com.request("POST", "/datasources/" + user + "/profiles/" + str(profileId) + "/options", {"keyRing" : keyRing})
  
  def get_stored_datasource_options(self, user, profileId, jobId):
    return self.com.request("GET", "/datasources/" + user + "/profiles/" + str(profileId) + "/" + str(jobId) + "/storedOptions")
  
  def change_datasource_profile(self, user, jobId, profileId, options):
    return self.com.request("PUT", "/datasources/" + user + "/profiles/" + str(profileId) +"/" + str(jobId), {"sourceOptions" : options})
  
  def auth_datasource(self, user, datasourceId, profileName, keyRing):
    return self.com.request("POST", "/datasources/" + user + "/" + str(datasourceId) + "/auth", {"profileName" : profileName, "keyRing" : keyRing})
  
  def post_auth_datasource(self, user, profileId, keyRing, authParams):
    authParams.update({"keyRing" : keyRing})
    return self.com.request("POST", "/datasources/" + user + "/" + str(profileId) + "/auth/post", authParams)
  
  
  ##### Datasink operations #####
  
  def get_datasinks(self):
    return self.com.request("GET", "/datasinks")
  
  def get_datasink_profiles(self, user):
    return self.com.request("GET", "/datasinks/" + user + "/profiles")
  
  def delete_datasink_profile(self, user, profileId):
    return self.com.request("DELETE", "/datasinks/" + user + "/profiles/" + str(profileId))
  
  def change_datasink_profile(self, user, profileId, options):
    return self.com.request("PUT", "/datasinks/" + user + "/profiles/" + str(profileId), {sourceOptions : options})
  
  def auth_datasink(self, user, datasinkId, profileName, keyRing):
    return self.com.request("POST", "/datasinks/" + user + "/" + str(datasinkId) + "/auth", {"profileName" : profileName, "keyRing" : keyRing})
  
  def post_auth_datasink(self, user, profileId, keyRing, authParams):
    authParams.update({"keyRing" : keyRing})
    return self.com.request("POST", "/datasinks/" + user + "/" + str(profileId) + "/auth/post", authParams)
  
  ##### Profile operations #####
  
  def update_profile(self, profileId, params, keyRing):
    params["keyRing"] = keyRing
    return self.com.request("POST", "/profiles/" + str(profileId), params)
  
  ##### Metadata operations #####
  
  def get_specific_metadata(self, user, profileId, prop, keyRing):
    return self.com.request("POST", "/meta/" + user + "/" + str(profileId) + "/" + prop, {"keyRing" : keyRing})
  
  def get_metadata(self, user, profileId, keyRing):
    return self.com.request("POST", "/meta/" + user + "/" + str(profileId), {"keyRing" : keyRing})
  
  ##### Backup-Job operations #####
  
  def get_backup_jobs(self, user):
    return self.com.request("GET", "/jobs/" + user)
  
  
  def validate_backup_job(self, user, jobId, keyRing):
    return self.com.request("POST", "/jobs/" + user + "/validate/" + str(jobId), {"keyRing" : keyRing})


  def create_backup_job(self, user, keyRing, sourceProfiles, requiredActions, sinkProfileId, when, jobTitle, settings=None):
    params = {"sourceProfiles" : sourceProfiles,
            "keyRing" : keyRing,
            "timeExpression" : when,
            "sinkProfileId" : sinkProfileId,
            "jobTitle" : jobTitle,
            "actions" : requiredActions
           }
    if (settings != None):
      params.update(settings)
    return self.com.request("POST", "/jobs/" + user, params)
  
  def delete_backup_job(self, user, jobId):
    return self.com.request("DELETE", "/jobs/" + user + "/" + str(jobId))
  
  def get_backup_job_status(self, user, jobId):
    req = "/jobs/" + user + "/" + str(jobId) + "/status";
    return self.com.request("GET", req)
  
  def get_all_backup_job_status(self, user):
    return self.com.request("GET", "/jobs/" + user + "/status")
  
  
  def get_file_details(self, user, fileId):
    return self.com.request("GET", "/jobs/" + user + "/" + fileId + "/details")
  
  def get_overview(self, user, duration):
    return self.com.request("GET", "/jobs/" + user + "/status/overview?duration=" + duration)
  
  #def validate_profile(self, user, profileId):
  #  return request("GET", "/datasources
  
  ######### Action Operations #########
  def get_actions(self ):
    return self.com.request("GET", "/actions")
  
  def get_action_options(self, actionId):
    return self.com.request("GET", "/actions/" + actionId + "/options")
  
  def get_stored_action_options(self, actionId, jobId):
    return self.com.request("GET", "/actions/" + actionId + "/storedOptions/" + str(jobId))
  
  def update_action_options(self, actionId, jobId, params):
    return self.com.request("PUT", "/actions/" + actionId + "/options/" + str(jobId), params)
  
  ######### Mail Operations ###########
  def send_text_mail(self, to, subject, msg):
    return self.com.request("POST", "/mails/send/text", {
      "to" : to,
      "subject" : subject,
      "message" : msg
    })
  
  def send_html_mail(self, to, subject, msg):
    return self.com.request("POST", "/mails/send/html", {
      "to" : to,
      "subject" : subject,
      "message" : msg
    })
  ######### Search Operations #########
  def request_search_index(self, username, keyRing, query):
    return self.com.request("POST", "/backups/" + username + "/search", {
      "query" : query,
      "keyRing" : keyRing
    })
  
  def query_index(self, username, searchId):
    return self.com.request("GET" , "/backups/" + username + "/" + str(searchId) + "/query")

