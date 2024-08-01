package com.example.uvillage;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "index", value = "/index")
public class indexServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String temp = "/WEB-INF/views/template/dftemplate.jsp";
        String view = "/WEB-INF/views/vindex.jsp";

        request.setAttribute("view", view);

        RequestDispatcher dispatcher = request.getRequestDispatcher(temp);
        dispatcher.forward(request, response);
    }
}
