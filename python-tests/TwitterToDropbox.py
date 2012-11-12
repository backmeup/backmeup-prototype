
import RESTBackMeUp as BMU

TWITTER_AUTH_DATA = {
  "secret" : "WYnjIW2ymHBXI4IjPUubaeSgxaMtQ3gEv8bFdGsN7lQ",
  "oauth_token" : "n7MgvbBnL2keYejuC0FePAFuAsXmzC6P5BpeFqCZs",
  "oauth_verifier" : "igLr5JsgmQlxNcQXFoAtcFAWZoilwLPHbw1OFuRkIM",
  "token" : "843501650-3eU0c2txIX6rzNTNaPXEmHcsN7qvu7drzNNo32Sz"
}

WRONG_TWITTER_AUTH_DATA = {
  "secret" : "WYnjIW2ymHBXI4IjPUubaeSgxaMtQ3gEv8bFdGsN7lQxxxx",
  "oauth_token" : "n7MgvbBnL2keYejuC0FePAFuAsXmzC6P5BpeFqCZsxxxx",
  "oauth_verifier" : "igLr5JsgmQlxNcQXFoAtcFAWZoilwLPHbw1OFuRkIMxxxx",
  "token" : "843501650-3eU0c2txIX6rzNTNaPXEmHcsN7qvu7drzNNo32Szxxxx"
}

DROPBOX_AUTH_DATA = {
  "token":"thhc9we93cwk78s", "secret":"656rwxei8vf7i5v"
}

WRONG_DROPBOX_AUTH_DATA = {
  "token":"ABCthhc9we93cwk78s", "secret":"ABC656rwxei8vf7i5v"
}

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.dropbox", "SinkProfile", "password").data["profileId"]
BMU.update_profile(sinkId, WRONG_DROPBOX_AUTH_DATA, "password");
    
sourceId = BMU.auth_datasource("TestUser", "org.backmeup.twitter", "SinkProfile", "password").data["profileId"]
BMU.update_profile(sourceId, WRONG_TWITTER_AUTH_DATA, "password");


res = BMU.create_backup_job("TestUser", "password", [sourceId], ["org.backmeup.indexer"], sinkId, "daily", "Twitter to Dropbox")
print res
