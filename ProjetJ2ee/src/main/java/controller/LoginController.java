package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    // Modifie la méthode pour qu'elle soit publique
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if ("admin".equals(username) && "123".equals(password)) {
                HttpSession session = request.getSession();
                session.setAttribute("loggedUser", username);
                RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
                rd.forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Identifiants incorrects");
                RequestDispatcher rd = request.getRequestDispatcher("/vue/login.jsp");
                rd.forward(request, response);
            }
        } else if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("errorMessage", "Veuillez remplir tous les champs !");
                RequestDispatcher rd = request.getRequestDispatcher("/register.jsp");
                rd.forward(request, response);
            } else {
                request.setAttribute("msg", "Inscription réussie ! Vous pouvez vous connecter.");
                RequestDispatcher rd = request.getRequestDispatcher("/vue/login.jsp");
                rd.forward(request, response);
            }
        } else {
            response.sendRedirect("/vue/login.jsp");
        }
    }
}
