package org.backmeup.twitter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.ecs.Document;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.H2;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.xhtml.br;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

import twitter4j.AccountTotals;
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
	private long twitterUserId = 0;
	private List<Long> states = new LinkedList<Long>();

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

		try {

			twitterUserId = twitter.getId();

			arg2.progress("Downloading User-Information...");
			downloadUser(twitter, arg1);
			arg2.progress("Downloading Favourites...");
			downloadSimpleTable(twitter, "Favorites", arg1);
			arg2.progress("Downloading RetweetsOfMe...");
			downloadSimpleTable(twitter, "RetweetsOfMe", arg1);
			arg2.progress("Downloading RetweetsToMe...");
			downloadSimpleTable(twitter, "RetweetsToMe", arg1);
			arg2.progress("Downloading RetweetsByMe...");
			downloadSimpleTable(twitter, "RetweetsByMe", arg1);
			arg2.progress("Downloading User-Lists...");
			downloadLists(twitter, arg1);

		} catch (TwitterException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Twitter-Profile", e);
		}
	}

	@Override
	public String getStatistics(Properties arg0) {
		System.out.println(arg0.toString());
		return null;
	}

	/**
	 * create link in the text, if text contains "http"
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

	private void downloadUser(Twitter twitter, StorageWriter storage) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			User user = twitter.showUser(twitterUserId);

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

			doc.appendBody(new IMG(user.getProfileImageURL().toString()));

			doc.appendBody(new br());
			doc.appendBody(new br());

			AccountTotals acct = twitter.getAccountTotals();
			doc.appendBody("Friend(s): " + acct.getFriends() + " Follower(s): "
					+ acct.getFollowers());
			doc.appendBody(new br());
			doc.appendBody("<a href = 'Favorites.html'>Favourit(en)</a>: "
					+ acct.getFavorites() + " Update(s): " + acct.getUpdates());
			doc.appendBody(new br());
			doc.appendBody("<a href = 'RetweetsToMe.html'>RetweetsToMe</a>   ");
			doc.appendBody("<a href = 'RetweetsOfMe.html'>RetweetsOfMe</a>   ");
			doc.appendBody("<a href = 'RetweetsByMe.html'>RetweetsByMe</a>   ");

			doc.appendBody(new H2("User-Listen"));

			long cursor = -1;
			PagableResponseList lists;

			do {
				lists = twitter.getUserLists(user.getId(), cursor);
				for (Object l : lists) {
					UserList ul = (UserList) l;
					doc.appendBody("<a href= 'list" + ul.getId() + ".html'>"
							+ ul.getFullName() + "</a>");
				}
			} while ((cursor = lists.getNextCursor()) != 0);

			doc.appendBody(new H2("Home-Timeline"));

			TR tr = null;
			TD td = null;
			Table timeTable = new Table();
			timeTable.setBorder(3);
			Status lastState = null;

			while (timeline.size() > 1) {
				for (Status status : timeline) {
					states.add(status.getId());
					String text = createLink(status.getText());
					tr = new TR();
					td = new TD(status.getCreatedAt().toString() + " "
							+ status.getId());
					tr.addElement(td);
					td = new TD("@" + status.getUser().getScreenName());
					tr.addElement(td);
					td = new TD("<a name = '" + status.getId() + "'>" + text
							+ "</a>");
					tr.addElement(td);
					timeTable.addElement(tr);
					lastState = status;
				}
				paging.setMaxId(lastState.getId());
				timeline = twitter.getHomeTimeline(paging);
			}

			doc.appendBody(timeTable);
			
			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, "timeline" + user.getId() + ".html");

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
			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> download = getTypeStates(twitter, type, paging);
			
			//create HTML type.html
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
				for (Status status : download) {
					String text = createLink(status.getText());
					tr = new TR();
					td = new TD();

					if (!(type.equals("Favorites"))) {
						Status source = status.getRetweetedStatus();
						if (states.contains(source.getId())) {
							td.addElement("<a href = 'timeline" + twitterUserId
									+ ".html#" + source.getId()
									+ "'> go to Source-Tweet </a>");
						}
					} else {
						if (states.contains(status.getId())) {
							td.addElement("<a href = 'timeline" + twitterUserId
									+ ".html#" + status.getId()
									+ "'> go to Source-Tweet </a>");
						}
					}

					tr.addElement(td);
					td = new TD(status.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + status.getUser().getScreenName());
					tr.addElement(td);
					td = new TD(text);
					tr.addElement(td);
					table.addElement(tr);
					lastState = status;
				}
				paging.setMaxId(lastState.getId());
				download = getTypeStates(twitter, type, paging);
			}

			doc.appendBody(table);
			
			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, type + ".html");

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
			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			UserList list = twitter.showUserList(listId);

			//create HTML list+listID.html
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
				for (Status status : states) {
					String text = createLink(status.getText());

					tr = new TR();
					td = new TD(status.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + status.getUser().getScreenName());
					tr.addElement(td);
					td = new TD(text);
					tr.addElement(td);
					stateTable.addElement(tr);
					lastState = status;
				}
				paging.setMaxId(lastState.getId());
				states = twitter.getUserListStatuses(listId, paging);
			}
			
			doc.appendBody(stateTable);
			
			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, "list" + listId + ".html");

		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Lists", e);
		}
	}

	private void downloadLists(Twitter twitter, StorageWriter storage){
		try {
			List<UserList> lists = twitter.getAllUserLists(twitterUserId);
			for (UserList l : lists) {
				downloadList(twitter, l.getId(), storage);
			}
		} catch (TwitterException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Lists", e);
		}
	}

}
