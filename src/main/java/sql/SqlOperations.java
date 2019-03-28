package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqlOperations {

    public static Boolean checkThatMessageIsFiltered (String text) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://213.232.228.186:5432/qa_db",
                    "postgres", "");

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM errbit_ahtung");

            while (rs.next()) {
                String[] subStrings = rs.getString(4).split("¶");
                int countSubString = 0;
                for (int m = 0; m < subStrings.length; m++) {
                    if (text.contains(subStrings[m]))
                        countSubString++;
                }
                if (countSubString == subStrings.length && countSubString != 0) {
                    return false;
                }
            }
        } catch (Exception ex) {
            System.out.println("checkThatMessageIsFiltered - " + ex.getMessage());
        }

        return true;
    }
}
