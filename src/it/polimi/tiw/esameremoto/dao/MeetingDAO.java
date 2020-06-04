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

    private Connection con;

    public MeetingDAO(Connection connection) {
        this.con = connection;
    }

    public List<Meeting> findMeetingsByUser(int userId) throws SQLException {

        List<Meeting> meetings = new ArrayList<Meeting>();

        String query = "SELECT * from reunions where reporter = ? ORDER BY date DESC";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setInt(1, userId);
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Meeting meeting = new Meeting();
                    meeting.setId(result.getInt("id"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setDescription(result.getString("description"));
                    meetings.add(meeting);
                }
            }
        }
        return meetings;
    }

    public void createMeeting(String name, Date date, String description, int reporterId) throws SQLException {

        String query = "INSERT into meetings (name, date, description, reporter) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, name);
            pstatement.setDate(2, new java.sql.Date(date.getTime()));
            pstatement.setString(3, description);
            pstatement.setInt(4, reporterId);
            pstatement.executeUpdate();
        }
    }
}
