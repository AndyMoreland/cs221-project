import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:/Users/andrew/cs221-project/scrape.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM emails");
            while (resultSet.next()) {
                String dateString = resultSet.getString("timestamp");
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ");
                SimpleDateFormat secondFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss ZZZZ");
                try {
                    System.out.println(format.parse(dateString));
                } catch (ParseException e) {
                    System.out.println(secondFormat.parse(dateString));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
