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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;


@WebServlet("/GetMeetings")
public class GetMeetings extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GetMeetings() {
        super();
    }

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If the user is not logged in (not present in session) redirect to the login
        String loginPath = getServletContext().getContextPath() + "/index.html";
        HttpSession session = request.getSession();

        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(loginPath);
            return;
        }

        User user = (User) session.getAttribute("user");
        MeetingDAO meetingDAO = new MeetingDAO(connection);
        List<Meeting> meetings;

        try {
            meetings = meetingDAO.findMeetingsByUser(user.getUsername());
        } catch (SQLException e) {
            // for debugging only e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover missions");
            return;
        }

        // Redirect to the Home page and add missions to the parameters
        String path = "/home.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
    
        ArrayList<Meeting> myMeetings = new ArrayList<>();
        ArrayList<Meeting> othersMeetings = new ArrayList<>();
        for (Meeting meeting : meetings){
            if (meeting.getUsernameCreator().equals(user.getUsername()))
                myMeetings.add(meeting);
            else
                othersMeetings.add(meeting);
        }
        ctx.setVariable("myMeetings", myMeetings);
        ctx.setVariable("othersMeetings", othersMeetings);
        templateEngine.process(path, ctx, response.getWriter());
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
