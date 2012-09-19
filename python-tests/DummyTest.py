
import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sourceId = BMU.auth_datasource("TestUser", "org.backmeup.dummy", "SrcProfile", "password").data["profileId"]
    
sinkId = BMU.auth_datasink("TestUser", "org.backmeup.dummy", "SinkProfile", "password").data["profileId"]


res = BMU.create_backup_job("TestUser", "password", [sourceId], ["org.backmeup.actiondummy"], sinkId, "realtime")

