package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException{
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String authHeader = request.getHeader("Authorization");
        if("OPTIONS".equals(request.getMethod())){
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(req, res);
        }else{
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                throw new ServletException("不合法");
            }
            String token = authHeader.substring(7);
            try{
                Claims claims = Jwts.parser().setSigningKey("base64EncodeSecretKey").parseClaimsJws(token).getBody();
                request.setAttribute("claims", claims);
            }catch(Exception e){
                throw new ServletException("Invalid Token");
            }
            chain.doFilter(req, res);
        }
    }
}
