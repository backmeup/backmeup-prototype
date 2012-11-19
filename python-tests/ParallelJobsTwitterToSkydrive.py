from threading import Thread, current_thread
from optparse import OptionParser
from RESTBackMeUp2 import BMU
from time import time
from datetime import datetime

TWITTER_AUTH_DATA = {
  "secret" : "WYnjIW2ymHBXI4IjPUubaeSgxaMtQ3gEv8bFdGsN7lQ",
  "oauth_token" : "n7MgvbBnL2keYejuC0FePAFuAsXmzC6P5BpeFqCZs",
  "oauth_verifier" : "igLr5JsgmQlxNcQXFoAtcFAWZoilwLPHbw1OFuRkIM",
  "token" : "843501650-3eU0c2txIX6rzNTNaPXEmHcsN7qvu7drzNNo32Sz"
}

SKYDRIVE_AUTH = {
  "secret" : "jvZDGQl1aUamXYJCpyqWCqncO5bxRpd-",
  "refreshToken" : "vEUNSAAAHgA%241uVUDAYSjCt!wxeimQCsIl!wCKVNQwvMzJl1ap9TawbDjQR1tAO7DW4rvd35HBKeMu2*svalqszcgJJDP3osO*W8e96SWsEp9oSEXxi!5vgEhoNnHkCPw2fLcgYyoHyOWzvxH4mdJ8C5toMdugYEcyKRZLJPR7BfC2",
  "token": "EwAoAq1DBAAUlbRWyAJjK5w968Ru3Cyt\/6GvwXwAAWEUR4uTb0I1nikK\/BOgJqORMRWmtnATWNyinXE47pLib87+hOTiv4kFkOlb86GwEzn69Mkup5v5Qpl4+E9OA5nVzm69ah9b6mMVlMDKqjGxdwLBxtrAi8\/kAfYh0eG+GT9gILR504RUYe5VKPYpdKBwTAvbdX9HCMct2v3oc4lG24ZMu3Z9MTXWQOOfIIFrD\/s2Mdwe\/pcZE1\/M2q6RrFEaBqKRLAEUEupmSn6TDCtTZZReAlCASjKguZPzjXm8K5icMp9ho3myLxZmDDWJZ7Jpn89QwoBLsw85wrEDOrjkrw+fpkjenQ55dzUfw9lwvzMJ\/jJsWd9qK10xX72+I6gDZgAACCCUMcDqmjjK+AAduBLN6yL4JDhjgh4nQjsP4r4pKN6fsnViobcnezTFBXyGalF7kG1T97iTjsOCxw4zR1r3b\/hIS+4aGwTlY8Z\/OSU2jqprEe6IH6Ccvpm6pT8RqFta3ODOrx1XuEBMN9e4bTbmyivzKePGLVZTa4pDWfmBwWL9qyPv\/JWXj0wqFHo6Wj4ExUG1EpjpsN807BSoplKoMQSU7izU8SOadak4T0OStHhCzSwZi6jpIQYnUuDF8Ja9cuScbnthS2wywST3yPRmKaZFLullgbqyJ1QpVkMkgQufef4eOZ+cABdfootlrtaA5J2kfHxoeW\/76lnr1sZ\/T0DfyAAA",
  "key" : "00000000480D45BC",
  "code" : "aedfdc21-d223-2fe7-d186-74f8c70ffa3b",
  "callback" : "http://www.backmeup.at/"
}

def createJob(numberOfJobs, bmu, user, pwd):
  name = current_thread().name
  sourceId = bmu.auth_datasource(user, "org.backmeup.twitter", name, pwd).data["profileId"]
  bmu.update_profile(sourceId, TWITTER_AUTH_DATA, pwd);

  sinkId = bmu.auth_datasink(user, "org.backmeup.skydrive", "SinkProfile", pwd).data["profileId"]
  bmu.update_profile(sinkId, SKYDRIVE_AUTH, pwd);

  for i in range(0, numberOfJobs):
    res = bmu.create_backup_job(user, pwd, [sourceId], ["org.backmeup.indexer"], sinkId, "monthly", "Twitter to Skydrive: " + name)
    if res.code >= 400:
      print res.data

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-c", "--count", dest="count", help="start COUNT threads to execute jobs", metavar="COUNT", default=100)
  parser.add_option("-j", "--jobCount", dest="jobs", help="each thread starts JOBCOUNT jobs", metavar="JOBCOUNT", default=2)
  parser.add_option("-u", "--user", dest="user", help="USER user which shall be used for the jobs", metavar="USER", default="ptest@trash-mail.com")
  parser.add_option("-p", "--password", dest="password", help="PASSWORD of the user which shall be used for the jobs", metavar="PASSWORD", default="123456789")
  parser.add_option("-d", "--delete", dest="delete", help="if DELETE is true, the user will be deleted before starting the test", metavar="DELETE", default=False)
  (options, args) = parser.parse_args()

  print "=============== Settings ================="
  print options
  print "=============== Creating user ============"
  try:
    bmu = BMU()
    if bool(options.delete):
      bmu.delete_user(options.user)
    verificationKey = bmu.register_user(options.user, options.password, options.password, options.user).data["verificationKey"]
    bmu.verify_email(verificationKey)
  except:
    print "User already created!"
  raw_input("Press enter to continue")
  print "=============== Creating threads ========="
  threads = []
  for i in range(0, int(options.count)):
    thread = Thread(target = createJob, args = (int(options.jobs), BMU(), options.user, options.password))
    threads.append(thread)
  startingDate = datetime.now()
  startTime = time()
  print "=============== Starting threads ========="
  for t in threads:
    t.start()
  print "=============== Threads started =========="
  for t in threads:
    t.join()
  print "=============== Finished execution ======="
  endTime = time()
  print "Started @ " + str(startingDate)
  print "==== Duration: " + str(endTime - startTime) + " ======"





  

