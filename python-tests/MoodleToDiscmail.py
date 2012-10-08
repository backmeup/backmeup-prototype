
import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sourceId = BMU.auth_datasource("TestUser", "org.backmeup.moodle", "SrcProfile", "password").data["profileId"]

BMU.update_profile(sourceId, {"Username" : "backmeup", "Password" : "286bafbb1a9faf4dc4e104a33e222304", "Moodle Server Url" : "http://gtn02.gtn-solutions.com/moodle20"}, "password");

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.discmailing", "SinkProfile", "password").data["profileId"]

BMU.update_profile(sinkId, {"Street" : "Mariahilferstrasse 15", "City" : "Wien", "Postcode" : "1150"}, "password");

res = BMU.create_backup_job("TestUser", "password", [sourceId], [], sinkId, "realtime")

