import RESTBackMeUp as BMU

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

SKYDRIVE_AUTH = {
  "secret" : "jvZDGQl1aUamXYJCpyqWCqncO5bxRpd-",
  "refreshToken" : "vEUNSAAAHgA%241uVUDAYSjCt!wxeimQCsIl!wCKVNQwvMzJl1ap9TawbDjQR1tAO7DW4rvd35HBKeMu2*svalqszcgJJDP3osO*W8e96SWsEp9oSEXxi!5vgEhoNnHkCPw2fLcgYyoHyOWzvxH4mdJ8C5toMdugYEcyKRZLJPR7BfC2",
  "token": "EwAoAq1DBAAUlbRWyAJjK5w968Ru3Cyt\/6GvwXwAAWEUR4uTb0I1nikK\/BOgJqORMRWmtnATWNyinXE47pLib87+hOTiv4kFkOlb86GwEzn69Mkup5v5Qpl4+E9OA5nVzm69ah9b6mMVlMDKqjGxdwLBxtrAi8\/kAfYh0eG+GT9gILR504RUYe5VKPYpdKBwTAvbdX9HCMct2v3oc4lG24ZMu3Z9MTXWQOOfIIFrD\/s2Mdwe\/pcZE1\/M2q6RrFEaBqKRLAEUEupmSn6TDCtTZZReAlCASjKguZPzjXm8K5icMp9ho3myLxZmDDWJZ7Jpn89QwoBLsw85wrEDOrjkrw+fpkjenQ55dzUfw9lwvzMJ\/jJsWd9qK10xX72+I6gDZgAACCCUMcDqmjjK+AAduBLN6yL4JDhjgh4nQjsP4r4pKN6fsnViobcnezTFBXyGalF7kG1T97iTjsOCxw4zR1r3b\/hIS+4aGwTlY8Z\/OSU2jqprEe6IH6Ccvpm6pT8RqFta3ODOrx1XuEBMN9e4bTbmyivzKePGLVZTa4pDWfmBwWL9qyPv\/JWXj0wqFHo6Wj4ExUG1EpjpsN807BSoplKoMQSU7izU8SOadak4T0OStHhCzSwZi6jpIQYnUuDF8Ja9cuScbnthS2wywST3yPRmKaZFLullgbqyJ1QpVkMkgQufef4eOZ+cABdfootlrtaA5J2kfHxoeW\/76lnr1sZ\/T0DfyAAA",
  "key" : "00000000480D45BC",
  "code" : "aedfdc21-d223-2fe7-d186-74f8c70ffa3b",
  "callback" : "http://www.backmeup.at/"
}

BMU.delete_user("TestUser")
res = BMU.register_user("TestUser", "password", "password", "TestUser@trash-mail.com")
BMU.verify_email(res.data["verificationKey"])

sinkId = BMU.auth_datasink("TestUser", "org.backmeup.skydrive", "SrcProfile", "password").data["profileId"]

BMU.update_profile(sinkId, SKYDRIVE_AUTH, "password");

sourceId = BMU.auth_datasource("TestUser", "org.backmeup.dummy", "SinkProfile", "password").data["profileId"]

res = BMU.create_backup_job("TestUser", "password", [sourceId], [], sinkId, "realtime", "Dummy to Skydrive")

