import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");//points to database and keeps it
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, gameName VARCHAR, gameGenre VARCHAR, gamePlatform VARCHAR, gameYear INT);");
        System.out.println("Starting GameTracker");
        Spark.init();

        Spark.get("/",
                (request, response) -> {
                    ArrayList<Game> games = selectGames(conn);
                    HashMap m = new HashMap();
                    m.put("games", games);
                    return new ModelAndView(m, "home.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post("/create-game", (request, response) -> {
            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String gamePlatform = request.queryParams("gamePlatform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));
            insertGames(conn, gameName, gameGenre, gamePlatform, gameYear);
            response.redirect("/");
            return "";
        });

        Spark.post("/delete-game", (request, response) -> {
            int id = Integer.valueOf(request.queryParams("editNumber"));
            deleteGame(conn, id);
            response.redirect("/");
            return"";
        });

        Spark.post("/edit-game", (request, response) -> {
            int id = Integer.valueOf(request.queryParams("editNumber"));
            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String gamePlatform = request.queryParams("gamePlatform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));
            updateGame(conn, gameName, gameGenre, gamePlatform, gameYear, id);
            response.redirect("/");
            return "";
        });
    }

    public static void insertGames(Connection conn, String gameName, String gameGenre, String gamePlatform, int gameYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO games VALUES (NULL, ?, ?, ?, ?);");
        stmt.setString(1, gameName);
        stmt.setString(2, gameGenre);
        stmt.setString(3, gamePlatform);
        stmt.setInt(4, gameYear);
        stmt.execute();
    }

    public static void deleteGame(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games;");
        ResultSet results = stmt.executeQuery();
        while(results.next()){
            int id = results.getInt("id");
            String gameName = results.getString("gameName");
            String gameGenre = results.getString("gameGenre");
            String gamePlatform = results.getString("gamePlatform");
            int gameYear = results.getInt("gameYear");
            games.add(new Game(id, gameName, gameGenre, gamePlatform, gameYear));//don't forget to add it to the list
        }
        return games;
    }

    public static void updateGame(Connection conn, String gameName, String gameGenre, String gamePlatform, int gameYear, int editNumber) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE games SET gameName=?, gameGenre=?, gamePlatform=?, gameYear=? WHERE id = ?");
        stmt.setString(1, gameName);
        stmt.setString(2, gameGenre);
        stmt.setString(3, gamePlatform);
        stmt.setInt(4, gameYear);
        stmt.setInt(5, editNumber);
        stmt.execute();
    }
}