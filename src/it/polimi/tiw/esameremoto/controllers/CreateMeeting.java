package it.polimi.tiw.esameremoto.controllers;

import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection = null;

    public CreateMeeting() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    private Date getMeYesterday() {
        return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If the user is not logged in (not present in session) redirect to the login
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            String loginpath = getServletContext().getContextPath() + "/index.html";
            response.sendRedirect(loginpath);
            return;
        }

        // Get and parse all parameters from request
        boolean isBadRequest = false;
        String name = null;
        Date date = null;
        String description = null;
        try {

            name = request.getParameter("name");
            //description = StringEscapeUtils.escapeJava(request.getParameter("description"));
            description = request.getParameter("description");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = (Date) sdf.parse(request.getParameter("date"));

            isBadRequest = name.isEmpty() || date==null;

        } catch (NumberFormatException | NullPointerException | ParseException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }

        // Create mission in DB
        User user = (User) session.getAttribute("user");
        MeetingDAO meetingDAO = new MeetingDAO(connection);
        try {
            meetingDAO.createMeeting(name, date, description, user.getUsername());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create mission");
            return;
        }

        // return the user to the right view
        String ctxpath = getServletContext().getContextPath();
        String path = ctxpath + "/Home";
        response.sendRedirect(path);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

