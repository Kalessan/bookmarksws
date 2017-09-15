package com.cgi.training.bookmarksws;

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

@Path("user/bookmarks")
public class BookmarksResource {
	List<Bookmark> bookmarks = new ArrayList<>();
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getBookmarksById(@PathParam("id") int id) {
		

		try {
			BookmarkService bs = new BookmarkService();
			try {
				bookmarks = bs.findAllBookmarks(id);
				System.out.println(bookmarks);
			} catch (UserDoesNotExistException ex) {
				// System.out.println("L'utilisateur n'existe pas " + ex.getMessage());
				throw new WebApplicationException(400);
			}
		} catch (Exception e) {
			if (BookmarkService.userNotFound) {
				throw new WebApplicationException(400);
			} else {
				throw new WebApplicationException(500);
			}
		}
		return bookmarks.toString();
	}

//	@Path("bookmarks")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void postBookmark(
			@FormParam("userId") int userId,
			@FormParam("site") String site,
			@FormParam("desc") String desc
			) {
		
		if(userId == 0 || site == null) {
			throw new WebApplicationException(400);
		}
		
		// TODO Si site n'existe pas, le créer.
		
		// TODO Si site n'existe pas, le créer.

		// Appel de la méthode de création de bookmark
		BookmarkService.createBookmark(userId, site, desc);

	}
	
}
