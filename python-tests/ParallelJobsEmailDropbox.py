from threading import Thread, current_thread
from optparse import OptionParser
from RESTBackMeUp2 import BMU
from time import time
from datetime import datetime

DROPBOX_DATA={"token":"thhc9we93cwk78s", "secret":"656rwxei8vf7i5v"};
MAIL_SETTINGS = {
  "SSL" : True,
  "Port" : 993,
  "Host" : "imap.gmail.com",
  "Username" : "backmeup100@gmail.com",
  "Password" : "thebackmeuppasswordisgreat",
  "Type" : "imap"
}

def createJob(numberOfJobs, bmu, user, pwd):
  name = current_thread().name
  sourceId = bmu.auth_datasource(user, "org.backmeup.mail", name, pwd).data["profileId"]
  bmu.post_auth_datasource(user, sourceId, pwd, MAIL_SETTINGS);

  sinkId = bmu.auth_datasink(user, "org.backmeup.dropbox", name, pwd).data["profileId"]
  bmu.update_profile(sinkId, DROPBOX_DATA, pwd);

  for i in range(0, numberOfJobs):
    res = bmu.create_backup_job(user, pwd, [sourceId], ["org.backmeup.indexer"], sinkId, "monthly", "EMail to Dropbox: "+name)
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





  

