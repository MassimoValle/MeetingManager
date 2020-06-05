package it.polimi.tiw.esameremoto.filters;

import javax.servlet.*;
import java.io.IOException;

public class LoginChecker implements Filter {
    public void destroy() {
    }
    
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        System.out.println("Filtro LoginChecker in esecuzione...");
        
        
        
        chain.doFilter(req, resp);
    }
    
    public void init(FilterConfig config) throws ServletException {
        
    }
    
}
