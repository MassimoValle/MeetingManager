package it.polimi.tiw.esameremoto.dao;

import it.polimi.tiw.esameremoto.beans.Meeting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeetingDAO {

    private Connection connection;

    public MeetingDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Meeting> findMeetingsByUser(String username) throws SQLException {

        List<Meeting> meetings = new ArrayList<>();

        String query =
                "SELECT * FROM db_meeting_manager_esame2020.meeting " +
                "WHERE idMeeting = any (" +
                        "SELECT idMeeting " +
                        "FROM participation " +
                        "WHERE username = ?" +
                        ")";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Meeting meeting = new Meeting();
                    meeting.setIdMeeting(result.getInt("idMeeting"));
                    meeting.setHour(result.getTime("hour"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setDuration(result.getTime("duration"));
                    meeting.setMaxParticipantsNumber(result.getInt("maxParticipantsNumber"));
                    meeting.setTitle(result.getString("title"));
                    meeting.setUsernameCreator(result.getString("usernameCreator"));
                    meetings.add(meeting);
                }
            }
        }
        
        return meetings;
    }

    public void createMeeting(String name, Date date, String description, String reporterId) throws SQLException {

        String query = "INSERT into meetings (name, date, description, reporter) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, name);
            pstatement.setDate(2, new java.sql.Date(date.getTime()));
            pstatement.setString(3, description);
            pstatement.setString(4, reporterId);
            pstatement.executeUpdate();
        }
    }
}
