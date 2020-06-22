package it.polimi.tiw.esameremoto.controllers;

import it.polimi.tiw.esameremoto.beans.User;
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

@WebServlet("/SignUp")
@MultipartConfig
public class SignUp extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private Connection connection = null;
    
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = new User();
        
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String name = (String) request.getAttribute("name");
        String surname = (String) request.getAttribute("surname");
        String firstPassword = (String) request.getAttribute("firstPassword");
        String secondPassword = (String) request.getAttribute("secondPassword");
        
        if (username==null || username.isEmpty()
                || email==null || email.isEmpty()
                || name==null || name.isEmpty()
                || surname==null || surname.isEmpty()
                || firstPassword==null || firstPassword.isEmpty()
                || secondPassword==null || secondPassword.isEmpty()){
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Credentials can't be null.");
            return;
        }
        else if (/*TODO checkare che la mail sia ok*/false){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The email isn't valid.");
            return;
        }
        else if (!firstPassword.equals(secondPassword)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Passwords aren't equal!");
            return;
        }
    
        try {
            UserDAO userDAO = new UserDAO(connection);
            
            if (!userDAO.checkUsername(username)) {
                //TODO potrei modificarlo portando direttamente la lista di usernames lato client
                // prima ancora di registrarsi
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("The name already exists, choose another one.");
                return;
            }
        
            user.setUsername(username);
            user.setName(name);
            user.setSurname(surname);
            user.setEmail(email);
            user.setPassword(firstPassword);
        
            userDAO.insertUser(user);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
