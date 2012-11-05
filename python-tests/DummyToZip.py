
import RESTBackMeUp as BMU

USER = "TestUser@trash-mail.com"
PASS = "password"

try:
  res = BMU.register_user(USER, PASS, PASS, USER)
  BMU.verify_email(res.data["verificationKey"])
except:
  pass

sourceId = BMU.auth_datasource(USER, "org.backmeup.dummy", "SrcProfile", PASS).data["profileId"]

sinkId = BMU.auth_datasink(USER, "org.backmeup.zip", "SinkProfile", PASS).data["profileId"]

res = BMU.create_backup_job(USER, PASS, [sourceId], [], sinkId, "realtime", "Dummy to Zip")

