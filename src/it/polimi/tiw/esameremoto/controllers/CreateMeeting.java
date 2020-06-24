package it.polimi.tiw.esameremoto.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@WebServlet("/CreateMeeting")
@MultipartConfig
public class CreateMeeting extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private Connection connection = null;
    
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("doGet CreateMeeting");
        
        String json_newMeetingParameters = request.getHeader("newMeetingParameters");
        Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
        Map<String, Object> jSonObject = gson.fromJson(json_newMeetingParameters, Map.class);
    
        boolean isBadRequest = false;
        String title = null;
        Date date = null;
        Time hour = null;
        Integer duration = null;
        Integer maxParticipantsNumber = null;
        String usernameCreator = null;
        ArrayList<String> usernameParticipants = null;
    
        try {
        
            title = jSonObject.get("title").toString();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = simpleDateFormat.parse(jSonObject.get("date").toString());
            hour = Time.valueOf(jSonObject.get("hour").toString()+":00");
            duration = Integer.parseInt(jSonObject.get("duration").toString());
            maxParticipantsNumber = Integer.parseInt(jSonObject.get("maxParticipantsNumber").toString());
            usernameCreator = jSonObject.get("usernameCreator").toString();
            usernameParticipants = (ArrayList<String>) jSonObject.get("participants");
        
            Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
        
            isBadRequest = title==null ||
                            title.isEmpty() ||
                            date == null ||
                            date.before(todayDate) ||
                            usernameCreator==null ||
                            usernameCreator.isEmpty();
        
        } catch (IllegalArgumentException | NullPointerException | ParseException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Ops! Qualcosa e' andato storto. Ricompilare il form.");
            return;
        }
    
        MeetingDAO meetingDAO = new MeetingDAO(connection);
        try {
            if (usernameParticipants==null || usernameParticipants.size()==0)
                meetingDAO.createMeeting(title, date, hour, duration, maxParticipantsNumber, usernameCreator, null);
            else {
                if (usernameParticipants.size() > maxParticipantsNumber) {
                   int attempts = (int) request.getSession().getAttribute("attempts");
                   
                    if (attempts>=3 || attempts<=0) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("attempts");   // quando il client riceve "attempts", capisce che sono stati fatti troppi tentativi
                        request.getSession().removeAttribute("attempts");
                        return;
                    }
    
                    request.getSession().setAttribute("attempts", attempts+1);
    
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Il numero di partecipanti scelto è maggiore del massimo disponibile.\n" +
                            "Riprovare.");
                    return;
                }
            
                meetingDAO.createMeeting(title, date, hour, duration, maxParticipantsNumber, usernameCreator, usernameParticipants);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create mission");
            e.printStackTrace();
        }
    }
}
