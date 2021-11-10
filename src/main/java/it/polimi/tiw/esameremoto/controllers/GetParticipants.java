package it.polimi.tiw.esameremoto.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.esameremoto.dao.UserDAO;
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
import java.util.ArrayList;

@WebServlet("/GetParticipants")
@MultipartConfig
public class GetParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDAO userDAO = new UserDAO(connection);
        ArrayList<String> usernames = new ArrayList<>();
        
        try {
            usernames = userDAO.getUsernames();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal server error: SQLException when getting usernames.");
            e.printStackTrace();
        }
        
        Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
        String json_users = gson.toJson(usernames);
    
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json_users);
    }
    
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
