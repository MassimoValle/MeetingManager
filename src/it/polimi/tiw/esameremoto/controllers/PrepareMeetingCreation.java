package it.polimi.tiw.esameremoto.controllers;

import it.polimi.tiw.esameremoto.beans.Meeting;
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.UserDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;
import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


@WebServlet("/PrepareMeetingCreation")
@MultipartConfig
public class PrepareMeetingCreation extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection = null;
    private TemplateEngine templateEngine;
    
    public PrepareMeetingCreation() {
        super();
    }
    
    public void init() throws ServletException {
        this.templateEngine = ServletUtils.createThymeleafTemplate(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object tempUsersChosen = session.getAttribute("usersChosenUsernames");
        ArrayList<String> usersChosen;
        
        if (tempUsersChosen==null)
            usersChosen = new ArrayList<>();
        else
            usersChosen = (ArrayList<String>) tempUsersChosen;
        
        String usernameSelected = request.getParameter("username");
        if (usersChosen.contains(usernameSelected))
            usersChosen.remove(usernameSelected);
        else
            usersChosen.add(request.getParameter("username"));
        
        session.setAttribute("usersChosenUsernames", usersChosen);
    
        WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process("/anagrafica.html", webContext, response.getWriter());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get and parse all parameters from request
        boolean isBadRequest = false;
        String title = null;
        Date date = null;
        Time hour = null;
        Integer duration = null;
        Integer maxParticipantsNumber = null;
        
        try {

            title = request.getParameter("title");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = simpleDateFormat.parse(request.getParameter("date"));
            hour = Time.valueOf(request.getParameter("hour")+":00");
            duration = Integer.parseInt(request.getParameter("duration"));
            maxParticipantsNumber = Integer.parseInt(request.getParameter("maxParticipantsNumber"));
            
            Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

            isBadRequest = title==null || title.isEmpty() || date == null || date.before(todayDate);

        } catch (IllegalArgumentException | NullPointerException | ParseException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            request.getSession().setAttribute("errorMessage", "Ops! Qualcosa e' andato storto. Ricompilare il form.");
            response.sendRedirect("GetMeetings");
            return;
        }
    
        // save the meeting that will be created if everything goes well
        Meeting meetingToCreate = new Meeting();
        meetingToCreate.setTitle(title);
        meetingToCreate.setDate(date);
        meetingToCreate.setHour(hour);
        meetingToCreate.setDuration(duration);
        meetingToCreate.setMaxParticipantsNumber(maxParticipantsNumber);
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        meetingToCreate.setUsernameCreator(user.getUsername());
        
        session.setAttribute("meetingToCreate", meetingToCreate);
        
        // get all the users in order to let the client choose who can participate
        UserDAO userDAO = new UserDAO(connection);
        ArrayList<User> users;
        try {
            users = userDAO.getUsers();
        } catch (SQLException e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get the users");
            e.printStackTrace();
            return;
        }
        session.setAttribute("users", users);
        session.setAttribute("attempts", 1);
        
        WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process("/anagrafica.html", webContext, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

