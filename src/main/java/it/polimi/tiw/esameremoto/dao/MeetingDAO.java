package it.polimi.tiw.esameremoto.dao;

import it.polimi.tiw.esameremoto.beans.Meeting;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeetingDAO {

    private Connection connection;

    public MeetingDAO(Connection connection) {
        this.connection = connection;
    }

    public Meeting findMeetingById(Integer id) throws SQLException{

        Meeting meeting = null;

        String query =
                "SELECT * FROM db_meeting_manager_esame2020.meeting " +
                        "WHERE idMeeting = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());

            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    meeting = new Meeting();
                    meeting.setIdMeeting(result.getInt("idMeeting"));
                    meeting.setHour(result.getTime("hour"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setDuration(result.getInt("duration"));
                    meeting.setMaxParticipantsNumber(result.getInt("maxParticipantsNumber"));
                    meeting.setTitle(result.getString("title"));
                    meeting.setUsernameCreator(result.getString("usernameCreator"));
                }
            }
        }

        return meeting;
    }

    public List<Meeting> findMeetingsNotExpiredByUser(String username) throws SQLException {

        List<Meeting> meetings = new ArrayList<>();

        String query =
                "SELECT * FROM db_meeting_manager_esame2020.meeting " +
                "WHERE idMeeting = any (" +
                        "SELECT idMeeting " +
                        "FROM participation " +
                        "WHERE username = ?" +
                        ")" +
                        "AND meeting.date > current_date";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Meeting meeting = new Meeting();
                    meeting.setIdMeeting(result.getInt("idMeeting"));
                    meeting.setHour(result.getTime("hour"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setDuration(result.getInt("duration"));
                    meeting.setMaxParticipantsNumber(result.getInt("maxParticipantsNumber"));
                    meeting.setTitle(result.getString("title"));
                    meeting.setUsernameCreator(result.getString("usernameCreator"));
                    meetings.add(meeting);
                }
            }
        }
        
        return meetings;
    }

    public void createMeeting(String title, Date date, Time hour, int duration, int maxParticipantsNumber, String usernameCreator, ArrayList<String> usersChosen) throws SQLException {

        String addMeetingQuery = "INSERT into db_meeting_manager_esame2020.meeting " +
                "(hour, date, title, duration, maxParticipantsNumber, usernameCreator) VALUES(?, ?, ?, ?, ?, ?)";
        String addParticipationQuery = "INSERT INTO db_meeting_manager_esame2020.participation " +
                " VALUES (?, ?)";
        
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(addMeetingQuery)) {
            preparedStatement.setTime(1, hour);
            preparedStatement.setDate(2, new java.sql.Date(date.getTime()));
            preparedStatement.setString(3, title);
            preparedStatement.setInt(4, duration);
            preparedStatement.setInt(5, maxParticipantsNumber);
            preparedStatement.setString(6, usernameCreator);
            preparedStatement.executeUpdate();
        }
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(addParticipationQuery)) {
            Statement statement = connection.createStatement();
            int idMeeting;
    
            try (ResultSet resultSet = statement.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (resultSet.next())
                    idMeeting = resultSet.getInt("LAST_INSERT_ID()");
                else
                    throw new SQLException();
            }
            
            preparedStatement.setInt(1, idMeeting);
            preparedStatement.setString(2, usernameCreator);
            preparedStatement.executeUpdate();
    
            if (usersChosen!=null) {
                for (String userChosen: usersChosen){
                    preparedStatement.setString(2, userChosen);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }
}
