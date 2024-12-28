package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if ("admin".equals(username) && "123".equals(password)) {
                HttpSession session = request.getSession();
                session.setAttribute("loggedUser", username);
                // Redirection vers le lobby
                response.sendRedirect(request.getContextPath() + "/lobby");
            } else {
                request.setAttribute("errorMessage", "Identifiants incorrects");
                request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
            }
        } else if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("errorMessage", "Veuillez remplir tous les champs !");
                request.getRequestDispatcher("/vue/register.jsp").forward(request, response);
            } else {
                request.setAttribute("msg", "Inscription réussie ! Vous pouvez vous connecter.");
                request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/vue/login.jsp");
        }
    }
}
