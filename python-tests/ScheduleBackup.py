#!/usr/bin/python

import RESTBackMeUp as BMU

appKey = "4pheai3cd0btkd5"
appSecret = "0sjcpoqe1jeqlat"


BMU.delete_user("Seppl")
res = BMU.register_user("Seppl", "SepplSeppl", "SepplSeppl", "backmeup71@gmx.at")
print res.data
BMU.verify_email(res.data["verificationKey"])
res = BMU.auth_datasink("Seppl", "org.backmeup.dropbox", "Dropbox", "SepplSeppl")
sinkId = res.data["profileId"]
res = BMU.update_profile(sinkId, {"token" : appKey, "secret" : appSecret}, "SepplSeppl")
res = BMU.auth_datasource("Seppl", "org.backmeup.dropbox", "Dropbox", "SepplSeppl")
sourceId = res.data["profileId"]
res = BMU.update_profile(sourceId, {"token" : appKey, "secret" : appSecret}, "SepplSeppl")
res = BMU.create_backup_job("Seppl", "SepplSeppl", [sourceId], None, sinkId, "daily")


