from REST import Comm
from time import mktime

##### User operations #####
com = Comm()

def register_user(user, password, keyRingPassword, email):
  answer = com.request("POST", "/users/" + user + "/register", {"password" : password, "keyRing" : keyRingPassword, "email" : email})
  return answer

def delete_user(user):
  answer = com.request("DELETE", "/users/" + user)
  return answer

def change_user(oldUsername, newUsername, oldPassword, newPassword, newEmail):
  answer = com.request("PUT", "/users/" + oldUsername, 
      { "username" : newUsername, "oldPassword" : oldPassword, "password" : newPassword, "email" : newEmail })
  return answer

def verify_email(verificationKey):
  answer = com.request("GET", "/users/" + verificationKey + "/verifyEmail");
  return answer

def new_verification_email(user):
  answer = com.request("GET", "/users/" + user + "/newVerificationEmail");
  return answer

def login_user(user, password):
  answer = com.request("POST", "/users/" + user + "/login", {"password" : password})
  return answer

def get_user(user):
  answer = com.request("GET", "/users/" + user)
  return answer

def get_user_property(user, prop):
  answer = com.request("GET", "/users/" + user + "/properties/" + prop)
  return answer

def set_user_property(user, prop, value):
  answer = com.request("POST", "/users/" + user + "/properties/" + prop + "/"  + value)
  return answer

def delete_user_property(user, prop):
  answer = com.request("DELETE", "/users/" + user + "/properties/" + prop)
  return answer
  
##### Datasource operations #####

def get_datasources():
  return com.request("GET", "/datasources")

def get_datasource_profiles(user):
  return com.request("GET", "/datasources/" + user + "/profiles")

def delete_datasource_profile(user, profileId):
  return com.request("DELETE", "/datasources/" + user + "/profiles/" + str(profileId))

def generate_datasource_options(user, profileId, keyRing):
  return com.request("POST", "/datasources/" + user + "/profiles/" + str(profileId) + "/options", {"keyRing" : keyRing})

def get_stored_datasource_options(user, profileId, jobId):
  return com.request("GET", "/datasources/" + user + "/profiles/" + str(profileId) + "/" + str(jobId) + "/storedOptions")

def change_datasource_profile(user, jobId, profileId, options):
  return com.request("PUT", "/datasources/" + user + "/profiles/" + str(profileId) +"/" + str(jobId), {"sourceOptions" : options})

def auth_datasource(user, datasourceId, profileName, keyRing):
  return com.request("POST", "/datasources/" + user + "/" + str(datasourceId) + "/auth", {"profileName" : profileName, "keyRing" : keyRing})

def post_auth_datasource(user, profileId, keyRing, authParams):
  authParams.update({"keyRing" : keyRing})
  return com.request("POST", "/datasources/" + user + "/" + str(profileId) + "/auth/post", authParams)


##### Datasink operations #####

def get_datasinks():
  return com.request("GET", "/datasinks")

def get_datasink_profiles(user):
  return com.request("GET", "/datasinks/" + user + "/profiles")

def delete_datasink_profile(user, profileId):
  return com.request("DELETE", "/datasinks/" + user + "/profiles/" + str(profileId))

def change_datasink_profile(user, profileId, options):
  return com.request("PUT", "/datasinks/" + user + "/profiles/" + str(profileId), {sourceOptions : options})

def auth_datasink(user, datasinkId, profileName, keyRing):
  return com.request("POST", "/datasinks/" + user + "/" + str(datasinkId) + "/auth", {"profileName" : profileName, "keyRing" : keyRing})

def post_auth_datasink(user, profileId, keyRing, authParams):
  authParams.update({"keyRing" : keyRing})
  return com.request("POST", "/datasinks/" + user + "/" + str(profileId) + "/auth/post", authParams)

##### Profile operations #####

def update_profile(profileId, params, keyRing):
  params["keyRing"] = keyRing
  return com.request("POST", "/profiles/" + str(profileId), params)

##### Metadata operations #####

def get_specific_metadata(user, profileId, prop, keyRing):
  return com.request("POST", "/meta/" + user + "/" + str(profileId) + "/" + prop, {"keyRing" : keyRing})

def get_metadata(user, profileId, keyRing):
  return com.request("POST", "/meta/" + user + "/" + str(profileId), {"keyRing" : keyRing})

##### Backup-Job operations #####

def get_backup_jobs(user):
  return com.request("GET", "/jobs/" + user)


def validate_backup_job(user, jobId, keyRing):
  return com.request("POST", "/jobs/" + user + "/validate/" + str(jobId), {"keyRing" : keyRing})

def create_backup_job(user, keyRing, sourceProfileIds, requiredActions, sinkProfileId, when, sourceOptions=None):
  params = {"sourceProfileIds" : sourceProfileIds,
            "requiredActionIds" : requiredActions,
            "keyRing" : keyRing,
            "timeExpression" : when,
            "sinkProfileId" : sinkProfileId
           }
  return com.request("POST", "/jobs/" + user, params)

def delete_backup_job(user, jobId):
  return com.request("DELETE", "/jobs/" + user + "/" + str(jobId))

def get_backup_job_status(user, jobId):
  req = "/jobs/" + user + "/" + str(jobId) + "/status";
  return com.request("GET", req)

def get_all_backup_job_status(user):
  return com.request("GET", "/jobs/" + user + "/status")


def get_file_details(user, fileId):
  return com.request("GET", "/jobs/" + user + "/" + fileId + "/details")

def get_overview(user, duration):
  return com.request("GET", "/jobs/" + user + "/status/overview?duration=" + duration)

#def validate_profile(user, profileId):
#  return request("GET", "/datasources

######### Action Operations #########
def get_actions():
  return com.request("GET", "/actions")

######### Mail Operations ###########
def send_text_mail(to, subject, msg):
  return com.request("POST", "/mails/send/text", {
      "to" : to,
      "subject" : subject,
      "message" : msg
    })

def send_html_mail(to, subject, msg):
  return com.request("POST", "/mails/send/html", {
      "to" : to,
      "subject" : subject,
      "message" : msg
    })
######### Search Operations #########
def request_search_index(username, keyRing, query):
  return com.request("POST", "/backups/" + username + "/search", {
      "query" : query,
      "keyRing" : keyRing
    })

def query_index(username, searchId):
  return com.request("GET" , "/backups/" + username + "/" + str(searchId) + "/query")

    

