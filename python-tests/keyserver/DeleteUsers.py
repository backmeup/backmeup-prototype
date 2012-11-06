from REST import Comm

com = Comm()
for i in range(0, 500):
  print i
  com.request("DELETE", "/users/" + str(i))
com.close()
