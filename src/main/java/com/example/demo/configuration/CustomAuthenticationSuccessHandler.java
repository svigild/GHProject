package com.example.demo.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null ?
                ((SavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST")).getRedirectUrl() :
                "/";
        response.sendRedirect(targetUrl);
    }
}
