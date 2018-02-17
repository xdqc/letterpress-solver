package web_app;

import DbConnector.DbConnector;
import swing_app.LetterpressGame;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LetterpressSolver extends HttpServlet {
    private List<LetterpressGame> gameList = new ArrayList<>();

    public LetterpressSolver() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setAttribute("gameList", gameList);

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setAttribute("gameList", gameList);


        DbConnector.fetchGridFromDB(gameList);

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }
}
