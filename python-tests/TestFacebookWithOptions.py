import RESTBackMeUp as BMU
user="fbtest@trash-mail.com"
pw="password"

BMU.delete_user(user)
res = BMU.register_user(user, pw, pw, user)
BMU.verify_email(res.data["verificationKey"])

res = BMU.auth_datasource(user, "org.backmeup.facebook", "SrcProfile", pw)
print "Open following URL:"
print res.data["redirectURL"]
sourceId = res.data["profileId"]
code = raw_input("Enter code: ")

print BMU.post_auth_datasource(user, sourceId, pw, {"code" : code})

print BMU.generate_datasource_options(user, sourceId, pw)
what = raw_input("Enter option to use: ")

sinkId = BMU.auth_datasink(user, "org.backmeup.dummy", "SinkProfile", pw).data["profileId"]

job = BMU.create_backup_job(user, pw, [sourceId], [], sinkId, "daily", "Facebook to dummy").data["job"]["jobId"]
print BMU.change_datasource_profile(user, job, sourceId, [what])

