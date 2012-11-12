import RESTBackMeUp as BMU

def register():
  BMU.delete_user("MyTestUser1234")
  BMU.delete_user("MyTestUser123")
  res = BMU.register_user("MyTestUser123", "123456789", "123456789", "MyTestUser123@trash-mail.com")
  BMU.verify_email(res.data["verificationKey"])

register()
print BMU.delete_user("MyTestUser123")
raw_input("delete completed...")
register()
print BMU.change_user("MyTestUser123", "MyTestUser1234", "123456789", "12345678", "MightyDuck@trash-mail.com")
raw_input("change_user completed...")
res = BMU.new_verification_email("MyTestUser1234")
BMU.verify_email(res.data["verificationKey"])
print BMU.login_user("MyTestUser1234", "12345678")
raw_input("login completed...")
print BMU.set_user_property("MyTestUser1234", "jameston", "charlston")
raw_input("set user property completed...")
print BMU.delete_user_property("MyTestUser1234", "jameston")
raw_input("delete user property completed...")
sourceId = BMU.auth_datasource("MyTestUser1234", "org.backmeup.dummy", "SrcProfile", "12345678").data["profileId"]
print BMU.delete_datasource_profile("MyTestUser1234", sourceId)
raw_input("delete datasource profile completed...")
sourceId = BMU.auth_datasource("MyTestUser1234", "org.backmeup.dummy", "SrcProfile", "12345678").data["profileId"]
print BMU.post_auth_datasource("MyTestUser1234", sourceId, "12345678", {"yep" : "hi"})
raw_input("post authorize datasource profile completed...")
sinkId = BMU.auth_datasink("MyTestUser1234", "org.backmeup.dummy", "SrcProfile", "12345678").data["profileId"]
print BMU.delete_datasink_profile("MyTestUser1234", sinkId)
raw_input("delete datasink profile completed...")
sinkId = BMU.auth_datasink("MyTestUser1234", "org.backmeup.dummy", "SrcProfile", "12345678").data["profileId"]
print BMU.post_auth_datasink("MyTestUser1234", sinkId, "12345678", {"yep" : "hi"})
raw_input("post authorize datasink profile completed...")
jobId = BMU.create_backup_job("MyTestUser1234", "12345678", [sourceId], [], sinkId, "daily", "Dummy to dummy job" ).data["job"]["jobId"]
print BMU.change_datasource_profile("MyTestUser1234", jobId, sourceId, {"key1" : "value1"})
raw_input("change datasource profile completed...")

