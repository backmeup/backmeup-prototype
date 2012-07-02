from urllib import urlencode
from httplib import HTTPConnection, HTTP
from mimetypes import guess_type
from json import loads

from RESTConfig import SERVER, PORT, BASE_URL
import logging
import logging.config
from socket import error as socket_error
from datetime import datetime

logging.config.fileConfig('logging.conf')
logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())

class RequestResult:
  def __init__(self, code, reason, data, location=None):
    self.code = code
    self.reason = reason
    self.data = data
    self.location = location
  def __str__(self):
    location = ""
    if (self.location != None):
      location = " Location: " + self.location
    data = ""
    if (self.data != None and len(str(self.data)) > 0):
      data = "\nData:\n" + str(self.data)
    return "Code: " + str(self.code) + " => Reason: " + self.reason + location + data


class Comm :

  def _create_connection(self):
    self.con = HTTPConnection(SERVER, PORT)
    #self.con.set_debuglevel(100)    

  def __init__(self):
    self._create_connection()
    self._retry_count = 0

  def request(self, op, url, params=None, isFile=False, convertToJson=True):
    try:
      if hasattr(url, "startswith") == True:
        if not url.startswith("http://") and not url.startswith("https://"):
          url = BASE_URL + url
      if isFile == True:
        name, fileName = params
        fh = open(fileName, "rb")
        data = fh.read()
        fh.close()
        data = self._post_multipart(SERVER+":"+str(PORT), url, None, [[name, fileName, data]])
        return data
      headers = {"Content-Type" : "application/x-www-form-urlencoded", "Accept" : "application/json", "Connection" : "Keep-Alive", "Cache-Control" : "max-age=0", "User-Agent" : "Mozilla/5.0 (Windows NT 6.1; WOW64)"}
      if params <> None:
        encoded = urlencode(params, doseq=True)
        logger.debug("REQUEST: "+ op + " http://" + SERVER + ":" + str(PORT) + url + " " + str(encoded))
        self.con.request(op, url, encoded, headers)
      else:
        logger.debug("REQUEST: " + op + " http://" + SERVER + ":" + str(PORT) + url)
        self.con.request(op, url, headers=headers)
      logger.debug("getting response...")
      resp = self.con.getresponse()
      logger.debug(str(resp.status) + " " + resp.reason)
      location = None
      if resp.status == 202:     
        location = resp.getheader("location")

      result = resp.read()
  
      if resp.status != 204 and convertToJson == True:
        try:
          result = loads(result)
        except:
          pass
  
      self._retry_count = 0;
      rr = RequestResult(resp.status, resp.reason, result, location)
      logger.debug(str(rr))
      return rr
    except socket_error as se:
      logger.debug("Exception during request: " + str(se))
      self._create_connection()
      self._retry_count += 1;
      logger.debug("Retrying request for the " + str(self._retry_count) + ". time")
      if (self._retry_count > 3):
        logger.error("Couldn't fullfill request! Aborting.")
        raise Exception("Error: couldn't establish a connection to the requested server! (Address: " + SERVER + " / Port: " + str(PORT)) 
      return self.request(op, url, params, isFile, convertToJson)

  # The following three methods are from: http://code.activestate.com/recipes/146306-http-client-to-post-using-multipartform-data/
  def _post_multipart(self, host, selector, fields, files):
    """
    Post fields and files to an http host as multipart/form-data.
    fields is a sequence of (name, value) elements for regular form fields.
    files is a sequence of (name, filename, value) elements for data to be uploaded as files
    Return the server's response page.
    """
    content_type, body = self._encode_multipart_formdata(files)
    h = HTTP(host)
    h.putrequest('POST', selector)
    h.putheader('content-type', content_type)
    h.putheader('content-length', str(len(body)))
    h.endheaders()
    h.send(body)
    errcode, errmsg, headers = h.getreply()
    logger.debug("File sent with error code " + str(errcode) + "; Message was: " + str(errmsg))
    return RequestResult(errcode, errmsg, h.file.read())

  def _encode_multipart_formdata(self, files):
    """
    fields is a sequence of (name, value) elements for regular form fields.
    files is a sequence of (name, filename, value) elements for data to be uploaded as files
    Return (content_type, body) ready for httplib.HTTP instance
    """
    BOUNDARY = '----------ThIs_Is_tHe_bouNdaRY_$'
    CRLF = '\r\n'
    L = []
    for (key, filename, value) in files:
        L.append('--' + BOUNDARY)
        L.append('Content-Disposition: form-data; name="%s"; filename="%s"' % (key, filename))
        L.append('Content-Type: %s' % _get_content_type(filename))
        L.append('')
        L.append(value)
    L.append('--' + BOUNDARY + '--')
    L.append('')
    body = CRLF.join(L)
    content_type = 'multipart/form-data; boundary=%s' % BOUNDARY
    return content_type, body

  def _get_content_type(self, filename):
    return guess_type(filename)[0] or 'application/octet-stream'

