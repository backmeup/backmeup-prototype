
import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.dummy", "SrcProfile", "password").data["profileId"]
    
sourceId = BMU.auth_datasource("TestUser", "org.backmeup.dropbox", "SinkProfile", "password").data["profileId"]
BMU.update_profile(sourceId, {"token":"thhc9we93cwk78s", "secret":"656rwxei8vf7i5v"}, "password");


res = BMU.create_backup_job("TestUser", "password", [sourceId], [], sinkId, "realtime", "TestUserJob")

