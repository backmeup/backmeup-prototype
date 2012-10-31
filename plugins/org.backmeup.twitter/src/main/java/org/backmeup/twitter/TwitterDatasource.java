package org.backmeup.twitter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ecs.Document;
import org.apache.ecs.html.A;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.H2;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import twitter4j.AccountTotals;
import twitter4j.MediaEntity;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
//import org.apache.ecs.xhtml.br;

/**
 * class TwitterDatasource to download the whole user profile including
 * home-timeline, lists, retweets and favourites
 * 
 * @author mmurauer
 */
public class TwitterDatasource implements Datasource {
	private static final String TWITTER = "twitter";

	private List<Status> states = new LinkedList<Status>();
	private List<Long> retweets = new LinkedList<Long>();
	private User user = null;

	@Override
	public void downloadAll(Properties arg0, List<String> options, Storage arg1,
			Progressable arg2) throws DatasourceException, StorageException {

		// create new access token
		AccessToken at = new AccessToken(arg0.getProperty("token"),
				arg0.getProperty("secret"));

		// create new Twitter Instance with generated access token
		TwitterHelper th = TwitterHelper.getInstance();

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(th.getAppKey())
				.setOAuthConsumerSecret(th.getAppSecret())
				.setOAuthAccessToken(at.getToken())
				.setOAuthAccessTokenSecret(at.getTokenSecret());
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		arg2.progress("Download Benutzer-Information...");
		String document = downloadUser(twitter, arg1);
		arg2.progress("Download Retweets von meinen Tweets...");
		downloadSimpleTable(twitter, "RetweetsOfMe", arg1);
		arg2.progress("Download Retweets an mich...");
		downloadSimpleTable(twitter, "RetweetsToMe", arg1);
		arg2.progress("Download Retweets von mir...");
		downloadSimpleTable(twitter, "RetweetsByMe", arg1);

		// to create Timeline-Metadata retweets are needed
		createUser(document, arg1);

		arg2.progress("Download Favouriten...");
		downloadSimpleTable(twitter, "Favorites", arg1);
		arg2.progress("Download Benutzer-Listen...");
		downloadLists(twitter, arg1);

		/*try {
			System.out.println(twitter.getRateLimitStatus().getRemainingHits());
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

	@Override
	public String getStatistics(Properties arg0) {
		// TO DO
		return null;
	}

	/**
	 * create link in the text, if text contains "http"
	 * 
	 * @param text
	 *            a tweet contains
	 * @return text with html-link
	 */
	private String createLink(String text) {
		List<String> result = new ArrayList<String>();

        Pattern pattern = Pattern.compile(
            "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" + 
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" + 
            "|mil|biz|info|mobi|name|aero|jobs|museum" + 
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" + 
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" + 
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" + 
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" + 
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        for(String link : result){
        	text = text.replace(link, "<a target='_blank' href='"+link+"'>"+link+"</a>");
        }
        
        return text;
	}

	private Document createDocument(String title, String header) {
		Document doc = (Document) new Document();
		doc.appendHead("<meta http-equiv='content-type' content='text/html; charset=UTF-8' />");

		doc.appendTitle(title);
		doc.appendBody(new Table().addElement(
				new TD().addElement(new IMG(new TwitterDescriptor()
						.getImageURL()).setHeight(50).setWidth(50)))
				.addElement(new TD().addElement(new H1(header))));
		return doc;
	}

	private Metainfo statusMetadata(Status status, String type) {
		Metainfo metainfo = new Metainfo();

		metainfo.setAttribute("authorName", status.getUser().getName());
		metainfo.setAttribute("authorScreenName", status.getUser()
				.getScreenName());
		metainfo.setAttribute("text", status.getText());
		metainfo.setBackupDate(new Date());
		metainfo.setDestination(type + ".html#" + status.getId());
		metainfo.setId(Long.toString(status.getId()));
		metainfo.setModified(status.getCreatedAt());
		metainfo.setSource(TWITTER);
		metainfo.setType("tweet");

		return metainfo;
	}

	private String extractMedia(Status state, String parent,
			Storage storage) {
		try {
			MediaEntity[] media = state.getMediaEntities();
			for (MediaEntity m : media) {
				URL url = m.getMediaURL();
				int indexDot = url.toString().lastIndexOf('.');
				String extension = url.toString().substring(indexDot);

				// check if URL-content exists
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();

				if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
					MetainfoContainer metadata = new MetainfoContainer();
					Metainfo metainfo = new Metainfo();

					metainfo.setAttribute("parenturl", parent + ".html#"
							+ state.getId());
					metainfo.setBackupDate(new Date());
					metainfo.setDestination(m.getId() + extension);
					metainfo.setId(Long.toString(m.getId()));
					metainfo.setParent(Long.toString(state.getId()));
					metainfo.setSource(TWITTER);
					metainfo.setType("image");

					metadata.addMetainfo(metainfo);

					InputStream is = url.openStream();
					storage.addFile(is, m.getId() + extension, metadata);

					return m.getId() + extension;

				}
			}
		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while extraction media entities", e);
		}
		return "";
	}

	/**
	 * create timeline-file and MetainfoContainer of user
	 * 
	 * @param storage
	 */
	private void createUser(String document, Storage storage) {
		try {
			MetainfoContainer metadata = new MetainfoContainer();

			URL url = user.getProfileImageURL();
			int indexDot = url.toString().lastIndexOf('.');
			String extension = url.toString().substring(indexDot);

			Metainfo userInfo = new Metainfo();

			userInfo.setAttribute("name", user.getName());
			userInfo.setAttribute("screenName", user.getScreenName());
			userInfo.setAttribute("profileImage", "profileImage" + extension);
			userInfo.setBackupDate(new Date());
			userInfo.setDestination("index.html");
			userInfo.setId(Long.toString(user.getId()));
			userInfo.setModified(user.getCreatedAt());
			userInfo.setSource(TWITTER);
			userInfo.setType("user");

			metadata.addMetainfo(userInfo);

			Metainfo tweetInfo = new Metainfo();

			// create metadata for each state, seperate Tweets and Retweets
			for (Status state : states) {
				tweetInfo = statusMetadata(state, "index");
				if (retweets.contains(state.getId())) {
					tweetInfo.setParent(Long.toString(state
							.getRetweetedStatus().getId()));
					tweetInfo.setAttribute("sourceName", state
							.getRetweetedStatus().getUser().getName());
					tweetInfo.setAttribute("sourceScreenName", state
							.getRetweetedStatus().getUser().getScreenName());
					tweetInfo.setType("retweet");
				}
				metadata.addMetainfo(tweetInfo);
			}

			InputStream is = new ByteArrayInputStream(document.getBytes());
			storage.addFile(is, "index.html", metadata);

		} catch (StorageException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"A twitter-error occurred while creating User-File", e);
		}
	}

	private String downloadUser(Twitter twitter, Storage storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			user = twitter.showUser(twitter.getId());

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> timeline = twitter.getHomeTimeline(paging);

			// create HTML timeline+userID.html
			Document doc = createDocument("Index", "Twitter - Benutzer");
			
			Date d = new Date();
			doc.appendBody(d.toString());
			doc.appendBody(new BR());
			doc.appendBody(new BR());

			doc.appendBody("Benutzername: " + user.getName());
			doc.appendBody(new BR());
			doc.appendBody(new BR());

			// save profile image in extra file (profileImage.extension) if
			// exists
			URL url = user.getProfileImageURL();
			int indexDot = url.toString().lastIndexOf('.');
			String extension = url.toString().substring(indexDot);

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				MetainfoContainer metadata = new MetainfoContainer();

				Metainfo metainfo = new Metainfo();
				metainfo.setAttribute("url", url.toString());
				metainfo.setBackupDate(new Date());
				metainfo.setDestination("profileImage" + extension);
				metainfo.setId("profileImage");
				metainfo.setSource(TWITTER);
				metainfo.setType("image");
				metadata.addMetainfo(metainfo);

				InputStream is = url.openStream();
				storage.addFile(is, "profileImage" + extension, metadata);

				IMG img = new IMG("profileImage" + extension);
				doc.appendBody(img);
			}

			doc.appendBody(new BR());
			doc.appendBody(new BR());

			AccountTotals acct = twitter.getAccountTotals();
			doc.appendBody("Freund(e): " + acct.getFriends() + " Follower(s): "
					+ acct.getFollowers());
			doc.appendBody(new BR());
			doc.appendBody(new A("Favorites.html", "Favourite(n)"));
			doc.appendBody(acct.getFavorites() + " Update(s): "
					+ acct.getUpdates());
			doc.appendBody(new BR());
			doc.appendBody(new A("RetweetsToMe.html", "Retweets an mich   "));
			doc.appendBody(new BR());
			doc.appendBody(new A("RetweetsOfMe.html", "   Retweets von meinen Tweets   "));
			doc.appendBody(new BR());
			doc.appendBody(new A("RetweetsByMe.html", "   Retweets von mir"));
			doc.appendBody(new BR());
			
			doc.appendBody(new H2("Benutzer-Listen"));

			long cursor = -1;
			PagableResponseList lists;

			do {
				lists = twitter.getUserLists(user.getId(), cursor);
				for (Object l : lists) {
					UserList ul = (UserList) l;
					doc.appendBody(new A("list" + ul.getId() + ".html", ul
							.getFullName()));
				}
			} while ((cursor = lists.getNextCursor()) != 0);

			doc.appendBody(new H2("Home-Timeline"));

			TR tr = null;
			TD td = null;
			Table timeTable = new Table();
			timeTable.setBorder(3);
			Status lastState = null;

			while (timeline.size() > 1) {
				for (Status state : timeline) {
					states.add(state);

					String text = createLink(state.getText());
					tr = new TR();
					td = new TD(state.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + state.getUser().getScreenName());
					tr.addElement(td);
					td = new TD("<a name = '" + state.getId() + "'>" + text
							+ "</a>");
					tr.addElement(td);

					timeTable.addElement(tr);
					lastState = state;

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state, "index", storage);
						if (media != null) {
							td = new TD("<a href = "+media+" target='_blank' > bild </a>");
							tr.addElement(td);
						}
					}

				}
				paging.setMaxId(lastState.getId());
				timeline = twitter.getHomeTimeline(paging);

			}
			doc.appendBody(timeTable);

			return doc.toString();

		} catch (TwitterException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"A twitter-error occurred while download User-Information",
					e);
		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download User-Information", e);
		}
	}

	private void downloadSimpleTable(Twitter twitter, String type,
			Storage storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			MetainfoContainer metadata = new MetainfoContainer();

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> download = getTypeStates(twitter, type, paging);

			String typeText = "";
			if(type.equals("RetweetsOfMe")) typeText = "Retweets von meinen Tweets";
			if(type.equals("RetweetsToMe")) typeText = "Retweets an mich";
			if(type.equals("RetweetsByMe")) typeText = "Retweets von mir";
			if(type.equals("Favorites")) typeText = "Favouriten";
			// create HTML type.html
			Document doc = createDocument(type, "Twitter - " + typeText);

			doc.appendBody(new H2("<a name = '#test'>Die letzten </a> " + typeText
					+ " (maximal 3200)"));

			TR tr = null;
			TD td = null;
			Table table = new Table();
			table.setBorder(3);
			Status lastState = null;

			while (download.size() >= 1) {
				for (Status state : download) {

					Metainfo metainfo = statusMetadata(state, type);

					String text = createLink(state.getText());
					tr = new TR();
					td = new TD();
					if ((type.equals("RetweetsByMe") || (type
							.equals("RetweetsToMe")))) {
						retweets.add(state.getId());
						Status source = state.getRetweetedStatus();

						metainfo.setParent(Long.toString(source.getId()));
						metainfo.setAttribute("sourceName", source.getUser()
								.getName());
						metainfo.setAttribute("sourceScreenName", source
								.getUser().getScreenName());
						metainfo.setType("retweet");

						if (states.contains(source)) {
							td.addElement(new A("index.html#" + source.getId(),
									"zum Quell-Tweet"));
						}
					} else {
						if(type.equals("RetweetsOfMe")) metainfo.setType("retweet");
						else metainfo.setType("favourit");
						if (states.contains(state)) {
							td.addElement(new A("index.html#" + state.getId(),
									"zum Quell-Tweet"));
						}
					}

					metadata.addMetainfo(metainfo);

					tr.addElement(td);
					td = new TD(state.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + state.getUser().getScreenName());
					tr.addElement(td);
					td = new TD("<a name = '" + state.getId() + "'>" + text
							+ "</a>");
					tr.addElement(td);

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state, type, storage);
						if (!media.equals("")) {
							td = new TD("<a href = "+media+" target='_blank' > bild </a>");
							tr.addElement(td);
						}
					}

					table.addElement(tr);
					lastState = state;
				}
				paging.setMaxId(lastState.getId());
				download = getTypeStates(twitter, type, paging);
				if (download.size() == 1)
					download.remove(0);
			}

			doc.appendBody(table);

			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, type + ".html", metadata);

		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while downloading " + type, e);
		}
	}

	private List<Status> getTypeStates(Twitter twitter, String type,
			Paging paging) {
		List<Status> download = new LinkedList<Status>();
		try {
			if (type.equals("Favorites"))
				download = twitter.getFavorites(paging);
			else if (type.equals("RetweetsToMe"))
				download = twitter.getRetweetedToMe(paging);
			else if (type.equals("RetweetsByMe"))
				download = twitter.getRetweetedByMe(paging);
			else if (type.equals("RetweetsOfMe"))
				download = twitter.getRetweetsOfMe(paging);

			return download;
		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while downloading " + type, e);
		}
	}

	private void downloadList(Twitter twitter, int listId, Storage storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			MetainfoContainer metadata = new MetainfoContainer();

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			UserList list = twitter.showUserList(listId);

			Metainfo listInfo = new Metainfo();
			listInfo.setAttribute("name", list.getName());
			listInfo.setAttribute("fullname", list.getFullName());
			listInfo.setAttribute("description", list.getDescription());
			listInfo.setBackupDate(new Date());
			listInfo.setDestination("list" + list.getId() + ".html");
			listInfo.setId(Long.toString(list.getId()));
			listInfo.setSource(TWITTER);
			listInfo.setType("list");

			metadata.addMetainfo(listInfo);

			// create HTML list+listID.html
			Document doc = createDocument("Liste" + listId, "Twitter - Liste");

			doc.appendBody("Listenname: " + list.getFullName());
			doc.appendBody(new BR());
			doc.appendBody("Beschreibung: " + list.getDescription());
			doc.appendBody(new BR());
			doc.appendBody("Mitglied(er): " + list.getMemberCount());
			doc.appendBody(new BR());
			doc.appendBody("Teilnehmer: " + list.getSubscriberCount());

			doc.appendBody(new BR());
			doc.appendBody(new BR());

			long cursor = -1;
			PagableResponseList users;
			TR tr = null;
			TD td = null;

			if (list.getMemberCount() > 0) {
				doc.appendBody(new H2("Miglieder: "));
				Table memberTable = new Table();
				memberTable.setBorder(3);

				do {
					users = twitter.getUserListMembers(listId, cursor);
					tr = new TR();
					for (Object objUser : users) {
						User user = (User) objUser;
						td = new TD("@" + user.getScreenName());
						tr.addElement(td);
					}
					memberTable.addElement(tr);
				} while ((cursor = users.getNextCursor()) != 0);

				doc.appendBody(memberTable);
				doc.appendBody(new BR());
			}

			if (list.getSubscriberCount() > 0) {
				doc.appendBody(new H2("Teilnehmer: "));

				cursor = -1;
				Table subscriberTable = new Table();
				subscriberTable.setBorder(3);

				do {
					users = twitter.getUserListSubscribers(listId, cursor);
					tr = new TR();
					for (Object objUser : users) {
						User user = (User) objUser;
						td = new TD("@" + user.getScreenName());
						tr.addElement(td);
					}
					subscriberTable.addElement(tr);
				} while ((cursor = users.getNextCursor()) != 0);

				doc.appendBody(subscriberTable);
				doc.appendBody(new BR());
			}
			List<Status> states = twitter.getUserListStatuses(listId, paging);

			Table stateTable = new Table();
			stateTable.setBorder(3);
			Status lastState = null;

			while (states.size() > 1) {
				for (Status state : states) {

					Metainfo metainfo = statusMetadata(state,
							"list" + list.getId());

					if (retweets.contains(state.getId())) {
						metainfo.setParent(Long.toString(state
								.getRetweetedStatus().getId()));
						metainfo.setAttribute("sourceName", state
								.getRetweetedStatus().getUser().getName());
						metainfo.setAttribute("sourceScreenName", state
								.getRetweetedStatus().getUser().getScreenName());
						metainfo.setType("retweet");
					}

					metadata.addMetainfo(metainfo);

					String text = createLink(state.getText());

					tr = new TR();
					td = new TD(state.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + state.getUser().getScreenName());
					tr.addElement(td);
					td = new TD("<a name = '" + state.getId() + "'>" + text
							+ "</a>");
					tr.addElement(td);
					stateTable.addElement(tr);
					lastState = state;

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state,
								"list" + list.getId(), storage);
						if (!media.equals("")) {
							td = new TD("<a href = "+media+" target='_blank' > bild </a>");
							tr.addElement(td);
						}
					}
				}
				paging.setMaxId(lastState.getId());
				states = twitter.getUserListStatuses(listId, paging);
			}

			doc.appendBody(stateTable);

			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, "list" + listId + ".html", metadata);

		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Lists", e);
		}
	}

	private void downloadLists(Twitter twitter, Storage storage) {
		try {
			List<UserList> lists = twitter.getAllUserLists(user.getId());
			for (UserList l : lists) {
				downloadList(twitter, l.getId(), storage);
			}
		} catch (TwitterException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Lists", e);
		}
	}

	@Override
	public List<String> getAvailableOptions(Properties accessData) {
		List<String> twitterBackupOptions = new ArrayList<String>();
		twitterBackupOptions.add("Profile");
		twitterBackupOptions.add("RetweetsToMe");
		twitterBackupOptions.add("RetweetsByMe");
		twitterBackupOptions.add("RetweetsOfMe");
		twitterBackupOptions.add("Favourites");
		twitterBackupOptions.add("Lists");
		return twitterBackupOptions;
	}
}
