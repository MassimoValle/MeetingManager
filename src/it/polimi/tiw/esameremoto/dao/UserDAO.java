package it.polimi.tiw.esameremoto.dao;

import it.polimi.tiw.esameremoto.beans.User;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO {
	private final Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkUser(String username, String password) throws SQLException {
		String query = "SELECT * FROM db_meeting_manager_esame2020.user WHERE username=? AND password=?";
		
		// try-catch with resources
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			
			try (ResultSet result = preparedStatement.executeQuery()) {
				if (result.next()){
					User user = new User();
					user.setUsername(result.getString("username"));
					user.setPassword(result.getString("password"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setEmail(result.getString("email"));
					return user;
				}
				else return null;
			}
		}
	}
	
	public ArrayList<String> getUsernames() throws SQLException {
		String query = "SELECT username FROM db_meeting_manager_esame2020.user";
		ArrayList<String> usernames = new ArrayList<>();
		
		// try-catch with resources
		try (Statement statement = connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery(query)) {
				while (resultSet.next()){
					String username = resultSet.getString("username");
					usernames.add(username);
				}
			}
		}
		
		return usernames;
	}
	
	public boolean checkUsername(String username) throws SQLException {
		String query = "SELECT * FROM db_meeting_manager_esame2020.user WHERE username=?";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setString(1, username);
			
			try (ResultSet result = preparedStatement.executeQuery()) {
				
				return !result.first();
				
			}
		}
	}
	
	public void insertUser(User user) throws SQLException {
		String query = "INSERT INTO db_meeting_manager_esame2020.user VALUES (?,?,?,?,?)";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setString(2, user.getName());
			preparedStatement.setString(3, user.getSurname());
			preparedStatement.setString(4, user.getEmail());
			preparedStatement.setString(5, user.getPassword());
			
			preparedStatement.executeUpdate();
		}
	}
}
