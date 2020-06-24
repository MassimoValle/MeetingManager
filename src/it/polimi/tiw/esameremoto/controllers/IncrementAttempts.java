package it.polimi.tiw.esameremoto.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/IncrementAttempts")
@MultipartConfig
public class IncrementAttempts extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        Object objectAttempts = session.getAttribute("attempts");
        int attempts;
        String previousTitle = (String) session.getAttribute("title");
        String currentTitle = request.getHeader("title");
    
        if (objectAttempts==null || previousTitle==null || !previousTitle.equals(currentTitle)) {
            session.setAttribute("attempts", 1);
            session.setAttribute("title", currentTitle);
        }
        else {
            attempts = (int) objectAttempts;
            attempts++;
            session.setAttribute("attempts", attempts);
        }
    }
}
