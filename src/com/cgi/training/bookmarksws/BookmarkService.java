package com.cgi.training.bookmarksws;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.cgi.training.bookmarksws.Site;
import com.cgi.training.bookmarksws.User;

public class BookmarkService {

	static boolean userNotFound = false;
	static boolean siteNotFound = false;
	static boolean newSiteCreated = false;

	public static final Logger LOGGER = Logger.getLogger(BookmarkService.class.getName());

	/**
	 * Connection to the MySQL database
	 * 
	 * @return conn
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection getConnection() throws SQLException, ClassNotFoundException {
		// TODO plutot utiliser un pool de connection
		// (https://github.com/brettwooldridge/HikariCP)

		// Verifier si un user associe a userId existe
		Connection conn = null;
		String url = "jdbc:mysql://localhost:3306";
		Properties connectionProps = new Properties();
		connectionProps.put("user", "root"); // dans un vrai projet, utiliser les credentials de votre application
		connectionProps.put("password", "");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, connectionProps);
		} catch (SQLException ex) {
			// TODO en garder une trace
			LOGGER.log(Level.SEVERE, "Impossible to establish a database connection", ex);
			throw ex;
		}
		return conn;
	}

	public User mapResultSetToUser(ResultSet result) throws SQLException {
		User user = new User();
		user.setId(result.getInt("id"));
		user.setUsername(result.getString("username"));

		return user;
	}
	
	private Site mapResultSetToSite(ResultSet result) throws SQLException {
		Site site = new Site();
		site.setId(result.getInt("id"));
		site.setUrl(result.getString("url"));

		return site;
	}

	public Bookmark mapResultSetToBookmark(ResultSet result) throws SQLException {
		Bookmark bookmark = new Bookmark();
		bookmark.setId(result.getInt("id"));

		User user = new User();
		user.setId(result.getInt("userId"));
		user.setUsername(result.getString("username"));
		bookmark.setUser(user);

		// Site site = bookmarkService.findSiteById(result.getInt("site_id"))
		Site site = new Site();
		site.setId(result.getInt("siteId"));
		site.setUrl(result.getString("url"));
		bookmark.setSite(site);

		bookmark.setDescription(result.getString("description"));
		bookmark.setTimeStamp(result.getTimestamp("created_at").toLocalDateTime());

		return bookmark;
	}

	/**
	 * Fetch a User for a given id
	 * 
	 * @param userId
	 * @return The user associated to the id
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public User findUserById(int userId) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bookmarks.user WHERE id = ?");
		stmt.setInt(1, userId);

		ResultSet result = stmt.executeQuery();

		if (!result.next()) {
			LOGGER.log(Level.WARNING, "findUserById: " + userId);
			throw new UserDoesNotExistException(" " + userId);
//			userNotFound = true;
//			throw new WebApplicationException(400);
		}

		User user = mapResultSetToUser(result);

		conn.close();

		return user;
	}
	
	/**
	 * Fetch a site entry for a given url
	 * 
	 * @param url
	 * @return The site associated to the url
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Site findSiteIdByURL(String url) throws ClassNotFoundException, SQLException {
		Connection conn = getConnection();

		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bookmarks.site WHERE url = ?");
		stmt.setString(1, url);

		ResultSet result = stmt.executeQuery();
		
		// If url doesn't already exist in the database, add it in a new entry
		if (!result.next()) {
			LOGGER.log(Level.INFO, "findSiteIdByURL: " + url + " doesn't exist in the database... Creating a new entry");
			createSite(url);
			newSiteCreated = true;
			conn.close();
			return null;
		} else {
			Site site = mapResultSetToSite(result);	
			conn.close();
			return site;
		}
	}

	/**
	 * Fetch all the bookmarks for a given user
	 * 
	 * @param userId : (int) The user from whom to fetch the bookmarks
	 * @return a list containing the user's bookmarks. If the user has no bookmarks, return an empty list
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws RuntimeException if the userId does not correspond to an existing user
	 */
	public List<Bookmark> findAllBookmarks(int userId) throws SQLException, ClassNotFoundException {
		User u = findUserById(userId);

		Connection conn = getConnection();

		try {
			PreparedStatement stmt = conn.prepareStatement(
					"SELECT B.id as id, B.description as description, B.created_at as created_at , U.id as userId, U.username as username, S.id as siteId, S.url as url FROM bookmarks.bookmark AS B INNER JOIN bookmarks.user as U ON B.user_id = U.id INNER JOIN bookmarks.site as S ON B.site_id = S.id WHERE user_id = ?");
			stmt.setInt(1, userId);

			ResultSet result = stmt.executeQuery();

			List<Bookmark> bookmarks = new ArrayList<>();

			while (result.next()) {
				Bookmark bookmark = mapResultSetToBookmark(result);
				bookmarks.add(bookmark);
			}
			return bookmarks;
//			return "{\"Status\" : \"OK\"},{\"url\" : \"user/\" + + \"/bookmarks\"}";
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Problem while calling findAllBookmarks", ex);
			throw ex;
		} finally {
			conn.close();
		}

	}
	
	/**
	 * Create a site entry for a given url
	 *  
	 * @param url
	 * @return null
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private Site createSite(String url) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		
		try {			
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookmarks.site (url) VALUES (?)");
			stmt.setString(1, url);
			int result = stmt.executeUpdate();
		} catch(SQLException ex) {
			LOGGER.log(Level.SEVERE, "Problem while calling createSite", ex);
			throw ex;
		} finally {
			conn.close();			
		}
		
		return null;
	}
	
	/**
	 * Create a bookmark for given parameters
	 * @param userId : int, The user from whom to fetch the bookmarks
	 * @param url : String, The URL to assign to the new bookmark
	 * @param desc : String, The description of the new bookmark
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public  Bookmark createBookmark(int userId, String url, String desc) throws SQLException, ClassNotFoundException {
		User u = findUserById(userId);
		
		// TODO Call a method to create a user if it doesn't already exist
		
		// Call the method to find a site by its URL
		Site s = findSiteIdByURL(url);
		if(newSiteCreated) { // In case the url had to be added to the DB, recall the method
			s = findSiteIdByURL(url);
		}

				Connection conn = getConnection();
		try {
			
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookmarks.bookmark (user_id, site_id, description) VALUES (?,?,?)");
			stmt.setInt(1, userId);
			stmt.setInt(2, s.getId());
			stmt.setString(3, desc);

			int result = stmt.executeUpdate();
		} catch(SQLException ex) {
			LOGGER.log(Level.SEVERE, "Problem while calling createBookmark", ex);
			throw ex;
		} finally {
			conn.close();
		}
		
		return null;
		
	}

}
