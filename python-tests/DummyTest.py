
import RESTBackMeUp as BMU


sourceId = BMU.auth_datasource("Rainer", "org.backmeup.dummy", "SrcProfile", "password").data["profileId"]
    
sinkId = BMU.auth_datasink("Rainer", "org.backmeup.dummy", "SinkProfile", "password").data["profileId"]


res = BMU.create_backup_job("Rainer", "password", [sourceId], ["org.backmeup.actiondummy"], sinkId, "realtime")

