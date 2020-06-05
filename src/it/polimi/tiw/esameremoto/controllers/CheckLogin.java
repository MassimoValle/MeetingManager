package it.polimi.tiw.esameremoto.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// import it.polimi.tiw.esameremoto
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.UserDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;

// import thymeleaf
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import org.apache.commons.text.StringEscapeUtils;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public void init() throws ServletException {
		
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// obtain and escape params
		String username = null;
		String password = null;
		
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			if (username==null || password==null){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Credentials must be not null");
				return;
			}
			else if(username.isEmpty() || password.isEmpty()){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Credentials can't be empty.");
				return;
			}
		} catch (NullPointerException e) {
			e.printStackTrace(); //TODO da togliere a progetto finito
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// query db to authenticate for user
		UserDAO userDao = new UserDAO(connection);
		User user;
		
		try {
			user = userDao.checkUser(username, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
			return;
		}


		// If the user exists, add info to the session and go to home page, otherwise
		// show login page with error message

		String path;
		if (user == null) {
			
			ServletContext servletContext = getServletContext();
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("errorMsg", "Incorrect username or password");
			path = "/index.html";
			templateEngine.process(path, webContext, response.getWriter());
			
		} else {
			
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/GetMeetings";	//va alla GetMeetings Servlets
			response.sendRedirect(path);
			
		}

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
