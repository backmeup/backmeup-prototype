package org.backmeup.twitter;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Document;
import org.apache.ecs.html.A;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.H2;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.LI;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.UL;
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
	private ConcreteElement ce = new ConcreteElement();
	private String list_point = "Themes/list_point.jpg";

	@Override
	public void downloadAll(Properties arg0, List<String> options,
			Storage arg1, Progressable arg2) throws DatasourceException,
			StorageException {

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

		getThemes(arg1, arg0);

		Document doc = createDocument("Index", "Twitter");
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		arg2.progress("Download Benutzer-Information...");
		String document = downloadUser(twitter, arg1, options);

		A profile = new A("profile.html", "Profil");
		profile.addAttribute("class", "navbutton");
		ul.addElement(new LI().addElement(profile));

		if (options.contains("RetweetsOfMe")) {
			A retweetsofme = new A("RetweetsOfMe.html",
					"Retweets von meinen Tweets");
			retweetsofme.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(retweetsofme));

			arg2.progress("Download Retweets von meinen Tweets...");
			downloadSimpleTable(twitter, "RetweetsOfMe", arg1);
		}

		/*if (options.contains("RetweetsToMe")) {
			A retweetstome = new A("RetweetsToMe.html", "Retweets an mich");
			retweetstome.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(retweetstome));

			arg2.progress("Download Retweets an mich...");
			downloadSimpleTable(twitter, "RetweetsToMe", arg1);
		}

		if (options.contains("RetweetsByMe")) {
			A retweetsbyme = new A("RetweetsByMe.html", "Retweets von mir");
			retweetsbyme.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(retweetsbyme));

			arg2.progress("Download Retweets von mir...");
			downloadSimpleTable(twitter, "RetweetsByMe", arg1);
		}*/

		// to create Timeline-Metadata retweets are needed
		createUser(document, arg1);

		if (options.contains("Favourites")) {
			A favourites = new A("Favorites.html", "Favoriten");
			favourites.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(favourites));

			arg2.progress("Download Favouriten...");
			downloadSimpleTable(twitter, "Favorites", arg1);
		}

		if (options.contains("Lists")) {
			A lists = new A("lists.html", "Listen");
			lists.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(lists));

			arg2.progress("Download Benutzer-Listen...");
			downloadLists(twitter, arg1);
		}

		navlist.addElement(ul);
		applic_content_page.addElement(navlist);
		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		arg1.addFile(is, "index.html", new MetainfoContainer());
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

		Pattern pattern = Pattern
				.compile("\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)"
						+ "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov"
						+ "|mil|biz|info|mobi|name|aero|jobs|museum"
						+ "|travel|[a-z]{2}))(:[\\d]{1,5})?"
						+ "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?"
						+ "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
						+ "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)"
						+ "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
						+ "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*"
						+ "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		for (String link : result) {
			text = text.replace(link, "<a target='_blank' href='" + link + "'>"
					+ link + "</a>");
		}

		return text;
	}

	private Document createDocument(String title, String header) {
		Document doc = (Document) new Document();
		doc.appendHead("<meta http-equiv='content-type' content='text/html; charset=UTF-8' />");
		String backmeuplogo = "Themes/backmeuplogo.jpg";
		String twitterlogo = "Themes/twitterlogo.png";
		doc.appendHead("<link rel='stylesheet' type='text/css' href='Themes/styles.css'>");

		doc.appendTitle(title);
		Div top = (Div) new Div().addAttribute("id", "top");
		Div topcontent = (Div) new Div().addAttribute("id", "topcontent");
		Div headlogo = (Div) new Div().addAttribute("class", "headlogo");
		IMG bmulogo = new IMG(backmeuplogo);
		headlogo.addElement(bmulogo);
		topcontent.addElement(headlogo);
		top.addElement(topcontent);

		doc.appendBody(top);

		Div content = (Div) new Div().addAttribute("id", "content");
		Div applic_logo = (Div) new Div().addAttribute("class", "applic_logo");
		IMG twlogo = new IMG(twitterlogo);
		applic_logo.addElement(twlogo);
		content.addElement(applic_logo);

		Div applic_content_header = (Div) new Div().addAttribute("class",
				"applic_content_header");
		applic_content_header.addElement("my twitter: " + title);

		content.addElement(applic_content_header);

		Div applic_content = (Div) new Div().addAttribute("class",
				"applic_content");
		Div applic_content_page = (Div) new Div().addAttribute("class",
				"applic_content_page");
		applic_content_page.addAttribute("id", "applic_content_page");
		applic_content.addElement(applic_content_page);
		content.addElement(applic_content);
		ce.addElementToRegistry("applic_content_page", applic_content_page);
		doc.appendBody(content);

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

	private String extractMedia(Status state, String parent, Storage storage,
			String text) {
		try {
			MediaEntity[] media = state.getMediaEntities();
			for (MediaEntity m : media) {
				URL url = new URL(m.getMediaURL());
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
					metainfo.setAttribute("tweet", text);

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

			URL url = new URL(user.getProfileImageURL());
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

			InputStream is;
			try {
				is = new ByteArrayInputStream(document.getBytes("UTF-8"));
				storage.addFile(is, "profile.html", metadata);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new StorageException(e);
			}
		} catch (StorageException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"A twitter-error occurred while creating User-File", e);
		} catch (MalformedURLException e1) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"A twitter-error occurred while creating User-File", e1);
		}
	}

	private String downloadUser(Twitter twitter, Storage storage,
			List<String> options) {
		TwitterDescriptor desc = new TwitterDescriptor();
		try {
			user = twitter.showUser(twitter.getId());

			// increase number of states per API call
			Paging paging = new Paging();
			paging.setCount(200);

			List<Status> timeline = twitter.getHomeTimeline(paging);

			// create HTML timeline+userID.html
			Document doc = createDocument("Profile", "Twitter");

			Div applic_content_page = (Div) ce
					.getElement("applic_content_page");

			Div applic_user = (Div) new Div().addAttribute("class",
					"applic_user clearfix");
			applic_content_page.addElement(applic_user);
			Div detail_row1 = (Div) new Div().addAttribute("class",
					"detail_row1");

			Div detail_row2 = (Div) new Div().addAttribute("class",
					"detail_row2");
			Table detail = new Table();
			detail.addAttribute("class", "detail_table");

			applic_user.addElement(detail_row1);
			applic_user.addElement(detail_row2);
			detail_row2.addElement(detail);

			// save profile image in extra file (profileImage.extension) if
			// exists

			URL url = new URL(user.getProfileImageURL());
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

				detail_row1.addElement(new IMG("profileImage" + extension));
			}

			TR row = new TR();
			row.addElement(new TD("Name").addAttribute("class", "firstrow"));
			row.addElement(new TD(user.getName()));
			detail.addElement(row);
			row = new TR();
			row.addElement(new TD("Username").addAttribute("class", "firstrow"));
			row.addElement(new TD(user.getScreenName()));
			detail.addElement(row);

		//	AccountTotals acct = twitter.getAccountTotals();
		
			/*row = new TR();
			row.addElement(new TD("Freund(e):").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(" " + acct.getFriends()));
			row.addElement(new TD("Follower(s):").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(" " + acct.getFollowers()));
			detail.addElement(row);
			row = new TR();
			row.addElement(new TD("Favorit(en):").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(" " + acct.getFavorites()));
			row.addElement(new TD("Update(s):").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(" " + acct.getUpdates()));
			detail.addElement(row);
*/
			Div applic_timeline = new Div();

			applic_timeline.addElement(new BR());
			applic_timeline.addElement(new BR());
			applic_timeline.addElement(new H2("Home-Timeline").addAttribute(
					"class", "detailhead2"));

			Table timeTable = (Table) new Table().addAttribute("class",
					"timeline_table");

			TR tr = null;
			TD td = null;

			Status lastState = null;

			while (timeline.size() > 1) {
				for (Status state : timeline) {
					states.add(state);

					String text = createLink(state.getText());
					tr = new TR();
					td = new TD(new A().setName(""+state.getId())+state.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + state.getUser().getScreenName());
					tr.addElement(td);
					td = new TD(text);
					tr.addElement(td);

					timeTable.addElement(tr);
					lastState = state;

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state, "index", storage,
								text);
						
						if (media != null && !media.equals("")) {
							td = new TD(new A(media,
									new IMG(media).addAttribute("height",
											"50px")).addAttribute("target",
									"_blank"));
							tr.addElement(td);
						}
					}

				}
				paging.setMaxId(lastState.getId());
				timeline = twitter.getHomeTimeline(paging);

			}
			applic_timeline.addElement(timeTable);

			applic_content_page.addElement(applic_timeline);
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
			if (type.equals("RetweetsOfMe"))
				typeText = "Retweets von meinen Tweets";
			if (type.equals("RetweetsToMe"))
				typeText = "Retweets an mich";
			if (type.equals("RetweetsByMe"))
				typeText = "Retweets von mir";
			if (type.equals("Favorites"))
				typeText = "Favoriten";
			// create HTML type.html
			Document doc = createDocument("Die letzten " + typeText
					+ " (max. 3200)", "Twitter");

			Div applic_content_page = (Div) ce
					.getElement("applic_content_page");

			Div applic_timeline = new Div();

			Table table = (Table) new Table().addAttribute("class",
					"timeline_table");

			TR tr = null;
			TD td = null;

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
							td.addElement(new A("profile.html#" + source.getId(),
									"zum Quell-Tweet"));
						}
					} else {
						if (type.equals("RetweetsOfMe"))
							metainfo.setType("retweet");
						else
							metainfo.setType("favourit");
						if (states.contains(state)) {
							td.addElement(new A("profile.html#" + state.getId(),
									"zum Quell-Tweet"));
						}
					}

					metadata.addMetainfo(metainfo);

					tr.addElement(td);
					td = new TD(state.getCreatedAt().toString());
					tr.addElement(td);
					td = new TD("@" + state.getUser().getScreenName());
					tr.addElement(td);
					td = new TD(text);
					tr.addElement(td);

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state, type, storage, text);
						if (!media.equals("")) {
							td = new TD(new A(media,
									new IMG(media).addAttribute("height",
											"50px")).addAttribute("target",
									"_blank"));
							tr.addElement(td);
						}
					}

					table.addElement(tr);
					lastState = state;
				}

				applic_timeline.addElement(table);

				applic_content_page.addElement(applic_timeline);

				paging.setMaxId(lastState.getId());
				download = getTypeStates(twitter, type, paging);
				if (download.size() == 1)
					download.remove(0);
			}

			InputStream is = new ByteArrayInputStream(doc.toString().getBytes(
					"UTF-8"));
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
			/*else if (type.equals("RetweetsToMe"))
				download = twitter.getRetweetedToMe(paging);
			else if (type.equals("RetweetsByMe"))
				download = twitter.getRetweetedByMe(paging);*/
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
			Document doc = createDocument(list.getFullName(), "Twitter");


			Div applic_content_page = (Div) ce
					.getElement("applic_content_page");

			Div applic_user = (Div) new Div().addAttribute("class",
					"applic_user clearfix");
			applic_content_page.addElement(applic_user);
			Div detail_row1 = (Div) new Div().addAttribute("class",
					"detail_row1");

			Div detail_row2 = (Div) new Div().addAttribute("class",
					"detail_row2");
			Table detail = new Table();
			detail.addAttribute("class", "detail_table");

			applic_user.addElement(detail_row1);
			applic_user.addElement(detail_row2);
			detail_row2.addElement(detail);

			TR row = new TR();
			row.addElement(new TD("Listenname: ").addAttribute("class", "firstrow"));
			row.addElement(new TD(list.getFullName()));
			detail.addElement(row);
			
			row = new TR();
			row.addElement(new TD("Beschreibung: ").addAttribute("class", "firstrow"));
			row.addElement(new TD(list.getDescription()));
			detail.addElement(row);
			
			row = new TR();
			row.addElement(new TD("Mitglied(er): ").addAttribute("class", "firstrow"));
			row.addElement(new TD(""+list.getMemberCount()));
			detail.addElement(row);
			
			row = new TR();
			row.addElement(new TD("Teilnehmer: ").addAttribute("class", "firstrow"));
			row.addElement(new TD(""+list.getSubscriberCount()));
			detail.addElement(row);
			
			long cursor = -1;
			PagableResponseList users;
			
			if (list.getMemberCount() > 0) {
				
				Table detail2 = new Table();
				detail2.addAttribute("class", "detail_table");
				applic_user.addElement(detail2);
				
				TR tr = new TR();
				detail2.addElement(new BR());
				detail2.addElement(tr);
				tr.addElement(new TD(new H2("Mitglieder").addAttribute("class", "detailhead2")).addAttribute("class", "firstrow"));
				UL ulMembers = new UL();
				
				do {
					users = twitter.getUserListMembers(listId, cursor);
					for (Object objUser : users) {
						User user = (User) objUser;
						ulMembers.addElement(new LI().addElement("@"+user.getScreenName()));
					}

				} while ((cursor = users.getNextCursor()) != 0);
				tr.addElement(new TD().addElement(ulMembers));
			}
			
			
			if (list.getSubscriberCount() > 0) {
				
				Table detail3 = new Table();
				detail3.addAttribute("class", "detail_table");
				applic_user.addElement(detail3);
				
				TR tr = new TR();
				detail3.addElement(new BR());
				detail3.addElement(tr);
				tr.addElement(new TD(new H2("Teilnehmer").addAttribute("class", "detailhead2")).addAttribute("class", "firstrow"));
				UL ulSubscriber = new UL();
				
				cursor = -1;
				do {
					users = twitter.getUserListSubscribers(listId, cursor);
					for (Object objUser : users) {
						User user = (User) objUser;
						ulSubscriber.addElement(new LI().addElement("@"+user.getScreenName()));
					}
				} while ((cursor = users.getNextCursor()) != 0);
				tr.addElement(new TD().addElement(ulSubscriber));
			}
			
			Div applic_timeline = new Div();

			applic_timeline.addElement(new BR());
			applic_timeline.addElement(new BR());
			applic_timeline.addElement(new H2("Home-Timeline").addAttribute(
					"class", "detailhead2"));

			Table timeTable = (Table) new Table().addAttribute("class",
					"timeline_table");

			TR tr = null;
			TD td = null;

			List<Status> states = twitter.getUserListStatuses(listId, paging);

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
					td = new TD(text);
					tr.addElement(td);
					timeTable.addElement(tr);
					lastState = state;

					// extract media entities and save in separate file
					if (state.getMediaEntities() != null) {
						String media = extractMedia(state,
								"list" + list.getId(), storage, state.getText());
						if (!media.equals("")) {
							td = new TD(new A(media,
									new IMG(media).addAttribute("height",
											"50px")).addAttribute("target",
									"_blank"));
							tr.addElement(td);
						}
					}
				}
				paging.setMaxId(lastState.getId());
				states = twitter.getUserListStatuses(listId, paging);
			}
			
			applic_timeline.addElement(timeTable);

			applic_content_page.addElement(applic_timeline);


			InputStream is = new ByteArrayInputStream(doc.toString().getBytes(
					"UTF-8"));
			storage.addFile(is, "list" + listId + ".html", metadata);

		} catch (Exception e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while download Lists", e);
		}
	}

	private void downloadLists(Twitter twitter, Storage storage) {

		Document doc = createDocument("Listen", "Twitter");
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		try {
			List<UserList> lists = twitter.getUserLists(user.getId());
			for (UserList l : lists) {
				A list = new A("list" + l.getId()+".html", l.getFullName());
				list.addAttribute("class", "navbutton");
				ul.addElement(new LI().addElement(list));

				downloadList(twitter, l.getId(), storage);
			}
			navlist.addElement(ul);
			applic_content_page.addElement(navlist);
			InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
			storage.addFile(is, "lists.html", new MetainfoContainer());
		} catch (TwitterException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occurred while downloading lists", e);
		} catch (StorageException e) {
			throw new PluginException(TwitterDescriptor.TWITTER_ID,
					"An error occured while storing lists");
		}

	}

	@Override
	public List<String> getAvailableOptions(Properties accessData) {
		List<String> twitterBackupOptions = new ArrayList<String>();
		//twitterBackupOptions.add("RetweetsToMe");
		//twitterBackupOptions.add("RetweetsByMe");
		twitterBackupOptions.add("RetweetsOfMe");
		twitterBackupOptions.add("Favourites");
		twitterBackupOptions.add("Lists");
		return twitterBackupOptions;
	}

	public void getThemes(Storage storage, Properties props)
			throws DatasourceException, StorageException {
		InputStream is;
		try {
			is = this.getClass().getResourceAsStream("/backmeuplogo.jpg");
			storage.addFile(is, "Themes/backmeuplogo.jpg", null);
			is = this.getClass().getResourceAsStream("/twitterlogo.png");
			storage.addFile(is, "Themes/twitterlogo.png", null);
			is = this.getClass().getResourceAsStream("/list_point.jpg");
			storage.addFile(is, "Themes/list_point.jpg", null);
			is = this.getClass().getResourceAsStream("/styles.css");
			storage.addFile(is, "Themes/styles.css", null);
			if (is != null)
				is.close();
			// fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
