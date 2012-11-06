import RESTBackMeUp as BMU
USER ="TestUser"
PASS ="password"
print "===================================== *:* ============================"
sId = BMU.request_search_index(USER, PASS, raw_input("Search For: ")).data["searchId"]
print BMU.query_index(USER, sId).data

