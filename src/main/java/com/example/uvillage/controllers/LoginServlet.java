package com.example.uvillage.controllers;

import com.example.uvillage.util.DatabaseUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "login", value = "/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private static final String USER_SESSION_ATTRIBUTE = "user";
    private static final String LOGIN_ERROR_ATTRIBUTE = "loginError";

    private DatabaseUtil databaseUtil;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loginPage = "/WEB-INF/views/login.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(loginPage);
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            handleLoginError(request, response, "Email and password are required");
            return;
        }

        try {
            boolean isAuthenticated = databaseUtil.authenticateUser(username, password);

            if (isAuthenticated) {
                handleSuccessfulLogin(request, response, username);
            } else {
                handleLoginError(request, response, "Invalid username or password");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during authentication", e);
            handleLoginError(request, response, "An error occurred during login");
        }
    }

    private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, String username) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute(USER_SESSION_ATTRIBUTE, username);
        logger.log(Level.INFO, "User logged in: {0}", username);
        response.sendRedirect(request.getContextPath() + "/index");
    }

    private void handleLoginError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setAttribute(LOGIN_ERROR_ATTRIBUTE, errorMessage);
        logger.log(Level.WARNING, "Login failed: {0}", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
        dispatcher.forward(request, response);
    }
}
