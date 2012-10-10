import RESTBackMeUp as BMU
print "===================================== *:* ============================"
sId = BMU.request_search_index("TestUser", "password", "*:*").data["searchId"]
print BMU.query_index("TestUser", sId).data

print "========================== title:html.txt ============================"
sId = BMU.request_search_index("TestUser", "password", "filename:html.txt").data["searchId"]
print BMU.query_index("TestUser", sId).data

