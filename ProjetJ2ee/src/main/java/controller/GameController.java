package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ce servlet est désormais inutile,
 * si tu vas directement sur /vue/game.jsp?gameId=...
 * 
 * Soit tu le supprimes, soit tu laisses une redirection.
 */
@WebServlet("/game")
public class GameController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Soit tu rediriges direct vers game.jsp?gameId=...
        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/lobby");
            return;
        }
        // ex: /vue/game.jsp?gameId=GAME-1234
        response.sendRedirect(request.getContextPath() + "/vue/game.jsp?gameId=" + gameId);
    }
}
