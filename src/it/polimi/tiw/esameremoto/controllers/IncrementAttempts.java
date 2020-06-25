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
        String previous_jsonString_newMeetingParameters = (String) session.getAttribute("jsonString_newMeetingParameters");
        String current_jsonString_newMeetingParameters = request.getHeader("newMeetingParameters");
    
        if (objectAttempts==null || previous_jsonString_newMeetingParameters==null
                || !previous_jsonString_newMeetingParameters.equals(current_jsonString_newMeetingParameters)) {
            session.setAttribute("attempts", 1);
            session.setAttribute("jsonString_newMeetingParameters", current_jsonString_newMeetingParameters);
        }
        else {
            attempts = (int) objectAttempts;
            attempts++;
            session.setAttribute("attempts", attempts);
        }
    }
}
