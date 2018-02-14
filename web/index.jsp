<%@ page import="swing_app.LetterpressGame" %>
<%@ page import="web_app.LetterpressSolver" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.3/css/bootstrap.min.css"
          integrity="sha384-Zug+QiDoJOrZ5t4lssLdxGhVrurbmBWopoEl+M6BdEfwnCJZtKxi1KgxUyJq13dy" crossorigin="anonymous">
    <title>Letterpress Solver</title>

  </head>
  <body>
    <div class="container">

      <h3>Game info:</h3>
      <table class="table table-hovered">
        <tr>
          <th>Player Name</th>
          <th>Game Grid</th>
          <th>Open Date</th>
        </tr>


        <% for(LetterpressGame game: gameList) { %>
        <tr>
          <td><%=game.getPlayerName() %></td>
          <td><%=game.getGrid() %></td>
          <td><%=game.getOpenDate() %></td>

        </tr>
        <% } %>
      </table>

      <form action="letterpresssolver" method="post">
        <input type="submit" name="submit" id="submit" value="Give me Games!">
      </form>

      <p>
        ${gameList};
      </p>
    </div>
  </body>
</html>
