package org.backmeup.facebook;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import com.restfb.types.Album;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Group;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Post;
import com.restfb.types.User;
import com.restfb.types.User.Education;
import com.restfb.types.User.Work;

/**
 * class FacebookDatasource to download the own profile, friends with photos,
 * groups, posts, friendslists, sites and albums with photos
 * 
 * @author aschmiedhofer, mmurauer
 */
public class FacebookDatasource implements Datasource {

	private static final boolean DOWNLOAD_NON_FRIEND_USERS = true;

	private List<String> allUsers = new LinkedList<String>();
	private String accessToken = "";
	private ConcreteElement ce = new ConcreteElement();
	private String list_point = "Themes/list_point.jpg";

	@Override
	public void downloadAll(Properties props, List<String> options,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {

		accessToken = props.getProperty(FacebookHelper.PROPERTY_TOKEN);
		FacebookClient client = new DefaultFacebookClient(accessToken);

		getThemes(storage, props);

		Document doc = createDocument("Index", "Facebook", false);
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		if (options.contains("Profile")) {
			progr.progress("Download User-Profil...");
			A profile = new A(downloadUser("me", client, storage, progr),
					"Mein Profil");
			profile.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(profile));
		}

		if (options.contains("Friends")) {
			progr.progress("Download Freunde...");
			downloadFriends(client, storage, progr);
			A friends = new A("friends.html", "Freunde");
			friends.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(friends));
		}
		if (options.contains("Friendslists")) {
			progr.progress("Download Freundesliste...");
			downloadFriendlists(client, storage, progr);
			A friendslist = new A("friendlists.html", "Freundesliste");
			friendslist.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(friendslist));
		}
		if (options.contains("Groups")) {
			progr.progress("Download Gruppen...");
			downloadGroups(client, storage, progr);
			A groups = new A("groups.html", "Gruppen");
			groups.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(groups));
		}
		if (options.contains("Posts")) {
			progr.progress("Download Posts...");
			downloadPosts("me", client, storage, progr);
			A posts = new A("posts-me.html", "Posts");
			posts.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(posts));
		}
		if (options.contains("Photos")) {
			progr.progress("Download Fotos...");
			downloadPhotos(client, storage, progr);
			A photos = new A("photos.html", "Fotos");
			photos.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(photos));
		}

		if (options.contains("Albums")) {
			progr.progress("Download Alben...");
			downloadAlbums(client, storage, progr);
			A albums = new A("albums.html", "Alben");
			albums.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(albums));
		}

		if (options.contains("Sites")) {
			progr.progress("Download Seiten...");
			downloadAccounts(client, storage, progr);
			A sites = new A("accounts.html", "Seiten");
			sites.addAttribute("class", "navbutton");
			ul.addElement(new LI().addElement(sites));
		}
		navlist.addElement(ul);
		applic_content_page.addElement(navlist);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "index.html", new MetainfoContainer());

	}

	@Override
	public String getStatistics(Properties props) {
		return null;
	}

	private void downloadAlbums(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Alben", "Facebook - Alben", false);
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div applic_albums = (Div) new Div().addAttribute("class",
				"applic_albums clearfix");

		Connection<Album> albums = client.fetchConnection("me/albums",
				Album.class);
		do {
			for (Album album : albums.getData()) {
				UL ul = new UL();
				ul.addElement(new LI().addElement(new IMG("Alben/Fotos/"
						+ album.getCoverPhoto() + ".jpg")));
				A link = new A(downloadAlbum(album, client, storage, progr),
						checkName(album.getName()));
				LI li = (LI) new LI().addAttribute("class", "albums_name");
				li.addElement(link);
				ul.addAttribute("class", "clearfix");
				ul.addElement(li);
				applic_albums.addElement(ul);
			}
		} while (albums.hasNext()
				&& (albums = client.fetchConnectionPage(
						albums.getNextPageUrl(), Album.class)) != null);

		applic_content_page.addElement(applic_albums);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "albums.html", new MetainfoContainer());
	}

	private void downloadPhotos(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Fotos", "Facebook - Fotos", false);

		downloadPhotos("me", "", doc, client, storage, progr);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "photos.html", new MetainfoContainer());
	}

	/**
	 * this method only inserts a photo, photo is not saved as a document
	 * 
	 * @param id
	 *            can be a album or user
	 */
	private void downloadPhotos(String id, String type, Document doc,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {
		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		if(type.equals("site")){
			applic_content_page = (Div) ce.getElement("detail_row2");
		}
		
		Div applic_photos = (Div) new Div().addAttribute("class",
				"applic_photos clearfix");
		
		Connection<Photo> photos = client.fetchConnection(id + "/photos",
				Photo.class);

		String parent = "";
		if (!id.equals("me"))
			parent = id;

		if (type.equals("album"))
			applic_content_page = (Div) ce.getElement("applic_friends");

		do {
			for (Photo photo : photos.getData()) {
				String piclink = downloadPhoto(photo, parent, client, storage,
						progr);
				if (!id.equals("me"))
					piclink = piclink.substring(6);
				if (type.equals("site"))
					piclink = "../Alben/" + piclink;
				String imglink = piclink.replace("html", "jpg");
				UL ul = new UL();
				ul.addElement(new LI().addElement(new IMG(imglink)));
				A link = new A(piclink, photo.getName() != null ? photo
						.getName().split("\n")[0] : "Foto");
				LI li = (LI) new LI().addAttribute("class", "photo_name");
				li.addElement(link);
				ul.addAttribute("class", "clearfix");
				ul.addElement(li);
				applic_photos.addElement(ul);
			}
		} while (photos.hasNext()
				&& (photos = client.fetchConnectionPage(
						photos.getNextPageUrl(), Photo.class)) != null);
		applic_content_page.addElement(applic_photos);
	}

	/**
	 * comments are added to the document
	 * 
	 * @param id
	 *            can be anything that has a connection "comments"
	 */
	private MetainfoContainer downloadComments(String id, String destination,
			String parent, String type, Document doc, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {

		Div div = null;
		if (type.equals("album"))
			div = (Div) ce.getElement("commentsAlbum");
		if (type.equals("post"))
			div = (Div) ce.getElement("commentsPost");
		if (type.equals("photo")) {
			div = (Div) ce.getElement("commentsPhoto");
		}

		MetainfoContainer metainfo = new MetainfoContainer();
		if (div != null) {
			Metainfo commentinfo;
			Connection<Post> comments = client.fetchConnection(
					id + "/comments", Post.class);
			if (comments.getData() == null)
				return metainfo;
			if (comments.getData().size() == 0)
				return metainfo;
			div.addElement(new H2("Kommentare").addAttribute("class",
					"detailhead2"));
			do {
				for (Post comment : comments.getData()) {

					commentinfo = new Metainfo();
					commentinfo.setAttribute("destination", destination);
					commentinfo.setAttribute("author", checkName(comment
							.getFrom().getName()));
					commentinfo.setAttribute("message", comment.getMessage());
					commentinfo.setBackupDate(new Date());
					commentinfo.setId(comment.getId());
					if (comment.getCreatedTime() != null)
						commentinfo.setCreated(comment.getCreatedTime());
					if (comment.getUpdatedTime() != null)
						commentinfo.setModified(comment.getUpdatedTime());
					commentinfo.setParent(parent);
					commentinfo.setSource("facebook");
					commentinfo.setType("comment");

					div.addElement(linkUser(comment.getFrom().getId(),
							checkName(comment.getFrom().getName()), type,
							client, storage, progr)
							+ ":");
					div.addElement(comment.getMessage());

					Div likesComments = (Div) new Div().addAttribute("class",
							"likesComments");
					ce.addElementToRegistry("likesComments", likesComments);
					div.addElement(likesComments);
					String likes = downloadLikes(comment.getId(), "comment",
							doc, client, storage, progr);

					likesComments.addElement(new BR());
					if (likes != null)
						commentinfo.setAttribute("likes", likes);
					
					metainfo.addMetainfo(commentinfo);
				}
			} while (comments.hasNext()
					&& (comments = client.fetchConnectionPage(
							comments.getNextPageUrl(), Post.class)) != null);
		}
		return metainfo;
	}

	/**
	 * likes are added to the document
	 * 
	 * @param id
	 *            can be anything that has a connection "likes"
	 */
	private String downloadLikes(String id, String type, Document doc,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {
		String likers = "";
		Div div = null;
		Connection<User> likes = client.fetchConnection(id + "/likes",
				User.class);
		if (type.equals("comment"))
			div = (Div) ce.getElement("likesComments");
		if (type.equals("post"))
			div = (Div) ce.getElement("likesPost");
		if (type.equals("photo"))
			div = (Div) ce.getElement("likesPhoto");
		if (type.equals("album"))
			div = (Div) ce.getElement("likesAlbum");
		if (div == null)
			return null;
		if (likes.getData() == null)
			return null;
		if (likes.getData().size() == 0)
			return null;

		if (type == "comment") {
			div.addElement("\tLikes:");
		} else{
			div.addElement(new BR());
			div.addElement(new H2("Likes").addAttribute("class", "detailhead2"));
		}

		do {
			for (User like : likes.getData()) {
				div.addElement(linkUser(like.getId(),
						checkName(like.getName()), type, client, storage, progr));
				likers += checkName(like.getName())+", ";
				div.addElement(new BR());
			}
		} while (likes.hasNext()
				&& (likes = client.fetchConnectionPage(likes.getNextPageUrl(),
						User.class)) != null);

		return likers.substring(0, likers.length()-2);
	}

	private void downloadGroups(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Gruppen", "Facebook - Gruppen", false);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		Connection<Group> groups = client.fetchConnection("me/groups",
				Group.class);
		do {
			for (Group group : groups.getData()) {
				A link = new A(downloadGroup(group.getId(), client, storage,
						progr), checkName(group.getName()));
				link.addAttribute("class", "navbutton");
				ul.addElement(new LI().addElement(link));
			}
		} while (groups.hasNext()
				&& (groups = client.fetchConnectionPage(
						groups.getNextPageUrl(), Group.class)) != null);

		navlist.addElement(ul);
		applic_content_page.addElement(navlist);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "groups.html", new MetainfoContainer());
	}

	private void downloadPosts(String id, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {

		Document doc = createDocument("Pinwand", "Facebook - Pinwand", false);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div applic_posts = (Div) new Div().addAttribute("class",
				"applic_posts clearfix");

		Connection<Post> posts = client.fetchConnection(id + "/feed",
				Post.class);
		do {
			for (Post post : posts.getData()) {
				if (post.getMessage() == null
						&& (post.getType() != "video"
								|| post.getType() != "link" || post.getType() != "photo")) {
					// ignoring everything which does not have a message (like
					// "xy got friend with yz")
					continue;
				}
				UL ul = new UL();
				A link = new A(getUserFilename(checkName(post.getFrom()
						.getName()) + post.getFrom().getId()), checkName(post
						.getFrom().getName()));
				ul.addAttribute("class", "clearfix");
				ul.addElement(new LI().addElement(link));
				LI date = (LI) new LI().addAttribute("class", "poststxt_date");
				date.addElement(new Date().toString());
				ul.addElement(date);
				LI txt = (LI) new LI().addAttribute("class", "poststxt_txt");
				txt.addElement(new A(
						downloadPost(post, client, storage, progr), post
								.getMessage().split("\n")[0]));
				ul.addElement(txt);
				applic_posts.addElement(ul);

			}
		} while (posts.hasNext()
				&& (posts = client.fetchConnectionPage(posts.getNextPageUrl(),
						Post.class)) != null);

		applic_content_page.addElement(applic_posts);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "posts-" + id + ".html", new MetainfoContainer());
	}

	private void downloadFriends(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Freunde", "Facebook - Freunde", false);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div applic_friends = (Div) new Div().addAttribute("class",
				"applic_friends clearfix");

		Connection<User> friends = client.fetchConnection("me/friends",
				User.class);
		;
		do {
			for (User friend : friends.getData()) {
				if (friend != null && friend.getId() != null
						&& friend.getName() != null) {
					UL ul = new UL();
					ul.addAttribute("class", "clearfix");
					ul.addElement(new LI().addElement(new IMG("Freunde/Fotos/"
							+ checkName(friend.getName()) + friend.getId() + ".jpg")));
					A link = new A(downloadUser(friend.getId(), client,
							storage, progr), checkName(friend.getName()));
					LI name = (LI) new LI()
							.addAttribute("class", "friend_name");
					name.addElement(link);
					ul.addElement(name);

					applic_friends.addElement(ul);
				}
			}
		} while (friends.hasNext()
				&& (friends = client.fetchConnectionPage(
						friends.getNextPageUrl(), User.class)) != null);

		applic_content_page.addElement(applic_friends);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "friends.html", new MetainfoContainer());
	}

	private void downloadFriendlists(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Freundesliste",
				"Facebook - Freundesliste", false);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		Connection<CategorizedFacebookType> lists = client.fetchConnection(
				"me/friendlists", CategorizedFacebookType.class);
		do {
			for (CategorizedFacebookType friendlist : lists.getData()) {
				Connection<User> members = client.fetchConnection(
						friendlist.getId() + "/members", User.class);

				if (members.getData().size() > 0) {
					A link = new A(downloadFriendlist(friendlist.getId(),
							checkName(friendlist.getName()), client, storage,
							progr), checkName(friendlist.getName()));
					link.addAttribute("class", "navbutton");
					ul.addElement(new LI().addElement(link));
				}
			}
		} while (lists.hasNext()
				&& (lists = client.fetchConnectionPage(lists.getNextPageUrl(),
						CategorizedFacebookType.class)) != null);
		navlist.addElement(ul);
		applic_content_page.addElement(navlist);
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "friendlists.html", new MetainfoContainer());
	}

	private String downloadFriendlist(String id, String name,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {

		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo listinfo = new Metainfo();
		listinfo.setAttribute("name", name);
		listinfo.setBackupDate(new Date());
		listinfo.setDestination(getFriendlistFilename(name + id));
		listinfo.setId(id);
		listinfo.setSource("facebook");
		listinfo.setType("list");
		metainfo.addMetainfo(listinfo);

		String listmembers = "";

		// create HTML
		Document doc = createDocument(name, "Facebook - Freundesliste", true);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div applic_user = (Div) new Div().addAttribute("class",
				"applic_user clearfix");
		Div detail = (Div) new Div().addAttribute("class", "detail_row2");

		Table t = (Table) new Table().addAttribute("class", "detail_table");
		TR tr = new TR();

		TD td1 = (TD) new TD().addAttribute("class", "firstrow");
		td1.addElement("Name:");
		tr.addElement(td1);
		TD td2 = new TD().addElement(name);
		tr.addElement(td2);
		t.addElement(tr);

		tr = new TR();
		td1 = (TD) new TD().addAttribute("class", "firstrow");
		td1.addElement("UserID:");
		tr.addElement(td1);
		td2 = new TD().addElement(id);
		tr.addElement(td2);
		t.addElement(tr);

		detail.addElement(t);

		applic_user.addElement(detail);

		applic_user.addElement(new BR());
		applic_user.addElement(new H2("Mitglieder").addAttribute("class",
				"detailhead2"));

		Div applic_friends = (Div) new Div().addAttribute("class",
				"applic_friends clearfix");

		// download members
		Connection<User> members = client.fetchConnection(id + "/members",
				User.class);
		do {
			for (User member : members.getData()) {
				UL ul = new UL();
				ul.addAttribute("class", "clearfix");
				ul.addElement(new LI().addElement(new IMG("../Freunde/Fotos/"
						+ checkName(member.getName()) + member.getId() + ".jpg")));
				A link = new A("../"
						+ getUserFilename(checkName(member.getName())
								+ member.getId()), checkName(member.getName()));
				LI friendname = (LI) new LI().addAttribute("class",
						"friend_name");
				friendname.addElement(link);
				ul.addElement(friendname);

				applic_friends.addElement(ul);

				listmembers += checkName(member.getName()) + " ";
			}
		} while (members.hasNext()
				&& (members = client.fetchConnectionPage(
						members.getNextPageUrl(), User.class)) != null);

		listinfo.setAttribute("members", listmembers);

		applic_user.addElement(applic_friends);
		applic_content_page.addElement(applic_user);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		String filename = getFriendlistFilename(name + id);
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadPost(Post post, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {

		Metainfo postinfo = new Metainfo();
		postinfo.setAttribute("author", checkName(post.getFrom().getName()));
		postinfo.setAttribute("message", post.getMessage());
		postinfo.setBackupDate(new Date());
		postinfo.setDestination(getPostFilename(post.getId()));
		postinfo.setId(post.getId());
		postinfo.setSource("facebook");
		if (post.getCreatedTime() != null)
			postinfo.setCreated(post.getCreatedTime());
		if (post.getUpdatedTime() != null)
			postinfo.setModified(post.getUpdatedTime());
		postinfo.setType("post");

		// create HTML
		Document doc = createDocument("Post", "Post", true);
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div applic_posts = (Div) new Div().addAttribute("class",
				"applic_posts clearfix");
	

		Table detail = new Table();
		detail.addAttribute("class", "detail_table");


		TR row = new TR();
		row.addElement(new TD("Type").addAttribute("class", "firstrow"));
		row.addElement(new TD(post.getType()));
		detail.addElement(row);

		row = new TR();
		row.addElement(new TD("Sender").addAttribute("class", "firstrow"));
		row.addElement(new TD(post.getFrom().getName()));
		detail.addElement(row);

		if (post.getTo() != null) {
			String to = post.getTo().toString();
			String[] toArray = to.split("=");
			if (toArray.length > 2) {
				int index = toArray[3].lastIndexOf(" ");
				String toName = checkName(toArray[3].substring(0, index));
				postinfo.setAttribute("receiver", toName);
				row = new TR();
				row.addElement(new TD("Empfänger").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(toName));
				detail.addElement(row);
			}
		}

		row = new TR();
		row.addElement(new TD("Zeit").addAttribute("class", "firstrow"));
		row.addElement(new TD(post.getCreatedTime().toString()));
		detail.addElement(row);
		row = new TR();
		row.addElement(new TD("Nachricht").addAttribute("class", "firstrow"));
		row.addElement(new TD(post.getMessage()));
		detail.addElement(row);
		if (post.getName() != null) {
			row = new TR();
			row.addElement(new TD("Name").addAttribute("class", "firstrow"));
			row.addElement(new TD(post.getName()));
			detail.addElement(row);
		}
		if (post.getDescription() != null) {
			row = new TR();
			row.addElement(new TD("Beschreibung").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(post.getDescription()));
			detail.addElement(row);
		}
		if (post.getLink() != null) {
			row = new TR();
			row.addElement(new TD("Link").addAttribute("class", "firstrow"));
			row.addElement(new TD("<a href=" + post.getLink()
					+ " target='_blank'>" + post.getLink() + "</a>"));
			detail.addElement(row);
		}
		if (post.getSource() != null) {
			row = new TR();
			row.addElement(new TD("Quelle").addAttribute("class", "firstrow"));
			row.addElement(new TD(post.getSource()));
			detail.addElement(row);
		}
		applic_posts.addElement(detail);
		
		Div commentsPost = (Div) new Div()
				.addAttribute("class", "commentsPost");
		ce.addElementToRegistry("commentsPost", commentsPost);

		MetainfoContainer metainfo = downloadComments(post.getId(),
				getPostFilename(post.getId()), getPostFilename(post.getId()),
				"post", doc, client, storage, progr);

		applic_posts.addElement(commentsPost);

		Div likesPost = (Div) new Div().addAttribute("class", "likesPost");
		ce.addElementToRegistry("likesPost", likesPost);

		String likes = downloadLikes(post.getId(), "post", doc, client,
				storage, progr);

		applic_posts.addElement(likesPost);

		if (likes != null)
			postinfo.setAttribute("likes", likes);
		metainfo.addMetainfo(postinfo);

		applic_content_page.addElement(applic_posts);
		
		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		String filename = getPostFilename(post.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadAlbum(Album album, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {
		String name = checkName(album.getName());

		Metainfo albuminfo = new Metainfo();
		albuminfo.setAttribute("name", name);
		albuminfo.setBackupDate(new Date());
		albuminfo.setDestination(getAlbumFilename(name + album.getId()));
		albuminfo.setId(album.getId());
		if (album.getCreatedTime() != null)
			albuminfo.setCreated(album.getCreatedTime());
		if (album.getUpdatedTime() != null)
			albuminfo.setModified(album.getUpdatedTime());
		albuminfo.setSource("facebook");
		albuminfo.setType("album");

		Document doc = createDocument(name, "Facebook - Album", true);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div applic_user = (Div) new Div().addAttribute("class",
				"applic_user clearfix");
		Div detail = (Div) new Div().addAttribute("class", "detail_row2");

		Table t = (Table) new Table().addAttribute("class", "detail_table");
		TR tr = new TR();

		TD td1 = (TD) new TD().addAttribute("class", "firstrow");
		td1.addElement("Name:");
		tr.addElement(td1);
		TD td2 = new TD().addElement(name);
		tr.addElement(td2);
		t.addElement(tr);

		if (album.getDescription() != null) {
			albuminfo.setAttribute("description", album.getDescription());

			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Beschreibung:");
			tr.addElement(td1);
			td2 = new TD().addElement(album.getDescription());
			tr.addElement(td2);
			t.addElement(tr);
		}

		if (album.getLocation() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Ort:");
			tr.addElement(td1);
			td2 = new TD().addElement(album.getLocation());
			tr.addElement(td2);
			t.addElement(tr);
		}

		if (album.getCreatedTime() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Erstellt am ");
			tr.addElement(td1);
			td2 = new TD().addElement(album.getCreatedTime().toString());
			tr.addElement(td2);
			t.addElement(tr);
		}

		if (album.getLink() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Link:");
			tr.addElement(td1);
			td2 = new TD().addElement("<a href=" + album.getLink()
					+ " target='_blank'>" + album.getLink() + "</a>");
			tr.addElement(td2);
			t.addElement(tr);
		}

		detail.addElement(t);

		applic_user.addElement(detail);

		applic_user.addElement(new BR());
		applic_user.addElement(new H2("Fotos").addAttribute("class",
				"detailhead2"));

		Div applic_friends = (Div) new Div().addAttribute("class",
				"applic_friends clearfix");
		applic_friends.addAttribute("id", "applic_friends");
		ce.addElementToRegistry("applic_friends", applic_friends);

		downloadPhotos(album.getId(), "album", doc, client, storage, progr);

		Div commentsAlbum = (Div) new Div().addAttribute("class",
				"commentsAlbum");
		ce.addElementToRegistry("commentsAlbum", commentsAlbum);

		MetainfoContainer metainfo = downloadComments(album.getId(),
				getAlbumFilename(name + album.getId()), getAlbumFilename(name
						+ album.getId()), "album", doc, client, storage, progr);

		applic_friends.addElement(commentsAlbum);

		Div likesAlbum = (Div) new Div().addAttribute("class", "likesAlbum");
		ce.addElementToRegistry("likesAlbum", likesAlbum);

		String likes = downloadLikes(album.getId(), "album", doc, client,
				storage, progr);

		applic_friends.addElement(likesAlbum);

		if (likes != null)
			albuminfo.setAttribute("likes", likes);

		metainfo.addMetainfo(albuminfo);

		applic_user.addElement(applic_friends);
		applic_content_page.addElement(applic_user);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		String filename = getAlbumFilename(name + album.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadPhoto(Photo photo, String parent,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {

		Metainfo photoinfo = new Metainfo();

		photoinfo.setBackupDate(new Date());
		photoinfo.setId(photo.getId());
		if(photo.getCreatedTime() != null)
			photoinfo.setCreated(photo.getCreatedTime());
		if(photo.getUpdatedTime() != null)
			photoinfo.setModified(photo.getUpdatedTime());
		photoinfo.setSource("facebook");
		photoinfo.setType("photo");
		if (!parent.equals(""))
			photoinfo.setParent(parent);

		// create HTML
		Document doc = createDocument("Foto", "Foto", true);
		Div applic_content_page = (Div) ce.getElement("applic_content_page");

		Div applic_photo = (Div) new Div().addAttribute("class",
				"applic_photo clearfix");
		applic_content_page.addElement(applic_photo);

		String ending = ".jpg";// only jpg supported
		String sourceFileName = "Alben/Fotos/" + photo.getId() + ending;

		photoinfo.setDestination("Alben/Fotos/" + photo.getId() + ".html");

		Div detail_row1 = (Div) new Div().addAttribute("class",
				"applic_photo_big");
		detail_row1.addElement(new IMG(sourceFileName.substring(12))
				.addAttribute("width", "200px"));

		Div detail_row2 = (Div) new Div().addAttribute("class", "detail_row2");
		Table detail = new Table();
		detail.addAttribute("class", "detail_table photodetail");

		applic_photo.addElement(detail_row1);
		applic_photo.addElement(detail_row2);
		detail_row2.addElement(detail);

		if (photo.getName() != null) {
			TR row = new TR();
			row.addElement(new TD("Bildunterschrift").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(photo.getName()));
			detail.addElement(row);

			photoinfo.setAttribute("name", photo.getName());
		}
		if (photo.getFrom() != null) {
			TR row = new TR();
			row.addElement(new TD("Von").addAttribute("class", "firstrow"));
			row.addElement(new TD(photo.getFrom().getName()));
			detail.addElement(row);
		}
		if (photo.getCreatedTime() != null) {
			TR row = new TR();
			row.addElement(new TD("Erstellt am").addAttribute("class",
					"firstrow"));
			row.addElement(new TD(photo.getCreatedTime().toString()));
			detail.addElement(row);
		}
		if (photo.getLink() != null) {
			TR row = new TR();
			row.addElement(new TD("Link").addAttribute("class", "firstrow"));
			row.addElement(new TD("<a href=" + photo.getLink()
					+ " target='_blank'>" + photo.getLink() + "</a>"));
			detail.addElement(row);
		}
		if (photo.getSource() == null) {
			throw new DatasourceException("error while downloading photos");
		}

		TR row = new TR();
		row.addElement(new TD("Quelle").addAttribute("class", "firstrow"));
		row.addElement(new TD("<a href=" + photo.getSource()
				+ " target='_blank'>" + photo.getSource() + "</a>"));
		detail.addElement(row);

		String tags = "";

		Table detail2 = new Table();
		detail2.addAttribute("class", "detail_table");
		applic_photo.addElement(detail2);
		
		if ((photo.getTags() != null) && (photo.getTags().size() > 0)) {
			TR taged = new TR();
			detail2.addElement(taged);
			taged.addElement(new TD(new H2("Tags").addAttribute("class", "detailhead2")).addAttribute("class", "firstrow"));
			UL tagedUsers = new UL();
			
			for (Tag tag : photo.getTags()) {
				if (tag.getName() != null && tag.getId() != null) {

					tagedUsers.addElement(new LI().addElement(linkUser(
							tag.getId(), checkName(tag.getName()), "photo",
							client, storage, progr)));

					tags += checkName(tag.getName()) + " ";
				}
			}
			taged.addElement(new TD().addElement(tagedUsers));
		}
		
		
		if (tags != "")
			photoinfo.setAttribute("tags", tags);

		Div commentsPhoto = (Div) new Div().addAttribute("class",
				"commentsPhoto");
		ce.addElementToRegistry("commentsPhoto", commentsPhoto);

		MetainfoContainer metainfo = downloadComments(photo.getId(),
				getPhotoFilename(photo.getId()),
				getPhotoFilename(photo.getId()), "photo", doc, client, storage,
				progr);

		applic_photo.addElement(commentsPhoto);

		Div likesPhoto = (Div) new Div().addAttribute("class", "likesPhoto");
		ce.addElementToRegistry("likesPhoto", likesPhoto);

		String likes = downloadLikes(photo.getId(), "photo", doc, client,
				storage, progr);

		applic_photo.addElement(likesPhoto);

		if (likes != null)
			photoinfo.setAttribute("likes", likes);
		
		downloadPicture(photo.getSource(), sourceFileName, "", storage, progr,
				photoinfo);

		metainfo.addMetainfo(photoinfo);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		String filename = getPhotoFilename(photo.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadGroup(String id, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {
		Group g = client.fetchObject(id, Group.class);
		String name = checkName(g.getName());

		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo groupinfo = new Metainfo();
		groupinfo.setAttribute("name", name);

		groupinfo.setBackupDate(new Date());
		groupinfo.setDestination(getGroupFilename(name + g.getId()));
		groupinfo.setId(g.getId());
		if(g.getUpdatedTime() != null)
			groupinfo.setModified(g.getUpdatedTime());
		groupinfo.setSource("facebook");
		groupinfo.setType("group");
		metainfo.addMetainfo(groupinfo);

		Document doc = createDocument(name, "Facebook - Gruppe", true);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div applic_user = (Div) new Div().addAttribute("class",
				"applic_user clearfix");
		Div detail = (Div) new Div().addAttribute("class", "detail_row2");

		Table t = (Table) new Table().addAttribute("class", "detail_table");
		TR tr = new TR();

		TD td1 = (TD) new TD().addAttribute("class", "firstrow");
		td1.addElement("Gruppenname:");
		tr.addElement(td1);
		TD td2 = new TD().addElement(name);
		tr.addElement(td2);
		t.addElement(tr);

		if (g.getDescription() != null) {

			groupinfo.setAttribute("description", g.getDescription());

			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Beschreibung:");
			tr.addElement(td1);
			td2 = new TD().addElement(g.getDescription());
			tr.addElement(td2);
			t.addElement(tr);
		}

		if (g.getPrivacy() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Privatsphäre:");
			tr.addElement(td1);
			td2 = new TD().addElement(g.getPrivacy());
			tr.addElement(td2);
			t.addElement(tr);
		}

		ce.addElementToRegistry("applic_user", applic_user);

		if (g.getOwner() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Inhaber:");
			tr.addElement(td1);
			LI li = (LI) new LI().addAttribute("class", "friend_name");
			li.addElement(linkUser(g.getOwner().getId(), checkName(g.getOwner()
					.getName()), "group", client, storage, progr));
			td2 = new TD().addElement(li);
			tr.addElement(td2);
			t.addElement(tr);

			groupinfo.setAttribute("owner", checkName(g.getOwner().getName()));
		}

		if (g.getLink() != null) {
			tr = new TR();
			td1 = (TD) new TD().addAttribute("class", "firstrow");
			td1.addElement("Link:");
			tr.addElement(td1);
			td2 = new TD().addElement("<a href=" + g.getLink()
					+ " target='_blank'>" + g.getLink() + "</a>");
			tr.addElement(td2);
			t.addElement(tr);
		}

		detail.addElement(t);
		applic_user.addElement(detail);
		applic_content_page.addElement(applic_user);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		String filename = getGroupFilename(name + g.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadUser(String id, FacebookClient client,
			Storage storage, Progressable progr) throws DatasourceException,
			StorageException {

		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo userinfo = new Metainfo();
		userinfo.setBackupDate(new Date());

		try {
			
			User u = client.fetchObject(id, User.class);

			String name = checkName(u.getName());

			userinfo.setAttribute("name", name);
			userinfo.setDestination(getUserFilename(name + u.getId()));
			userinfo.setId(name + u.getId());
			if (u.getUpdatedTime() != null)
				userinfo.setModified(u.getUpdatedTime());
			userinfo.setSource("facebook");
			userinfo.setType("user");

			// get profile picture
			String pic = downloadProfilePicture(name + u.getId(), u.getId(),
					"", storage, progr);

			userinfo.setAttribute("profilePicture", "Freunde/" + pic);
			metainfo.addMetainfo(userinfo);

			// create HTML
			Document doc = createDocument("Mein Profil", "User", true);
			Div applic_content_page = (Div) ce
					.getElement("applic_content_page");

			Div applic_user = (Div) new Div().addAttribute("class",
					"applic_user clearfix");
			applic_content_page.addElement(applic_user);

			Div detail_row1 = (Div) new Div().addAttribute("class",
					"detail_row1");
			detail_row1.addElement(new IMG(pic));

			Div detail_row2 = (Div) new Div().addAttribute("class",
					"detail_row2");
			Table detail = new Table();
			detail.addAttribute("class", "detail_table");

			applic_user.addElement(detail_row1);
			applic_user.addElement(detail_row2);
			detail_row2.addElement(detail);

			TR row = new TR();
			row.addElement(new TD("Username").addAttribute("class", "firstrow"));
			row.addElement(new TD(name));
			detail.addElement(row);

			if (u.getUsername() != null) {
				row = new TR();
				row.addElement(new TD("Benutzername").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getUsername()));
				detail.addElement(row);
			}
			if (u.getEmail() != null) {
				row = new TR();
				row.addElement(new TD("E-Mail").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getEmail()));
				detail.addElement(row);
			}

			if (u.getAbout() != null) {
				row = new TR();
				row.addElement(new TD("&Uuml;ber").addAttribute("class", "firstrow"));
				row.addElement(new TD(u.getAbout()));
				detail.addElement(row);
			}
			if (u.getBio() != null) {
				row = new TR();
				row.addElement(new TD("Bio").addAttribute("class", "firstrow"));
				row.addElement(new TD(u.getBio()));
				detail.addElement(row);
			}
			if (u.getGender() != null) {
				row = new TR();
				row.addElement(new TD("Geschlecht").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getGender()));
				detail.addElement(row);
			}
			if (u.getBirthday() != null) {
				row = new TR();
				row.addElement(new TD("Geburtstag").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getBirthday()));
				detail.addElement(row);
			}
			if (u.getHometownName() != null) {
				row = new TR();
				row.addElement(new TD("Heimatstadt").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getHometownName()));
				detail.addElement(row);
			}
			if (u.getLocation() != null) {
				row = new TR();
				row.addElement(new TD("Derzeitiger Wohnort").addAttribute(
						"class", "firstrow"));
				row.addElement(new TD(u.getLocation().getName()));
				detail.addElement(row);
			}

			if ((u.getLanguages() != null) && (u.getLanguages().size() > 0)) {
				String[] languages = new String[u.getLanguages().size()];
				int i = 0;
				for (NamedFacebookType language : u.getLanguages()) {
					languages[i] = language.getName();
					i++;
				}

				row = new TR();
				row.addElement(new TD("Sprachen").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(new UL(languages)));
				detail.addElement(row);
			}
			if ((u.getEducation() != null) && (u.getEducation().size() > 0)) {
				String[] edus = new String[u.getEducation().size()];
				int i = 0;
				for (Education educ : u.getEducation()) {
					edus[i] = educ.getType()
							+ (educ.getSchool() != null ? ": "
									+ educ.getSchool().getName()
									+ (educ.getYear() != null ? " until "
											+ educ.getYear().getName() : "")
									: "");
					i++;
				}

				row = new TR();
				row.addElement(new TD("Ausbildung").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(new UL(edus)));
				detail.addElement(row);
			}

			if ((u.getWork() != null) && (u.getWork().size() > 0)) {
				String[] works = new String[u.getWork().size()];
				int i = 0;
				for (Work work : u.getWork()) {
					works[i] = (work.getDescription() != null ? work
							.getDescription() : "")
							+ (work.getPosition() != null ? " als "
									+ work.getPosition().getName() : "")
							+ (work.getEmployer() != null ? " f&uuml;r "
									+ work.getEmployer().getName() : "")
							+ (work.getLocation() != null ? " bei "
									+ work.getLocation().getName() : "");
					i++;
				}
				row = new TR();
				row.addElement(new TD("Arbeit").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(new UL(works)));
				detail.addElement(row);
			}
			if ((u.getInterestedIn() != null)
					&& (u.getInterestedIn().size() > 0)) {
				row = new TR();
				row.addElement(new TD("Interessiert an:").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(new UL(u.getInterestedIn().toArray(
						new String[0]))));
				detail.addElement(row);
			}
			if (u.getRelationshipStatus() != null) {
				row = new TR();
				row.addElement(new TD("Beziehungsstatus").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getRelationshipStatus()));
				detail.addElement(row);
			}
			if (u.getSignificantOther() != null) {
				row = new TR();
				row.addElement(new TD("Bedeutende Personen").addAttribute(
						"class", "firstrow"));
				row.addElement(new TD(new A("../"
						+ getUserFilename(checkName(u.getSignificantOther()
								.getName()) + u.getSignificantOther().getId()),
						checkName(u.getSignificantOther().getName()))));
				detail.addElement(row);
			}
			if (u.getQuotes() != null) {
				row = new TR();
				row.addElement(new TD("Zitate").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getQuotes()));
				detail.addElement(row);
			}
			if (u.getReligion() != null) {
				row = new TR();
				row.addElement(new TD("Religion").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getReligion()));
				detail.addElement(row);
			}
			if (u.getPolitical() != null) {
				row = new TR();
				row.addElement(new TD("Politische Einstellung").addAttribute(
						"class", "firstrow"));
				row.addElement(new TD(u.getPolitical()));
				detail.addElement(row);
			}
			if (u.getWebsite() != null) {
				row = new TR();
				row.addElement(new TD("Website").addAttribute("class",
						"firstrow"));
				row.addElement(new TD(u.getWebsite()));
				detail.addElement(row);
			}
			if (u.getLink() != null) {
				row = new TR();
				row.addElement(new TD("Link").addAttribute("class", "firstrow"));
				row.addElement(new TD("<a href=" + u.getLink()
						+ " target='_blank'>" + u.getLink() + "</a>"));
				detail.addElement(row);
			}
			InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
			String filename = getUserFilename(name + u.getId());
			storage.addFile(is, filename, metainfo);

			allUsers.add(id);
			return filename;
		} catch (FacebookException e) {
			return null;
		}
	}

	private String getUserFilename(String id) {
		return "Freunde/" + id + ".html";
	}

	private String getPostFilename(String id) {
		return "Posts/" + id + ".html";
	}

	private String getGroupFilename(String id) {
		return "Gruppen/" + id + ".html";
	}

	private String getAlbumFilename(String id) {
		return "Alben/" + id + ".html";
	}

	private String getPhotoFilename(String id) {
		return "Alben/Fotos/" + id + ".html";
	}

	private String getFriendlistFilename(String id) {
		return "Freundeslisten/" + id + ".html";
	}

	/**
	 * This method is useing the Graph API (not the Old REST API (with restfb)).
	 * It is used to download the public profile picture of a user. Supports jpg
	 * only!
	 * 
	 * @param id
	 *            the id of the user (can also be "me")
	 * @param storage
	 *            the StorageWriter which can store the Image
	 * @return the path to the picture
	 * @throws StorageException
	 */
	private String downloadProfilePicture(String name, String id, String type,
			Storage storage, Progressable progr) throws StorageException {
		String fileName = "";
		if (type.equals("site"))
			fileName = "Seiten/Fotos/" + name + ".jpg";
		else if (!name.equals("me"))
			fileName = "Freunde/Fotos/" + name + ".jpg";
		else
			fileName = name + ".jpg";

		Metainfo photoinfo = new Metainfo();
		photoinfo.setId(id);
		photoinfo.setParent(name + ".html");
		photoinfo.setAttribute("name", name);
		photoinfo.setBackupDate(new Date());
		photoinfo.setDestination(fileName);
		photoinfo.setSource("facebook");
		photoinfo.setType("photo");

		String uPicLoc = null;
		if (type.equals("site"))
			uPicLoc = getGraphUrl(id + "/picture?access_token=" + accessToken,
					null);
		else
			uPicLoc = getGraphUrl(id + "/picture", "type=large");
		if (uPicLoc != null) {
			downloadPicture(uPicLoc, fileName, type, storage, progr, photoinfo);
		} else {
			progr.progress("no picture URL...");
		}
		if (type.equals("site"))
			return fileName.substring(7);

		return fileName.substring(8);
	}

	private boolean downloadPicture(String path, String destination,
			String type, Storage storage, Progressable progr, Metainfo photoinfo)
			throws StorageException {

		MetainfoContainer metainfo = new MetainfoContainer();
		photoinfo.setDestination(destination);

		metainfo.addMetainfo(photoinfo);

		HttpURLConnection c = null;
		
		try {
			URL url = new URL(path);
			c = (HttpURLConnection) url.openConnection();
			c.connect();
			if (c.getContentType().equals("image/jpeg")) {
				progr.progress("Download " + path + " nach " + destination);
				InputStream is = c.getInputStream();
				storage.addFile(is, destination, metainfo);
				is.close();
				return true;
			} else {
				progr.progress("Lade alternatives Bild");
				InputStream isAlt;
				try {
					isAlt = this.getClass().getResourceAsStream("/alternative.jpg");
					storage.addFile(isAlt, destination, null);
					isAlt.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			if (c != null)
				c.disconnect();
		}
		return false;
	}

	/**
	 * download pages, the user is admin of
	 * 
	 */
	private void downloadAccounts(FacebookClient client, Storage storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Seiten", "Facebook - Seiten", false);

		Div applic_content_page = (Div) ce.getElement("applic_content_page");
		Div navlist = (Div) new Div().addAttribute("class", "applic_navlist");
		UL ul = new UL();

		HttpURLConnection c = null;
		URL url;
		try {
			url = new URL(
					"https://graph.facebook.com/me/accounts?access_token="
							+ accessToken);
			c = (HttpURLConnection) url.openConnection();
			c.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					c.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder content = new StringBuilder();

			int temp;

			while ((temp = reader.read()) != -1) {
				content.append((char) temp);
			}
			
			try {
				  reader.close();
				} catch(Exception ex) {
				  ex.printStackTrace();
				}

			JSONObject json = new JSONObject(content.toString());
			JSONArray jsonArray = json.getJSONArray("data");

			ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonList.add(jsonArray.getJSONObject(i));
			}

			for (JSONObject obj : jsonList) {
				if (!obj.getString("category").equals("Application")) {
					A link = new A(downloadAccount(obj.getString("id"),
							checkName(obj.getString("name")), client, storage,
							progr), checkName(obj.getString("name")));
					link.addAttribute("class", "navbutton");
					ul.addElement(new LI().addElement(link));
				}
			}

		} catch (Exception e) {
			if (c != null) {
				c.disconnect();
			}
		}

		navlist.addElement(ul);
		applic_content_page.addElement(navlist);

		InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
		storage.addFile(is, "accounts.html", new MetainfoContainer());

	}

	private String downloadAccount(String id, String name,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {
		MetainfoContainer metadata = new MetainfoContainer();
        Metainfo accountinfo = new Metainfo();
        accountinfo.setBackupDate(new Date());
        accountinfo.setId(id);
        accountinfo.setSource("facebook");
        accountinfo.setType("site");


        HttpURLConnection c = null;
        URL url;
        Document doc = createDocument(name, "Seite", true);
        try {

            url = new URL("https://graph.facebook.com/" + id + "?access_token="
                    + accessToken);
            c = (HttpURLConnection) url.openConnection();
            c.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    c.getInputStream(), Charset.forName("UTF-8")));
            StringBuilder content = new StringBuilder();

            int temp;

            while ((temp = reader.read()) != -1) {
                content.append((char) temp);
            }
            
            try {
  			  reader.close();
  			} catch (Exception ex) {
  			  ex.printStackTrace();
  			}
            
            JSONObject json = new JSONObject(content.toString());
           
            Div applic_content_page = (Div) ce
                    .getElement("applic_content_page");

            Div applic_user = (Div) new Div().addAttribute("class",
                    "applic_user clearfix");
            applic_content_page.addElement(applic_user);

            String pic = downloadProfilePicture(name + id, id, "site", storage,
                    progr);
            Div detail_row1 = (Div) new Div().addAttribute("class",
                    "detail_row1");
            detail_row1.addElement(new IMG(pic));

            Div detail_row2 = (Div) new Div().addAttribute("class",
                    "detail_row2");
            Table detail = new Table();
            detail.addAttribute("class", "detail_table");

            applic_user.addElement(detail_row1);
            applic_user.addElement(detail_row2);
            detail_row2.addElement(detail);
           
            TR row = new TR();
            row.addElement(new TD("Name").addAttribute("class", "firstrow"));
            row.addElement(new TD(name));
            detail.addElement(row);
           
            row = new TR();
            row.addElement(new TD("Link").addAttribute("class", "firstrow"));
            row.addElement(new TD(json.getString("link")));
            detail.addElement(row);
           
            downloadPosts(id, client, storage, progr);
            
            row = new TR();
            row.addElement(new TD("Posts").addAttribute("class", "firstrow"));
            row.addElement(new TD(new A("../" + "posts-" + id + ".html", "Posts")));
            detail.addElement(row);
           
            row = new TR();
            ce.addElementToRegistry("detail_row2", detail_row2);
            downloadPhotos(id, "site", doc, client, storage, progr);
            detail.addElement(row);
           
            accountinfo.setId(id);
            accountinfo.setDestination("Seiten/" + name + id + ".html");
            accountinfo.setAttribute("name", name);

        } catch (Exception e) {
            if (c != null) {
                c.disconnect();
            }
        }
        metadata.addMetainfo(accountinfo);
        InputStream is = new ByteArrayInputStream(doc.toString("UTF-8").getBytes());
        storage.addFile(is, "Seiten/" + name + id + ".html", metadata);

        return "Seiten/" + name + id + ".html";
    }

	private Document createDocument(String title, String header, boolean out) {
		Document doc = (Document) new Document();
		doc.setCodeset("UTF-8");
		doc.appendHead("<meta http-equiv='content-type' content='text/html; charset=UTF-8' />");
		String backmeuplogo = "Themes/backmeuplogo.jpg";
		String facebooklogo = "Themes/facebooklogo.jpg";
		if (out) {
			if (title.equals("Foto")) {
				doc.appendHead("<link rel='stylesheet' type='text/css' href='../../Themes/styles.css'>");
				backmeuplogo = "../../Themes/backmeuplogo.jpg";
				facebooklogo = "../../Themes/facebooklogo.jpg";
				list_point = "../../Themes/list_point.jpg";
			} else {
				doc.appendHead("<link rel='stylesheet' type='text/css' href='../Themes/styles.css'>");
				backmeuplogo = "../Themes/backmeuplogo.jpg";
				facebooklogo = "../Themes/facebooklogo.jpg";
				list_point = "../Themes/list_point.jpg";
			}
		} else
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
		IMG fblogo = new IMG(facebooklogo);
		applic_logo.addElement(fblogo);
		content.addElement(applic_logo);

		Div applic_content_header = (Div) new Div().addAttribute("class",
				"applic_content_header");
		applic_content_header.addElement("my facebook: " + title);

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

	private String getGraphUrl(String path, String parameters) {
		if ((path == null) || (path.length() == 0)) {
			return null;
		}
		String url = "https://graph.facebook.com/" + path;
		if (parameters != null) {
			url += "?" + parameters;
		}
		return url;
	}

	/**
	 * this method downloads the user if it does not exist yet if
	 * DOWNLOAD_NON_FRIEND_USERS is not set then there will be no link (just the
	 * name)
	 */
	private String linkUser(String id, String name, String type,
			FacebookClient client, Storage storage, Progressable progr)
			throws DatasourceException, StorageException {

		String nameUser = name;

		if (id != null) {
			String path = getUserFilename(name + id);
			if (type.equals("photo") || type.equals("comment"))
				path = new String("../" + path);
			if (allUsers.contains(id)) {
				nameUser = "<a href = '../" + path + "'>" + name + "</a>";
			} else {
				if (DOWNLOAD_NON_FRIEND_USERS
						&& downloadUser(id, client, storage, progr) != null) {
					nameUser = "<a href = '../" + path + "'>" + name + "</a>";
				}
			}
		}
		return nameUser;
	}

	/**
	 * create a valid filename
	 * 
	 * @param str
	 * @return new string without illegal signs
	 */
	private String checkName(String str) {
		String[] illegal = { "\\", "|", "/", ":", "*", "?", "\"", "<", ">", "." };

		if (str.equals(".") || str.equals("..")) {
			str = "facebook";
		}
		/*for (int i = 0; i < str.length(); i++) {
			if ((str.charAt(i) < ' ' || str.charAt(i) > '~')) {
				// umlauts are allowed
				if (str.charAt(i) != 228 && str.charAt(i) != 196
						&& str.charAt(i) != 246 && str.charAt(i) != 214
						&& str.charAt(i) != 252 && str.charAt(i) != 220
						&& str.charAt(i) != 223) {
					str = str.replace(str.charAt(i), '-');
				}
			}
		}*/
		for (int i = 0; i < illegal.length; i++) {
			while (str.contains(illegal[i])) {
				str = str.replace(illegal[i], " ");
			}
		}

		return str;
	}

	
	@Override
	public List<String> getAvailableOptions(Properties accessData) {
		List<String> facebookBackupOptions = new ArrayList<String>();
		facebookBackupOptions.add("Profile");
		facebookBackupOptions.add("Friends");
		facebookBackupOptions.add("Friendslists");
		facebookBackupOptions.add("Groups");
		facebookBackupOptions.add("Sites");
		facebookBackupOptions.add("Posts");
		facebookBackupOptions.add("Photos");
		facebookBackupOptions.add("Albums");
		return facebookBackupOptions;
	}

	public void getThemes(Storage storage, Properties props)
			throws DatasourceException, StorageException {
		InputStream is;
		try {
			is = this.getClass().getResourceAsStream("/backmeuplogo.jpg");
			storage.addFile(is, "Themes/backmeuplogo.jpg", null);
			is = this.getClass().getResourceAsStream("/facebooklogo.jpg");
			storage.addFile(is, "Themes/facebooklogo.jpg", null);
			is = this.getClass().getResourceAsStream("/list_point.jpg");
			storage.addFile(is, "Themes/list_point.jpg", null);
			is = this.getClass().getResourceAsStream("/styles.css");
			storage.addFile(is, "Themes/styles.css", null);
			if(is!=null)
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
