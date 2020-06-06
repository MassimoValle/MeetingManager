package it.polimi.tiw.esameremoto.dao;

import it.polimi.tiw.esameremoto.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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



}
