package it.polimi.tiw.esameremoto.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.esameremoto.beans.Meeting;
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;
import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;


@WebServlet("/GetMeetings")
public class GetMeetings extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GetMeetings() {
        super();
    }

    public void init() throws ServletException {
        this.templateEngine = ServletUtils.createThymeleafTemplate(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        MeetingDAO meetingDAO = new MeetingDAO(connection);
        List<Meeting> meetings;


        try {
            meetings = meetingDAO.findMeetingsNotExpiredByUser(user.getUsername());
        } catch (SQLException e) {
            // for debugging only e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover meetings");
            return;
        }

        // Redirect to the Home page and add missions to the parameters
        String path = "/home.html";
        ServletContext servletContext = getServletContext();
        final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());

        ArrayList<Meeting> myMeetings = new ArrayList<>();
        ArrayList<Meeting> othersMeetings = new ArrayList<>();
        for (Meeting meeting : meetings){
            if (meeting.getUsernameCreator().equals(user.getUsername()))
                myMeetings.add(meeting);
            else
                othersMeetings.add(meeting);
        }
        
        webContext.setVariable("myMeetings", myMeetings);
        webContext.setVariable("othersMeetings", othersMeetings);
        
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage!=null) {
            webContext.setVariable("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        
        templateEngine.process(path, webContext, response.getWriter());

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
