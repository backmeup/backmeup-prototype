
import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sourceId = BMU.auth_datasource("TestUser", "org.backmeup.dummy", "SrcProfile", "password").data["profileId"]

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.discmailing", "SinkProfile", "password").data["profileId"]

BMU.update_profile(sinkId, {"Street" : "Mariahilferstrasse 15", "City" : "Wien", "Postcode" : "1150"}, "password");

res = BMU.create_backup_job("TestUser", "password", [sourceId], [], sinkId, "realtime", "Dummy to Discmailing")

