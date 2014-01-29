# -*- coding: ISO-8859-1 -*-

import RESTBackMeUp as BMU
import json

# USER ="t1@fetzig.at"
USER = "we@x-net.at"
PASS = "we19841225"
# PASS ="123456789"
print "===================================== *:* ============================"
sId = BMU.request_search_index(USER, PASS, raw_input("Search For: ")).data["searchId"]
# sId = BMU.request_search_index(USER, PASS, "*").data["searchId"]
print json.dumps(BMU.query_index(USER, sId).data, sort_keys=True, indent=4)


