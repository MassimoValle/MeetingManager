package it.polimi.tiw.esameremoto.filters;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
        String loginPath = "/index.html";
    
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user")==null) {
            WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale());
            webContext.setVariable("errorMessage", "I am sorry, you have to login to access.");
            ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(request.getServletContext());
            templateResolver.setTemplateMode(TemplateMode.HTML);
            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(templateResolver);
            templateResolver.setSuffix(".html");
            templateEngine.process(loginPath, webContext, response.getWriter());
            return;
        }
        
        chain.doFilter(req, resp);
    }
    
    public void init(FilterConfig config) throws ServletException {
        
    }
    
}
