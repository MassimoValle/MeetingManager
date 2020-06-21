package it.polimi.tiw.esameremoto.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.esameremoto.beans.Meeting;
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.MeetingDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;


@WebServlet("/GetMeetings")
public class GetMeetings extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetMeetings() {
        super();
    }

    public void init() throws ServletException {
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
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to recover meetings");
            return;
        }

        ArrayList<Meeting> myMeetings = new ArrayList<>();
        ArrayList<Meeting> othersMeetings = new ArrayList<>();
        for (Meeting meeting : meetings){
            if (meeting.getUsernameCreator().equals(user.getUsername()))
                myMeetings.add(meeting);
            else
                othersMeetings.add(meeting);
        }


        Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
        String json_myMeetings = gson.toJson(myMeetings);
        String json_othersMeetings = gson.toJson(othersMeetings);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json_myMeetings + '#' + json_othersMeetings);

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
