package it.polimi.tiw.esameremoto.controllers;

import it.polimi.tiw.esameremoto.utils.ServletUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/AddUser")
public class AddUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private TemplateEngine templateEngine;
    
    public void init() throws ServletException {
        this.templateEngine = ServletUtils.createThymeleafTemplate(getServletContext());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object tempUsersChosen = session.getAttribute("usersChosenUsernames");
        ArrayList<String> usersChosen;
    
        if (tempUsersChosen==null)
            usersChosen = new ArrayList<>();
        else
            usersChosen = (ArrayList<String>) tempUsersChosen;
    
        String usernameSelected = request.getParameter("username");
        if (usersChosen.contains(usernameSelected))
            usersChosen.remove(usernameSelected);
        else
            usersChosen.add(request.getParameter("username"));
    
        session.setAttribute("usersChosenUsernames", usersChosen);
    
        WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process("/anagrafica.html", webContext, response.getWriter());
    }
}
