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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

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
        
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String firstPassword = request.getParameter("firstPassword");
        String secondPassword = request.getParameter("secondPassword");
        
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
        else if (!isEmailValid(email)){
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
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(username);
            request.getSession().setAttribute("user", user);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    private boolean isEmailValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        
        Pattern patternRegex = Pattern.compile(emailRegex);
        
        if (email == null)
            return false;
        
        return patternRegex.matcher(email).matches();
    }
}
