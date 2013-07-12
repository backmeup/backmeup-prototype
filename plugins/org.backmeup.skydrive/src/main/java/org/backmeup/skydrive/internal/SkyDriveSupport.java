package org.backmeup.skydrive.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.skydrive.SkyDriveDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LiveApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * 
 * The SkyDriveSupport class is based on the SkyDrive Live SDK
 * (http://msdn.microsoft.com/en-us/library/live) and realizes all operations
 * which are necessary to create new files and folders within a users SkyDrive
 * account.
 * 
 * It uses Scribe (https://github.com/fernandezpablo85/scribe-java) and the
 * HttpURLConnection to interact with the SkyDrive REST/OAuth API.
 * 
 * @author fschoeppl
 */
public class SkyDriveSupport {
	private static final String REFRESH_URL = "https://oauth.live.com/token?client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=refresh_token&refresh_token=%s";
	private static final String FOLDER_URL = "https://apis.live.net/v5.0/%s/files";
	private static final String CONTENT_URL = "https://apis.live.net/v5.0/%s/content";
	private static final String ME_URL = "https://apis.live.net/v5.0/me";
	private static final String SHARED_LINK_URL = "https://apis.live.net/v5.0/%s/shared_read_link";

	public static final String ACCESS_TOKEN = "token";
	public static final String REFRESH_TOKEN = "refreshToken";
	public static final String CONSUMER_KEY = "key";
	public static final String CONSUMER_SECRET = "secret";
	
	private static final SimpleDateFormat ISO8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private static final String CRLF = "\r\n";

	public static class Service {
		public OAuthService service;
		public Token accessToken;
	}

	public static Service getService(Properties accessData) {
		Service s = new Service();
		// 1.) Get a service for Windows SkyDrive API:
		s.service = new ServiceBuilder().provider(LiveApi.class)
				.apiKey(accessData.getProperty(CONSUMER_KEY))
				.apiSecret(accessData.getProperty(CONSUMER_SECRET))
				.scope("wl.skydrive wl.skydrive_update wl.offline_access").build();
		// 2.) Create the refresh token based on the EXAMPLE_REFRESH_TOKEN
		// variable.
		Token refreshToken = new Token(accessData.getProperty(REFRESH_TOKEN), "",
				"");

		// 3.) Exchange the refresh token for an access token.
		s.accessToken = refreshAccessToken(refreshToken,
				accessData.getProperty(CONSUMER_KEY),
				accessData.getProperty(CONSUMER_SECRET),
				accessData.getProperty("callback"));
		accessData.put(ACCESS_TOKEN, s.accessToken.getToken());
		return s;
	}

	public static String getSharedLink(OAuthService service, Token accessToken,
			String path) {
		path = path.replace('\\', '/');
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		File f = new File(path);
		List<String> dirs = new LinkedList<String>();
		dirs.add(f.getName());
		while ((f = f.getParentFile()) != null) {
			dirs.add(0, f.getName());
		}
		String currentId = "me/skydrive";
		for (String dir : dirs) {
			String id = findFolderOrFileId(service, accessToken, currentId, dir);
			if (id == null) {
				currentId = null;
				System.out.println("Failed to find folder " + path);
				break;
			} else {
				currentId = id;
			}
		}
		if (currentId == null)
			return null;

		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(
				SHARED_LINK_URL, currentId));
		System.out.println("URL: " + request.getUrl());

		service.signRequest(accessToken, request);
		Response r = request.send();
		if (r.isSuccessful()) {
			System.out.println(r.getBody());
			return parseJSONProperty("link", r.getBody());
		} else {
			System.out.println("ERROR getting shared link!");
			System.out.println(r.getBody());
			System.out.println(r.getCode());
		}
		return null;
	}
	
	public static class Entry {
		private String name;
		private String id;
		private boolean isDirectory;
		private Date created;
		private Date modified;
		
		public Date getCreated() {
      return created;
    }
    public void setCreated(Date created) {
      this.created = created;
    }
    public Date getModified() {
      return modified;
    }
    public void setModified(Date modified) {
      this.modified = modified;
    }
    public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public boolean isDirectory() {
			return isDirectory;
		}
		public void setDirectory(boolean isDirectory) {
			this.isDirectory = isDirectory;
		}
	}
	
	public static List<Entry> getFolderContent(OAuthService service,
			Token accessToken, String folderId) {
		List<Entry> result = new ArrayList<Entry>();
		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(FOLDER_URL,
				folderId));
		service.signRequest(accessToken, request);
		// Request will look like this:
		// GET https://beta.apis.live.net/v5.0/FOLDER_ID/files
		Response r = request.send();
		if (r.isSuccessful()) {
			JSONArray array = parseJSONProperty("data", r.getBody());
			for (Object innerObj : array) {
				JSONObject item = (JSONObject) innerObj;
				// If the folder/file is within this directory, return its
				// id.
				Entry e = new Entry();
				e.setName((String) item.get("name"));
				
				String updatedTime = (String)item.get("updated_time");
				if (updatedTime != null) {
				  try {
				    e.setModified(ISO8601Format.parse(updatedTime));
				  } catch (Exception ex) {
				    ex.printStackTrace();
				  }
				}
				String createdTime = (String)item.get("created_time");
        if (createdTime != null) {
          try {
            e.setCreated(ISO8601Format.parse(createdTime));
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
				e.setId((String) item.get("id"));
				e.setDirectory(item.get("id").toString().startsWith("folder"));
				result.add(e);
			} // for
		} // if
		return result;
	}

	public static String findFolderOrFileId(OAuthService service,
			Token accessToken, String folderId, String folderName) {
		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(FOLDER_URL,
				folderId));
		service.signRequest(accessToken, request);
		// Request will look like this:
		// GET https://beta.apis.live.net/v5.0/FOLDER_ID/files
		Response r = request.send();
		if (r.isSuccessful()) {
			JSONArray array = parseJSONProperty("data", r.getBody());
			for (Object innerObj : array) {
				JSONObject item = (JSONObject) innerObj;
				// If the folder/file is within this directory, return its
				// id.
				String name = (String) item.get("name");
				if (name != null && name.equals(folderName)) {
					return (String) item.get("id");
				} // if
			} // for
		} // if
		return null;
	} // findFolderOrFileId
	
	public static String getUserId(OAuthService service, Token accessToken) {
	  OAuthRequest request = new OAuthRequest(Verb.GET, ME_URL);
	  service.signRequest(accessToken, request);
	  Response r = request.send();
	  if (r.isSuccessful()) {
	    String result = r.getBody();
	    String name = parseJSONProperty("name", r.getBody());
	    return name;
	  }
	  return null;
	}

	public static String createFolder(OAuthService service, Token accessToken,
			String parentId, String dir) {
	  HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(String.format(
					"https://apis.live.net/v5.0/%s?access_token=%s", parentId,
					accessToken.getToken())).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			StringBuffer content = new StringBuffer();
			content.append("{").append(CRLF).append(" \"name\": \"").append(dir)
					.append("\"").append(CRLF).append("}");

			byte[] contentData = content.toString().getBytes();
			connection.setDoOutput(true);

			OutputStream os = connection.getOutputStream();
			os.write(contentData);

			int code;
			connection.connect();
			code = connection.getResponseCode();
			if (code >= 200 && code <= 299) {
			  InputStream is = null;
			  try {
  				is = connection.getInputStream();
  				byte[] buffer = new byte[1024 * 1024];
  				int readBytes = 0;
  				StringBuffer sb = new StringBuffer();
  				while ((readBytes = is.read(buffer)) != -1) {
  					sb.append(new String(buffer, 0, readBytes));
  				}
  				String result = sb.toString();
  				return parseJSONProperty("id", result);
			  } finally {
			    try {
  			    if (is != null)
  			      is.close();
			    } catch (Exception ex) {
			      ex.printStackTrace();
			    }			    
			  }
			} // if
		} catch (Throwable e) {
			throw new PluginException(SkyDriveDescriptor.SKYDRIVE_ID,
					"An exception occurred during folder creation", e);
		} // try/catch
		finally {
		  try {
        connection.disconnect();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
		}
		return null;
	}

	public static boolean storeFile(OAuthService service, Token accessToken,
			InputStream data, String name) {
		name = name.replace('\\', '/');
		while (name.startsWith("/")) {
			name = name.substring(1);
		}

		File f = new File(name);
		String fileName = f.getName();

		List<String> dirs = new LinkedList<String>();
		while ((f = f.getParentFile()) != null) {
			dirs.add(0, f.getName());
		}

		String currentId = "me/skydrive";
		for (String dir : dirs) {
			String id = findFolderOrFileId(service, accessToken, currentId, dir);
			if (id == null) {
				currentId = createFolder(service, accessToken, currentId, dir);
			} else {
				currentId = id;
			}
		}
		HttpURLConnection connection = null;
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
			connection = (HttpURLConnection) new URL(String.format(
					"https://apis.live.net/v5.0/%s/files/%s?access_token=%s", currentId,
					fileName, accessToken.getToken())).openConnection();
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			byte[] buffer = new byte[1024 * 1024];
			int readBytes;
			while ((readBytes = data.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}

			int code;
			connection.connect();
			code = connection.getResponseCode();
			if (code < 200 || code > 299) {
				System.err.println("Error uploading file " + fileName);
				System.err.println("Code was: " + code);
				System.err.println("Message is: " + connection.getResponseMessage());
			}
			return code >= 200 && code <= 299;
		} catch (Throwable e) {
			throw new PluginException(SkyDriveDescriptor.SKYDRIVE_ID, 
				String.format(
					"An exception occurred while storing file %s", name), e);
		} // try/catch
		finally {
		  try {
		    connection.disconnect();
		  } catch (Exception ex) {
		    ex.printStackTrace();
		  }
		}
	} // storeFile

	public static InputStream readFile(OAuthService service, Token accessToken,
			String name) {
		String currentId = determineIdFor(true, service, accessToken, name);
		String content = getContentAsString(service, accessToken, currentId);
		ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
		return bais;
	}

	/**
	 * Retrieves a file from SkyDrive server.
	 * 
	 * @param service
	 *          The service that signs the requests
	 * @param accessToken
	 *          The access token of the user
	 * @param fileId
	 *          The file to download
	 * @return The response containing the file or null, if download failed.
	 */
	public static String getContentAsString(OAuthService service,
			Token accessToken, String fileId) {
		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(
				CONTENT_URL, fileId));
		service.signRequest(accessToken, request);
		// The request will look like this:
		// GET https://beta.apis.live.net/v5.0/file.ID/content
		Response response = request.send();
		if (response.isSuccessful()) {
			// The response contains a redirect location, looking like that:
			// http://storage.live.com/VERY_LONG_UNIQUE_ID/My%20Textfile.txt:Binary
			String location = response.getHeader("Location");
			request = new OAuthRequest(Verb.GET, location);
			response = request.send();
			if (response.isSuccessful()) {
				// this is the real content of the file				
				return response.getBody();
			} // if
		} // if
		return null;
	} // getContentAsString
	
	/**
	 * Retrieves a file from SkyDrive server.
	 * 
	 * @param service
	 *          The service that signs the requests
	 * @param accessToken
	 *          The access token of the user
	 * @param fileId
	 *          The file to download
	 * @return The response containing the file or null, if download failed.
	 */
	public static InputStream getContentAsStream(OAuthService service,
			Token accessToken, String fileId) {
		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(
				CONTENT_URL, fileId));
		service.signRequest(accessToken, request);
		// The request will look like this:
		// GET https://beta.apis.live.net/v5.0/file.ID/content
		Response response = request.send();
		if (response.isSuccessful()) {
			// The response contains a redirect location, looking like that:
			// http://storage.live.com/VERY_LONG_UNIQUE_ID/My%20Textfile.txt:Binary
			String location = response.getHeader("Location");
			if (location == null)
			  location = response.getHeader("Content-Location");
			request = new OAuthRequest(Verb.GET, location);
			response = request.send();
			if (response.isSuccessful()) {
				// this is the real content of the file							
				return response.getStream();
			} // if
		} // if
		return null;
	} // getContentAsString

	private static String determineIdFor(boolean useHead, OAuthService service,
			Token accessToken, String name) {
		name = name.replace('\\', '/');
		while (name.startsWith("/")) {
			name = name.substring(1);
		}

		File f = new File(name);
		String fileName = f.getName();

		List<String> dirs = new LinkedList<String>();
		if (useHead)
			dirs.add(fileName);
		while ((f = f.getParentFile()) != null) {
			dirs.add(0, f.getName());
		}

		String currentId = "me/skydrive";
		for (String dir : dirs) {
			String id = findFolderOrFileId(service, accessToken, currentId, dir);
			if (id == null) {
				return null;
			} else {
				currentId = id;
			}
		}
		return currentId;
	}

	public static void removeFile(OAuthService service, Token accessToken,
			String name) {
		String currentId = determineIdFor(true, service, accessToken, name);

		if (!"me/skydrive".equals(currentId)) {
		  HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) new URL(
						String.format("https://apis.live.net/v5.0/%s?access_token=%s",
								currentId, accessToken.getToken())).openConnection();
				connection.setRequestMethod("DELETE");
				connection.connect();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			  try { 
			    connection.disconnect();			    
			  } catch (Exception ex) {
			    ex.printStackTrace();
			  }
			}
		}
	}

	public static long getFileSize(OAuthService service, Token accessToken,
			String name) {
		String fileId = determineIdFor(true, service, accessToken, name);
		if (fileId == null) {
			throw new RuntimeException("getFileSize: fileId may not be null");
		}
		OAuthRequest request = new OAuthRequest(Verb.GET, String.format(
				"https://apis.live.net/v5.0/%s", fileId));
		service.signRequest(accessToken, request);
		Response resp = request.send();
		if (resp.isSuccessful()) {
			Long size = parseJSONProperty("size", resp.getBody());
			return size;
		}
		return 0L;
	}

	@SuppressWarnings("unchecked")
	private static <T> T parseJSONProperty(String property, String content) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(content);
			return (T) obj.get(property);
		} catch (ParseException e) {
			throw new PluginException(SkyDriveDescriptor.SKYDRIVE_ID, String.format(
					"Unable to find JSON Property %s in content %s", property, content),
					e);
		} // catch
	}

	/**
	 * Refreshes the access token with a refresh token
	 * 
	 * @param refreshToken
	 *          The refresh token for a specific user.
	 * @return The refreshed access token for the user.
	 */
	public static Token refreshAccessToken(Token refreshToken,
			String consumerKey, String consumerSecret, String redirectUrl) {
		// Create refresh url for windows live api.
		// It will look something like that:
		// https://oauth.live.com/token?client_id=YOURID&
		// client_secret=YOURSECRET&
		// redirect_uri=YOURURL&
		// grant_type=refresh_token&
		// refresh_token=YOURREFRESHTOKEN
		String url = String.format(REFRESH_URL, consumerKey, consumerSecret,
				redirectUrl, refreshToken.getToken());
		// Create a request based on that url.
		// System.out.println("Refresh URL: " + url);
		OAuthRequest accessTokenByRefreshToken = new OAuthRequest(Verb.GET, url);
		Response result = accessTokenByRefreshToken.send();
		if (result.isSuccessful()) {
			// The access token will be a part of the body within the response.
			// Parse it and return the access token
			return SkyDriveSupport.parseAccessToken(result.getBody());
		} else {
			throw new PluginException(
					SkyDriveDescriptor.SKYDRIVE_ID,
					String
							.format(
									"Failed to get access token for refresh token %s, consumerKey %s, consumerSecret %s and redirectUrl %s!",
									refreshToken, consumerKey, consumerSecret, redirectUrl));
		} // else
	} // refreshAccessToken

	/**
	 * Parses the access_token out of the body of a refresh response.
	 * 
	 * @param body
	 *          The body of a refresh response
	 * @return The access token or null, if none found
	 */
	public static Token parseAccessToken(String body) {
		String access_token = parseJSONProperty("access_token", body);
		return new Token(access_token, "", body);
	} // parseAccessToken

	/**
	 * This method actually parses the rawResponse of a token request and searches
	 * for the refresh token.
	 * 
	 * @param rawResponse
	 *          The refresh token will be searched within this response
	 * @return the refresh token or null, if none found
	 */
	public static Token parseRefreshToken(String rawResponse) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(rawResponse);
			String refresh_token = (String) obj.get("refresh_token");
			return new Token(refresh_token, "", rawResponse);
		} catch (ParseException e) {
			throw new PluginException(SkyDriveDescriptor.SKYDRIVE_ID, String.format(
					"Couldn't parse response %s", rawResponse), e);
		}
	} // parseRefreshToken
}
