package it.polimi.tiw.esameremoto.controllers;

import it.polimi.tiw.esameremoto.beans.Meeting;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;
import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private Connection connection = null;
    private TemplateEngine templateEngine;
    
    public void init() throws ServletException {
        this.templateEngine = ServletUtils.createThymeleafTemplate(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("doGet CreateMeeting");
        HttpSession session = request.getSession();
        Object tempUsersChosen = session.getAttribute("usersChosenUsernames");
        MeetingDAO meetingDAO = new MeetingDAO(connection);
        ArrayList<String> usersChosen;
    
        Meeting meeting = (Meeting) session.getAttribute("meetingToCreate");
        Time hour = meeting.getHour();
        Date date = meeting.getDate();
        String title = meeting.getTitle();
        int duration = meeting.getDuration();
        int maxParticipantsNumber = meeting.getMaxParticipantsNumber();
        String usernameCreator = meeting.getUsernameCreator();
    
        try {
            if (tempUsersChosen==null)
                meetingDAO.createMeeting(title, date, hour, duration, maxParticipantsNumber, usernameCreator, null);
            else {
                usersChosen = (ArrayList<String>) tempUsersChosen;
                
                if (usersChosen.size() > maxParticipantsNumber) {
                    session.setAttribute("errorMessage", "Troppi utenti selezionati, eliminarne almeno "
                            + (usersChosen.size()-maxParticipantsNumber));
                    
                    int attempts = (int) session.getAttribute("attempts");
                    if (attempts==3) {
                        session.removeAttribute("meetingToCreate");
                        session.removeAttribute("usersChosenUsernames");
                        session.removeAttribute("attempts");
                        session.removeAttribute("users");
                        session.removeAttribute("errorMessage");
                        response.sendRedirect("cancellazione.html");
                        return;
                    }
                    
                    session.setAttribute("attempts", attempts+1);
                    
                    WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
                    templateEngine.process("/anagrafica.html", webContext, response.getWriter());
                    return;
                }
    
                meetingDAO.createMeeting(title, date, hour, duration, maxParticipantsNumber, usernameCreator, usersChosen);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create mission");
            e.printStackTrace();
        }
        
        session.removeAttribute("meetingToCreate");
        session.removeAttribute("usersChosenUsernames");
        session.removeAttribute("attempts");
        session.removeAttribute("users");
        session.removeAttribute("errorMessage");
        
        response.sendRedirect("GetMeetings");
    }
}
