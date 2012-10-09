
import RESTBackMeUp as BMU

TWITTER_AUTH_DATA = {
  "secret" : "WYnjIW2ymHBXI4IjPUubaeSgxaMtQ3gEv8bFdGsN7lQ",
  "oauth_token" : "n7MgvbBnL2keYejuC0FePAFuAsXmzC6P5BpeFqCZs",
  "oauth_verifier" : "igLr5JsgmQlxNcQXFoAtcFAWZoilwLPHbw1OFuRkIM",
  "token" : "843501650-3eU0c2txIX6rzNTNaPXEmHcsN7qvu7drzNNo32Sz"
}

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.dummy", "SrcProfile", "password").data["profileId"]
    
sourceId = BMU.auth_datasource("TestUser", "org.backmeup.twitter", "SinkProfile", "password").data["profileId"]
BMU.update_profile(sourceId, TWITTER_AUTH_DATA, "password");


res = BMU.create_backup_job("TestUser", "password", [sourceId], [], sinkId, "realtime")

