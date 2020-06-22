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
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import it.polimi.tiw.esameremoto.beans.Meeting;
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;
import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;


@WebServlet("/GetMeetingDetails")
public class GetMeetingDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetMeetingDetails() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();

		// get and check params
		Integer meetingID = null;
		try {
			meetingID = Integer.parseInt(request.getParameter("meetingId"));
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}


		MeetingDAO meetingDAO = new MeetingDAO(connection);
		Meeting meeting = null;
		try {
			meeting = meetingDAO.findMeetingById(meetingID);
			if (meeting == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Resource not found");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover meeting");
			return;
		}

		String json = new Gson().toJson(meeting);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
