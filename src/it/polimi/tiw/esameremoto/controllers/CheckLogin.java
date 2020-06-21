package it.polimi.tiw.esameremoto.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// import it.polimi.tiw.esameremoto
import it.polimi.tiw.esameremoto.beans.User;
import it.polimi.tiw.esameremoto.dao.UserDAO;
import it.polimi.tiw.esameremoto.utils.ConnectionHandler;

// import thymeleaf
import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // obtain and escape params
        String username;
        String password;

        try {
            username = request.getParameter("username");
            password = request.getParameter("password");

            if (username==null || password==null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Credentials can't be null");
                return;
            }
            else if(username.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Credentials can't be empty");
                return;
            }

        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO da togliere a progetto finito
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // query db to authenticate for user
        UserDAO userDao = new UserDAO(connection);
        User user;

        try {
            user = userDao.checkUser(username, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal server error, retry later");
            return;
        }


        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Incorrect credentials");
        } else {
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(username);
        }
    }

    /*
    private void invalidCredentials(String errorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = "/index.html";
        ServletContext servletContext = getServletContext();
        final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
        webContext.setVariable("errorMessage", errorMessage);
        templateEngine.process(path, webContext, response.getWriter());
    }
     */

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
