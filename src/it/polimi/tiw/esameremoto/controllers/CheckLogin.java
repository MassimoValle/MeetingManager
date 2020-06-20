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
    private TemplateEngine templateEngine;


    public void init() throws ServletException {
        this.templateEngine = ServletUtils.createThymeleafTemplate(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // obtain and escape params
        String username;
        String password;
        String path;

        try {
            username = request.getParameter("username");
            password = request.getParameter("password");

            if (username==null || password==null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                invalidCredentials("Credentials can't be null", request, response);
                return;
            }
            else if(username.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                invalidCredentials("Credentials can't be empty.", request, response);
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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
            return;
        }


        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message

        if (user == null) {
            invalidCredentials("Incorrect username or password", request, response);
            return;
        }

        request.getSession().setAttribute("user", user);
        path = getServletContext().getContextPath() + "/GetMeetings";	//va alla GetMeetings Servlets
        response.sendRedirect(path);
    }

    private void invalidCredentials(String errorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = "/index.html";
        ServletContext servletContext = getServletContext();
        final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
        webContext.setVariable("errorMessage", errorMessage);
        templateEngine.process(path, webContext, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
