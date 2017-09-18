package com.cgi.training.bookmarksws;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.cgi.training.bookmarksws.Bookmark;
import com.cgi.training.bookmarksws.BookmarkService;
import com.cgi.training.bookmarksws.UserDoesNotExistException;

@Path("")
public class BookmarksResource {
	List<Bookmark> bookmarks = new ArrayList<>();
	
	/**
	 * Method called after a GET request to fetch all the bookmarks for a given user
	 * 
	 * @param id : (int) user id
	 * @return A list of all the bookmarks for a given user
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Path("user/{id}/bookmarks")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getBookmarksById(@PathParam("id") int id) throws ClassNotFoundException, SQLException {
		if(id < 0) {
			throw new WebApplicationException(404);
		}

//		try {
			BookmarkService bs = new BookmarkService();
			try {
				bookmarks = bs.findAllBookmarks(id);
//				System.out.println(bookmarks);
				return bookmarks.toString();
			} catch (UserDoesNotExistException ex) {
				// System.out.println("L'utilisateur n'existe pas " + ex.getMessage());
				throw new WebApplicationException(404);
			}
//		} catch (Exception e) {
//			if (BookmarkService.userNotFound) {
//				throw new WebApplicationException(400);
//			} else {
//				throw new WebApplicationException(500);
//			}
//		}
		
	}

	/**
	 * Method called after a POST request to add a bookmark for a given user
	 * 
	 * @param userId : (int) The user from whom to fetch the bookmarks
	 * @param site : (String) The URL to assign to the new bookmark
	 * @param desc : (String) The description of the new bookmark
	 * @return 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Path("user/{id}/bookmarks")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String postBookmark(
			@FormParam("userId") int userId,
			@FormParam("site") String site,
			@FormParam("desc") String desc
			) throws ClassNotFoundException, SQLException {
		
		if(userId < 0) {
			throw new WebApplicationException(404);
		}
		
		if(site == null || site.isEmpty()) {
			throw new WebApplicationException(400);
		}
		
		// Call the method to create a bookmark
		try {
		BookmarkService bs = new BookmarkService();
		Bookmark bookmark = bs.createBookmark(userId, site, desc);
		
		return "{ \"id\": " + bookmark.getId() + ", \"url\": \"" + bookmark.getSite().getUrl() + "\", \"description\": \""+ bookmark.getDescription() + "\"}";

		} catch(UserDoesNotExistException ex) {
			throw new WebApplicationException(404);
		}


	}
	
}
