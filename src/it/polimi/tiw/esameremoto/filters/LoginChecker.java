package it.polimi.tiw.esameremoto.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginChecker implements Filter {
    public void destroy() {
    }
    
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        System.out.println("LoginChecker filter executing...");
    
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String loginPath = "index.html";
    
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user")==null) {
            response.sendRedirect(loginPath);
            return;
        }
        
        chain.doFilter(req, resp);
    }
    
    public void init(FilterConfig config) throws ServletException {
        
    }
}
