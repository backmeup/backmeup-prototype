
import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.dummy", "SinkProfile", "password").data["profileId"]
    
sourceId = BMU.auth_datasource("TestUser", "org.backmeup.dummy", "SinkProfile", "password").data["profileId"]

print "================ Job WITH encryption ==================="
res = BMU.create_backup_job("TestUser", "password",
		[sourceId],["org.backmeup.indexer","org.backmeup.encryption"], sinkId,
		"realtime", "Dummy To Dummy", {"encryptionPwd" : "superiorEncryptionPassword"})
print res

print "================ Job without encryption ==================="
res = BMU.create_backup_job("TestUser", "password",
		[sourceId],["org.backmeup.indexer"], sinkId,
		"realtime", "Dummy To Dummy")
print res
