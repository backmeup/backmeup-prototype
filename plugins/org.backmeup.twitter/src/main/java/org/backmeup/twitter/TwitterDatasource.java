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

import org.apache.ecs.Document;
import org.apache.ecs.html.A;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.H2;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.xhtml.br;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

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
	public void downloadAll(Properties arg0, StorageWriter arg1,
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

		arg2.progress("Downloading User-Information...");
		String document = downloadUser(twitter, arg1);
		arg2.progress("Downloading Retweets of me...");
		downloadSimpleTable(twitter, "RetweetsOfMe", arg1);
		arg2.progress("Downloading Retweets to me...");
		downloadSimpleTable(twitter, "RetweetsToMe", arg1);
		arg2.progress("Downloading Retweets by me...");
		downloadSimpleTable(twitter, "RetweetsByMe", arg1);

		// to create Timeline-Metadata retweets are needed
		createUser(document, arg1);

		arg2.progress("Downloading Favourites...");
		downloadSimpleTable(twitter, "Favorites", arg1);
		arg2.progress("Downloading User-Lists...");
		downloadLists(twitter, arg1);

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
		StringBuffer textLink = new StringBuffer(text);
		if (text.contains("http")) {
			textLink.insert(text.indexOf("http"), "<a href=");
			String link = text.substring(text.indexOf("http"));
			textLink.append(">" + link + "</a>");
		}

		return textLink.toString();
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
			StorageWriter storage) {
		try {
			MediaEntity[] media = state.getMediaEntities();
			for (MediaEntity m : media) {
				URL url = m.getMediaURL();
				String extension = url.toString().substring(
						url.toString().length() - 4);

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
	private void createUser(String document, StorageWriter storage) {
		try {
			MetainfoContainer metadata = new MetainfoContainer();

			URL url = user.getProfileImageURL();
			String extension = url.toString().substring(
					url.toString().length() - 4);

			Metainfo userInfo = new Metainfo();

			userInfo.setAttribute("name", user.getName());
			userInfo.setAttribute("screenName", user.getScreenName());
			userInfo.setAttribute("profileImage", "profileImage" + extension);
			userInfo.setBackupDate(new Date());
			userInfo.setDestination("timeline" + user.getId() + ".html");
			userInfo.setId(Long.toString(user.getId()));
			userInfo.setModified(user.getCreatedAt());
			userInfo.setSource(TWITTER);
			userInfo.setType("user");

			metadata.addMetainfo(userInfo);

			Metainfo tweetInfo = new Metainfo();

			// create metadata for each state, seperate Tweets and Retweets
			for (Status state : states) {
				tweetInfo = statusMetadata(state, "timeline");
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
			storage.addFile(is, "timeline" + user.getId() + ".html", metadata);

		} catch (StorageException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"A twitter-error occurred while creating User-File", e);
		}
	}

	private String downloadUser(Twitter twitter, StorageWriter storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			user = twitter.showUser(twitter.getId());

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> timeline = twitter.getHomeTimeline(paging);

			// create HTML timeline+userID.html
			Document doc = (Document) new Document().appendTitle(
					"Timeline" + user.getId()).appendBody(
					new Table().addElement(
							new TD().addElement(new IMG(desc.getImageURL())
									.setHeight(50).setWidth(50))).addElement(
							new TD().addElement(new H1("Twitter - User"))));

			doc.appendBody("Username: " + user.getName());
			doc.appendBody(new br());
			doc.appendBody("UserID: " + user.getId());
			doc.appendBody(new br());
			doc.appendBody(new br());

			// save profile image in extra file (profileImage.extension) if
			// exists
			URL url = user.getProfileImageURL();
			String extension = url.toString().substring(
					url.toString().length() - 4);

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// con.setRequestMethod("HEAD");
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

			doc.appendBody(new br());
			doc.appendBody(new br());

			AccountTotals acct = twitter.getAccountTotals();
			doc.appendBody("Friend(s): " + acct.getFriends() + " Follower(s): "
					+ acct.getFollowers());
			doc.appendBody(new br());
			doc.appendBody(new A("Favorites.html", "Favourit(en)"));
			doc.appendBody(acct.getFavorites() + " Update(s): "
					+ acct.getUpdates());
			doc.appendBody(new br());
			doc.appendBody(new A("RetweetsToMe.html", "Retweets to me "));
			doc.appendBody(new A("RetweetsOfMe.html", "Retweets of me "));
			doc.appendBody(new A("RetweetsByMe.html", "Retweets by me "));

			doc.appendBody(new H2("User-Listen"));

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
						String media = extractMedia(state,
								"timeline" + user.getId(), storage);
						if (media != null) {
							td = new TD(new A(media, "media"));
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
			StorageWriter storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			MetainfoContainer metadata = new MetainfoContainer();

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> download = getTypeStates(twitter, type, paging);

			// create HTML type.html
			Document doc = (Document) new Document().appendTitle(type)
					.appendBody(
							new Table().addElement(
									new TD().addElement(new IMG(desc
											.getImageURL()).setHeight(50)
											.setWidth(50))).addElement(
									new TD().addElement(new H1("Twitter - "
											+ type))));

			doc.appendBody(new H2("<a name = '#test'>Your last </a> " + type
					+ "(maximum of 3200)"));

			TR tr = null;
			TD td = null;
			Table table = new Table();
			table.setBorder(3);
			Status lastState = null;

			while (download.size() > 1) {
				for (Status state : download) {

					Metainfo metainfo = statusMetadata(state, type);

					String text = createLink(state.getText());
					tr = new TR();
					td = new TD();

					if (!(type.equals("Favorites"))) {
						retweets.add(state.getId());
						Status source = state.getRetweetedStatus();

						metainfo.setParent(Long.toString(source.getId()));
						metainfo.setAttribute("sourceName", source.getUser()
								.getName());
						metainfo.setAttribute("sourceScreenName", source
								.getUser().getScreenName());
						metainfo.setType("retweet");

						if (states.contains(source)) {
							td.addElement(new A("timeline" + user.getId()
									+ ".html#" + source.getId(),
									"go to Source-Tweet"));
						}
					} else {
						metainfo.setType("favourit");
						if (states.contains(state)) {
							td.addElement(new A("timeline" + user.getId()
									+ ".html#" + state.getId(),
									"go to Source-Tweet"));
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
							td = new TD(new A(media, "media"));
							tr.addElement(td);
						}
					}

					table.addElement(tr);
					lastState = state;
				}
				paging.setMaxId(lastState.getId());
				download = getTypeStates(twitter, type, paging);
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

	private void downloadList(Twitter twitter, int listId, StorageWriter storage) {
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
			Document doc = (Document) new Document().appendTitle(
					"List" + listId).appendBody(
					new Table().addElement(
							new TD().addElement(new IMG(desc.getImageURL())
									.setHeight(50).setWidth(50))).addElement(
							new TD().addElement(new H1("Twitter - List"))));

			doc.appendBody("Listname: " + list.getFullName());
			doc.appendBody(new br());
			doc.appendBody("ListID: " + listId);
			doc.appendBody(new br());
			doc.appendBody("Beschreibung: " + list.getDescription());
			doc.appendBody(new br());
			doc.appendBody("Member(s): " + list.getMemberCount());
			doc.appendBody(new br());
			doc.appendBody("Subscriber(s): " + list.getSubscriberCount());

			doc.appendBody(new br());
			doc.appendBody(new br());

			doc.appendBody(new H2("Members: "));
			long cursor = -1;
			PagableResponseList users;
			TR tr = null;
			TD td = null;
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

			doc.appendBody(new H2("Subscribers: "));

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
							td = new TD(new A(media, "media"));
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

	private void downloadLists(Twitter twitter, StorageWriter storage) {
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
    //TODO: Return a list of selectable resources that should be backed up
    List<String> availableOptions = new ArrayList<String>();
    return availableOptions;
  }
}
