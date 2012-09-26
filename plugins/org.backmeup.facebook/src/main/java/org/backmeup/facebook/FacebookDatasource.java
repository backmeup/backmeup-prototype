package org.backmeup.facebook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.apache.ecs.html.Table;
import org.apache.ecs.xhtml.br;
import org.apache.ecs.xhtml.ul;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
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
 * groups, posts, friendslists and albums with photos
 * 
 * @author aschmiedhofer, mmurauer
 */
public class FacebookDatasource implements Datasource {

	private static final boolean DOWNLOAD_NON_FRIEND_USERS = true;

	private List<String> allUsers = new LinkedList<String>();

	@Override
	public void downloadAll(Properties props, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		String accessToken = props.getProperty(FacebookHelper.PROPERTY_TOKEN);
		FacebookClient client = new DefaultFacebookClient(accessToken);

		Document doc = createDocument("Index", "Facebook");

		progr.progress("Downloading User Profile...");
		doc.appendBody(new A(downloadUser("me", client, storage, progr),
				"My Profile"));
		doc.appendBody(new br());

		progr.progress("Downloading Friends...");
		downloadFriends(client, storage, progr);
		doc.appendBody(new A("friends.html", "Friends"));
		doc.appendBody(new br());

		progr.progress("Downloading Friendslists...");
		downloadFriendlists(client, storage, progr);
		doc.appendBody(new A("friendlists.html", "Friendlists"));
		doc.appendBody(new br());

		progr.progress("Downloading Groups...");
		downloadGroups(client, storage, progr);
		doc.appendBody(new A("groups.html", "Groups"));
		doc.appendBody(new br());

		progr.progress("Downloading Posts...");
		downloadPosts(client, storage, progr);
		doc.appendBody(new A("posts.html", "Posts"));
		doc.appendBody(new br());

		progr.progress("Downloading Photos...");
		downloadPhotos(client, storage, progr);
		doc.appendBody(new A("photos.html", "Photos"));
		doc.appendBody(new br());

		progr.progress("Downloading Albums...");
		downloadAlbums(client, storage, progr);
		doc.appendBody(new A("albums.html", "Albums"));
		doc.appendBody(new br());

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "index.html");
	}

	@Override
	public String getStatistics(Properties props) {
		return null;
	}

	private void downloadAlbums(FacebookClient client, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Albums", "Facebook - Albums");

		Connection<Album> albums = client.fetchConnection("me/albums",
				Album.class);
		do {
			for (Album album : albums.getData()) {
				doc.appendBody(new A(downloadAlbum(album, client, storage,
						progr), checkName(album.getName())));
				doc.appendBody(new br());
			}
		} while (albums.hasNext()
				&& (albums = client.fetchConnectionPage(
						albums.getNextPageUrl(), Album.class)) != null);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "albums.html");
	}

	private void downloadPhotos(FacebookClient client, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Photos", "Facebook - Photos");

		downloadPhotos("me", doc, client, storage, progr);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "photos.html");
	}

	/**
	 * this method only inserts a photo, photo is not saved as a document
	 * 
	 * @param id
	 *            can be a album or user
	 */
	private void downloadPhotos(String id, Document doc, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {

		Connection<Photo> photos = client.fetchConnection(id + "/photos",
				Photo.class);

		String parent = "";
		if (!id.equals("me"))
			parent = id;
		do {
			for (Photo photo : photos.getData()) {
				String piclink = downloadPhoto(photo, parent, client, storage,
						progr);
				if (!id.equals("me"))
					piclink = piclink.substring(7);
				doc.appendBody(new A(piclink, photo.getName() != null ? photo
						.getName().split("\n")[0] : "Photo"));
				doc.appendBody(new br());
			}
		} while (photos.hasNext()
				&& (photos = client.fetchConnectionPage(
						photos.getNextPageUrl(), Photo.class)) != null);
	}

	/**
	 * comments are added to the document
	 * 
	 * @param id
	 *            can be anything that has a connection "comments"
	 */
	private MetainfoContainer downloadComments(String id, String destination,
			String parent, String type, Document doc, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {
		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo commentinfo;
		Connection<Post> comments = client.fetchConnection(id + "/comments",
				Post.class);
		if (comments.getData() == null)
			return metainfo;
		if (comments.getData().size() == 0)
			return metainfo;
		doc.appendBody(new H2("Comments"));
		do {
			for (Post comment : comments.getData()) {

				commentinfo = new Metainfo();
				commentinfo.setAttribute("destination", destination);
				commentinfo.setAttribute("author", checkName(comment.getFrom()
						.getName()));
				commentinfo.setAttribute("message", comment.getMessage());
				commentinfo.setBackupDate(new Date());
				commentinfo.setId(comment.getId());
				if (comment.getUpdatedTime() != null)
					commentinfo.setModified(comment.getUpdatedTime());
				commentinfo.setParent(parent);
				commentinfo.setSource("facebook");
				commentinfo.setType("comment");

				linkUser(comment.getFrom().getId(), checkName(comment.getFrom()
						.getName()), type, doc, client, storage, progr);
				doc.appendBody(": " + comment.getMessage());
				doc.appendBody(new br());

				String likes = downloadLikes(comment.getId(), "comment", doc,
						client, storage, progr);

				doc.appendBody(new br());
				
				if (likes != null)
					commentinfo.setAttribute("likes", likes);

				metainfo.addMetainfo(commentinfo);
			}
		} while (comments.hasNext()
				&& (comments = client.fetchConnectionPage(
						comments.getNextPageUrl(), Post.class)) != null);
		return metainfo;
	}

	/**
	 * likes are added to the document
	 * 
	 * @param id
	 *            can be anything that has a connection "likes"
	 */
	private String downloadLikes(String id, String type, Document doc,
			FacebookClient client, StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {
		String likers = "";
		Connection<User> likes = client.fetchConnection(id + "/likes",
				User.class);

		if (likes.getData() == null)
			return null;
		if (likes.getData().size() == 0)
			return null;

		if (type == "comment") {
			doc.appendBody("Likes: ");
			doc.appendBody(new br());
		} else
			doc.appendBody(new H2("Likes"));

		do {
			for (User like : likes.getData()) {
				linkUser(like.getId(), checkName(like.getName()), type, doc,
						client, storage, progr);
				doc.appendBody(new br());
				likers += checkName(like.getName()) + " ";
			}
		} while (likes.hasNext()
				&& (likes = client.fetchConnectionPage(likes.getNextPageUrl(),
						User.class)) != null);

		return likers;
	}

	private void downloadGroups(FacebookClient client, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Groups", "Facebook - Groups");
		Connection<Group> groups = client.fetchConnection("me/groups",
				Group.class);
		do {
			for (Group group : groups.getData()) {
				doc.appendBody(new A(downloadGroup(group.getId(), client,
						storage, progr), checkName(group.getName())));
				doc.appendBody(new br());
			}
		} while (groups.hasNext()
				&& (groups = client.fetchConnectionPage(
						groups.getNextPageUrl(), Group.class)) != null);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "groups.html");
	}

	private void downloadPosts(FacebookClient client, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Wall", "Facebook - Wall");
		Connection<Post> posts = client.fetchConnection("me/feed", Post.class);
		do {
			for (Post post : posts.getData()) {
				if (post.getMessage() == null
						&& (post.getType() != "video"
								|| post.getType() != "link" || post.getType() != "photo")) {
					// ignoring everything which does not have a message (like
					// "xy got friend with yz")
					continue;
				}

				doc.appendBody(new A(getUserFilename(checkName(post.getFrom()
						.getName()) + post.getFrom().getId()), checkName(post
						.getFrom().getName())));
				doc.appendBody(": ");
				doc.appendBody(new A(
						downloadPost(post, client, storage, progr), post
								.getMessage().split("\n")[0]));

				doc.appendBody(new br());
			}
		} while (posts.hasNext()
				&& (posts = client.fetchConnectionPage(posts.getNextPageUrl(),
						Post.class)) != null);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "posts.html");
	}

	private void downloadFriends(FacebookClient client, StorageWriter storage,
			Progressable progr) throws DatasourceException, StorageException {

		Document doc = createDocument("Friends", "Facebook - Friends");

		Connection<User> friends = client.fetchConnection("me/friends",
				User.class);
		;
		do {
			for (User friend : friends.getData()) {
				doc.appendBody(new A(downloadUser(friend.getId(), client,
						storage, progr), checkName(friend.getName())));
				doc.appendBody(new br());
			}
		} while (friends.hasNext()
				&& (friends = client.fetchConnectionPage(
						friends.getNextPageUrl(), User.class)) != null);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "friends.html");
	}

	private void downloadFriendlists(FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {

		Document doc = createDocument("Friendlists", "Facebook - Friendlists");

		Connection<CategorizedFacebookType> lists = client.fetchConnection(
				"me/friendlists", CategorizedFacebookType.class);
		do {
			for (CategorizedFacebookType friendlist : lists.getData()) {
				Connection<User> members = client.fetchConnection(
						friendlist.getId() + "/members", User.class);
				
				if (members.getData().size() > 0) {
					doc.appendBody(new A(downloadFriendlist(friendlist.getId(),
							checkName(friendlist.getName()), client, storage,
							progr), checkName(friendlist.getName())));
					doc.appendBody(new br());
				}
			}
		} while (lists.hasNext()
				&& (lists = client.fetchConnectionPage(lists.getNextPageUrl(),
						CategorizedFacebookType.class)) != null);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		storage.addFile(is, "friendlists.html");
	}

	private String downloadFriendlist(String id, String name,
			FacebookClient client, StorageWriter storage, Progressable progr)
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
		Document doc = createDocument(name, "Facebook - Friendlist");

		doc.appendBody("Name: " + name);
		doc.appendBody(new H2("Members"));

		// download members
		Connection<User> members = client.fetchConnection(id + "/members",
				User.class);
		do {
			for (User member : members.getData()) {
				doc.appendBody(new A("../"
						+ getUserFilename(checkName(member.getName())
								+ member.getId()), checkName(member.getName())));
				doc.appendBody(new br());

				listmembers += checkName(member.getName()) + " ";
			}
		} while (members.hasNext()
				&& (members = client.fetchConnectionPage(
						members.getNextPageUrl(), User.class)) != null);

		listinfo.setAttribute("members", listmembers);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getFriendlistFilename(name + id);
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadPost(Post post, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {

		Metainfo postinfo = new Metainfo();
		postinfo.setAttribute("author", checkName(post.getFrom().getName()));
		postinfo.setAttribute("message", post.getMessage());
		postinfo.setBackupDate(new Date());
		postinfo.setDestination(getPostFilename(post.getId()));
		postinfo.setId(post.getId());
		postinfo.setSource("facebook");

		if (post.getUpdatedTime() != null)
			postinfo.setModified(post.getUpdatedTime());
		postinfo.setType("post");

		Document doc = createDocument("Post", "Facebook - Post");

		doc.appendBody("Type: " + post.getType());
		doc.appendBody(new br());
		doc.appendBody("From: ");
		linkUser(post.getFrom().getId(), checkName(post.getFrom().getName()),
				"post", doc, client, storage, progr);

		if (post.getTo() != null) {
			String to = post.getTo().toString();
			String[] toArray = to.split("=");
			if (toArray.length > 2) {
				int index = toArray[3].lastIndexOf(" ");
				String toName = checkName(toArray[3].substring(0, index));
				postinfo.setAttribute("receiver", toName);
				doc.appendBody(new br());
				doc.appendBody("To: " + toName);
			}
		}

		doc.appendBody(new br());
		doc.appendBody("Time: " + post.getCreatedTime());
		doc.appendBody(new br());
		doc.appendBody("Message: " + post.getMessage());
		doc.appendBody(new br());
		if (post.getName() != null) {
			doc.appendBody("Name: " + post.getName());
			doc.appendBody(new br());
		}
		if (post.getDescription() != null) {
			doc.appendBody("Description: " + post.getDescription());
			doc.appendBody(new br());
		}
		if (post.getLink() != null) {
			doc.appendBody("Link: ").appendBody("<a href="+post.getLink()+" target='_blank'>" +
					post.getLink()+"</a>");
			doc.appendBody(new br());
		}
		if (post.getSource() != null) {
			doc.appendBody("Source: " + post.getSource());
			doc.appendBody(new br());
		}

		MetainfoContainer metainfo = downloadComments(post.getId(),
				getPostFilename(post.getId()), getPostFilename(post.getId()),
				"post", doc, client, storage, progr);

		String likes = downloadLikes(post.getId(), "post", doc, client,
				storage, progr);

		if (likes != null)
			postinfo.setAttribute("likes", likes);
		metainfo.addMetainfo(postinfo);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getPostFilename(post.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadAlbum(Album album, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {
		String name = checkName(album.getName());

		Metainfo albuminfo = new Metainfo();
		albuminfo.setAttribute("name", name);
		albuminfo.setBackupDate(new Date());
		albuminfo.setDestination(getAlbumFilename(name + album.getId()));
		albuminfo.setId(album.getId());
		if (album.getUpdatedTime() != null)
			albuminfo.setModified(album.getUpdatedTime());
		albuminfo.setSource("facebook");
		albuminfo.setType("album");

		Document doc = createDocument(name, "Facebook - Album");

		doc.appendBody("Name: " + name);
		doc.appendBody(new br());
		if (album.getDescription() != null) {
			doc.appendBody("Description: " + album.getDescription());
			doc.appendBody(new br());

			albuminfo.setAttribute("description", album.getDescription());
		}
		if (album.getLocation() != null) {
			doc.appendBody("Location: " + album.getLocation());
			doc.appendBody(new br());
		}
		if (album.getCreatedTime() != null) {
			doc.appendBody("Created on " + album.getCreatedTime());
			doc.appendBody(new br());
		}
		if (album.getLink() != null) {
			doc.appendBody("Link: ").appendBody("<a href="+album.getLink()+" target='_blank'>" +
					album.getLink()+"</a>");
			doc.appendBody(new br());
		}

		doc.appendBody(new H2("Photos"));
		downloadPhotos(album.getId(), doc, client, storage, progr);

		MetainfoContainer metainfo = downloadComments(album.getId(),
				getAlbumFilename(name + album.getId()), getAlbumFilename(name
						+ album.getId()), "album", doc, client, storage, progr);

		String likes = downloadLikes(album.getId(), "album", doc, client,
				storage, progr);

		if (likes != null)
			albuminfo.setAttribute("likes", likes);

		metainfo.addMetainfo(albuminfo);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getAlbumFilename(name + album.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadPhoto(Photo photo, String parent,
			FacebookClient client, StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {

		Metainfo photoinfo = new Metainfo();

		photoinfo.setBackupDate(new Date());
		photoinfo.setId(photo.getId());
		photoinfo.setModified(photo.getUpdatedTime());
		photoinfo.setSource("facebook");
		photoinfo.setType("photo");
		if (!parent.equals(""))
			photoinfo.setParent(parent);

		Document doc = createDocument("Photo", "Facebook - Photo");

		if (photo.getName() != null) {
			doc.appendBody("Caption: " + photo.getName());
			doc.appendBody(new br());

			photoinfo.setAttribute("name", photo.getName());
		}
		if (photo.getFrom() != null) {
			doc.appendBody("From: ");
			linkUser(photo.getFrom().getId(), checkName(photo.getFrom()
					.getName()), "photo", doc, client, storage, progr);
			doc.appendBody(new br());
		}
		if (photo.getCreatedTime() != null) {
			doc.appendBody("Created on " + photo.getCreatedTime());
			doc.appendBody(new br());
		}
		if (photo.getLink() != null) {
			doc.appendBody("Link: ").appendBody("<a href="+photo.getLink()+" target='_blank'>" +
					photo.getLink()+"</a>");
			doc.appendBody(new br());
		}
		if (photo.getSource() == null) {
			throw new DatasourceException("error while downloading photos");
		}
		doc.appendBody("Source: ").appendBody("<a href="+photo.getSource()+" target='_blank'>" +
				photo.getSource()+"</a>");
		doc.appendBody(new br());

		String ending = ".jpg";// only jpg supported
		String sourceFileName = "Albums/Photos/" + photo.getId() + ending;
		downloadPicture(photo.getSource(), sourceFileName, storage, progr,
				photoinfo);

		photoinfo.setDestination("Albums/Photos/" + photo.getId() + ".html");

		sourceFileName = sourceFileName.substring(14);

		doc.appendBody(new IMG(sourceFileName));
		String tags = "";

		if ((photo.getTags() != null) && (photo.getTags().size() > 0)) {
			doc.appendBody(new H2("Tags"));
			for (Tag tag : photo.getTags()) {
				linkUser(tag.getId(), checkName(tag.getName()), "photo", doc,
						client, storage, progr);
				doc.appendBody(new br());

				tags += checkName(tag.getName()) + " ";
			}
		}

		if (tags != "")
			photoinfo.setAttribute("tags", tags);

		MetainfoContainer metainfo = downloadComments(photo.getId(),
				getPhotoFilename(photo.getId()),
				getPhotoFilename(photo.getId()), "photo", doc, client, storage,
				progr);

		String likes = downloadLikes(photo.getId(), "photo", doc, client,
				storage, progr);

		if (likes != null)
			photoinfo.setAttribute("likes", likes);

		metainfo.addMetainfo(photoinfo);

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getPhotoFilename(photo.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadGroup(String id, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {
		Group g = client.fetchObject(id, Group.class);
		String name = checkName(g.getName());

		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo groupinfo = new Metainfo();
		groupinfo.setAttribute("name", name);

		groupinfo.setBackupDate(new Date());
		groupinfo.setDestination(getGroupFilename(name + g.getId()));
		groupinfo.setId(g.getId());
		groupinfo.setModified(g.getUpdatedTime());
		groupinfo.setSource("facebook");
		groupinfo.setType("group");
		metainfo.addMetainfo(groupinfo);

		Document doc = createDocument(name, "Facebook - Group");

		doc.appendBody("Groupname: " + name);
		doc.appendBody(new br());
		if (g.getDescription() != null) {
			doc.appendBody("Description: " + g.getDescription());
			doc.appendBody(new br());

			groupinfo.setAttribute("description", g.getDescription());
		}
		if (g.getPrivacy() != null) {
			doc.appendBody("Privacy: " + g.getPrivacy());
			doc.appendBody(new br());
		}
		if (g.getOwner() != null) {
			doc.appendBody("Owner: ");
			linkUser(g.getOwner().getId(), checkName(g.getOwner().getName()),
					"group", doc, client, storage, progr);
			doc.appendBody(new br());

			groupinfo.setAttribute("owner", checkName(g.getOwner().getName()));
		}
		if (g.getLink() != null) {
			doc.appendBody("Link: ")
					.appendBody("<a href="+g.getLink()+" target='_blank'>" +
							g.getLink()+"</a>");
			doc.appendBody(new br());
		}

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getGroupFilename(name + g.getId());
		storage.addFile(is, filename, metainfo);
		return filename;
	}

	private String downloadUser(String id, FacebookClient client,
			StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {

		MetainfoContainer metainfo = new MetainfoContainer();
		Metainfo userinfo = new Metainfo();
		userinfo.setBackupDate(new Date());

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
				storage, progr);

		userinfo.setAttribute("profilePicture", "Friends/"+pic);
		metainfo.addMetainfo(userinfo);

		// create HTML
		Document doc = createDocument(name, "Facebook - User");

		doc.appendBody("Name: " + name);
		doc.appendBody(new br());

		if (u.getUsername() != null) {
			doc.appendBody("Username: " + u.getUsername());
			doc.appendBody(new br());
		}
		if (u.getEmail() != null) {
			doc.appendBody("Email: " + u.getEmail());
			doc.appendBody(new br());
		}
		doc.appendBody(new IMG(pic));
		doc.appendBody(new br());

		if (u.getAbout() != null) {
			doc.appendBody("About: " + u.getAbout());
			doc.appendBody(new br());
		}
		if (u.getBio() != null) {
			doc.appendBody("Bio: " + u.getBio());
			doc.appendBody(new br());
		}
		if (u.getGender() != null) {
			doc.appendBody("Gender: " + u.getGender());
			doc.appendBody(new br());
		}
		if (u.getBirthday() != null) {
			doc.appendBody("Birthday: " + u.getBirthday());
			doc.appendBody(new br());
		}
		if (u.getHometownName() != null) {
			doc.appendBody("Hometown: " + u.getHometownName());
			doc.appendBody(new br());
		}
		if (u.getLocation() != null) {
			doc.appendBody("Location: " + u.getLocation().getName());
			doc.appendBody(new br());
		}
		if ((u.getLanguages() != null) && (u.getLanguages().size() > 0)) {
			doc.appendBody("Languages: ");
			String[] languages = new String[u.getLanguages().size()];
			int i = 0;
			for (NamedFacebookType language : u.getLanguages()) {
				languages[i] = language.getName();
				i++;
			}
			doc.appendBody(new ul(languages));
			doc.appendBody(new br());
		}
		if ((u.getEducation() != null) && (u.getEducation().size() > 0)) {
			doc.appendBody("Education: ");
			String[] edus = new String[u.getEducation().size()];
			int i = 0;
			for (Education educ : u.getEducation()) {
				edus[i] = educ.getType()
						+ (educ.getSchool() != null ? ": "
								+ educ.getSchool().getName()
								+ (educ.getYear() != null ? " until "
										+ educ.getYear().getName() : "") : "");
				i++;
			}
			doc.appendBody(new ul(edus));
			doc.appendBody(new br());
		}
		if ((u.getWork() != null) && (u.getWork().size() > 0)) {
			doc.appendBody("Work: ");
			String[] works = new String[u.getWork().size()];
			int i = 0;
			for (Work work : u.getWork()) {
				works[i] = (work.getDescription() != null ? work
						.getDescription() : "")
						+ (work.getPosition() != null ? " as "
								+ work.getPosition() : "")
						+ (work.getEmployer() != null ? " for "
								+ work.getEmployer().getName() : "")
						+ (work.getLocation() != null ? " at "
								+ work.getLocation().getName() : "");
				i++;
			}
			doc.appendBody(new ul(works));
			doc.appendBody(new br());
		}
		if ((u.getInterestedIn() != null) && (u.getInterestedIn().size() > 0)) {
			doc.appendBody("Interested in: ");
			doc.appendBody(new ul(u.getInterestedIn().toArray(new String[0])));
			doc.appendBody(new br());
		}
		if ((u.getMeetingFor() != null) && (u.getMeetingFor().size() > 0)) {
			doc.appendBody("Meeting for: ");
			doc.appendBody(new ul(u.getMeetingFor().toArray(new String[0])));
			doc.appendBody(new br());
		}
		if (u.getRelationshipStatus() != null) {
			doc.appendBody("Relationship status: " + u.getRelationshipStatus());
			doc.appendBody(new br());
		}
		if (u.getSignificantOther() != null) {
			doc.appendBody("Significant other: ");
			doc.appendBody(new A("../"
					+ getUserFilename(checkName(u.getSignificantOther()
							.getName()) + u.getSignificantOther().getId()),
					checkName(u.getSignificantOther().getName())));
			doc.appendBody(new br());
		}
		if (u.getQuotes() != null) {
			doc.appendBody("Quotes: " + u.getQuotes());
			doc.appendBody(new br());
		}
		if (u.getReligion() != null) {
			doc.appendBody("Religion: " + u.getReligion());
			doc.appendBody(new br());
		}
		if (u.getPolitical() != null) {
			doc.appendBody("Political: " + u.getPolitical());
			doc.appendBody(new br());
		}
		if (u.getWebsite() != null) {
			doc.appendBody("Website: ").appendBody(
					new A(u.getWebsite(), u.getWebsite()));
			doc.appendBody(new br());
		}
		if (u.getLink() != null) {
			doc.appendBody("Link: ")
					.appendBody("<a href="+u.getLink()+" target='_blank'>" +
							u.getLink()+"</a>");
			;
			doc.appendBody(new br());
		}

		InputStream is = new ByteArrayInputStream(doc.toString().getBytes());
		String filename = getUserFilename(name + u.getId());
		storage.addFile(is, filename, metainfo);

		allUsers.add(id);
		return filename;
	}

	private String getUserFilename(String id) {
		return "Friends/" + id + ".html";
	}

	private String getPostFilename(String id) {
		return "Posts/" + id + ".html";
	}

	private String getGroupFilename(String id) {
		return "Groups/" + id + ".html";
	}

	private String getAlbumFilename(String id) {
		return "Albums/" + id + ".html";
	}

	private String getPhotoFilename(String id) {
		return "Albums/Photos/" + id + ".html";
	}

	private String getFriendlistFilename(String id) {
		return "Friendlists/" + id + ".html";
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
	private String downloadProfilePicture(String name, String id,
			StorageWriter storage, Progressable progr) throws StorageException {
		String fileName = "";
		if (name != "me")
			fileName = "Friends/Photos/" + name + ".jpg";
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
		uPicLoc = getGraphUrl(id + "/picture", "type=large");
		if (uPicLoc != null) {
			downloadPicture(uPicLoc, fileName, storage, progr, photoinfo);
		} else {
			progr.progress("no picture URL...");
		}
		return fileName.substring(8);
	}

	private boolean downloadPicture(String path, String destination,
			StorageWriter storage, Progressable progr, Metainfo photoinfo)
			throws StorageException {

		MetainfoContainer metainfo = new MetainfoContainer();
		photoinfo.setDestination(destination);

		metainfo.addMetainfo(photoinfo);

		HttpURLConnection c = null;
		progr.progress("Downloading " + path + " to " + destination);
		try {
			URL url = new URL(path);
			c = (HttpURLConnection) url.openConnection();
			c.connect();
			if (c.getContentType().equals("image/jpeg")) {
				InputStream is = c.getInputStream();
				storage.addFile(is, destination, metainfo);
				return true;
			} else {
				progr.progress(c.getContentType() + " is not a jpg");
			}
		} catch (IOException e) {
			if (c != null)
				c.disconnect();
		}
		return false;
	}

	private Document createDocument(String title, String header) {
		Document doc = (Document) new Document();
		doc.appendHead("<meta http-equiv='content-type' content='text/html; charset=UTF-8' />");

		doc.appendTitle(title);
		doc.appendBody(new Table().addElement(
				new TD().addElement(new IMG(new FacebookDescriptor()
						.getImageURL()).setHeight(50).setWidth(50)))
				.addElement(new TD().addElement(new H1(header))));
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
	private void linkUser(String id, String name, String type, Document doc,
			FacebookClient client, StorageWriter storage, Progressable progr)
			throws DatasourceException, StorageException {
		if (id != null) {
			String path = getUserFilename(name + id);
			if (type.equals("photo") || type.equals("comment"))
				path = new String("../" + path);
			if (allUsers.contains(id)) {
				doc.appendBody(new A("../" + path, name));
			} else {
				if (DOWNLOAD_NON_FRIEND_USERS) {
					downloadUser(id, client, storage, progr);
					doc.appendBody(new A("../" + path, name));
				} else {
					doc.appendBody(name);
				}
			}
		}
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
		for (int i = 0; i < str.length(); i++) {
			if ((str.charAt(i) < ' ' || str.charAt(i) > '~')) {
				// umlauts are allowed
				if (str.charAt(i) != 228 && str.charAt(i) != 196
						&& str.charAt(i) != 246 && str.charAt(i) != 214
						&& str.charAt(i) != 252 && str.charAt(i) != 220
						&& str.charAt(i) != 223) {
					str = str.replace(str.charAt(i), '-');
				}
			}
		}
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
		facebookBackupOptions.add("Photos");
		facebookBackupOptions.add("Albums");
		return facebookBackupOptions;
	}
}
