package DbConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

public class InjectDictionary {

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306";

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) {
        InjectDictionary ew = new InjectDictionary();
        //ew.connectDB();
    }

    private void connectDB() {
        Connection conn = null;
        Statement stmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");

            Properties dbProps = new Properties();
            try (FileInputStream fIn = new FileInputStream("web/WEB-INF/localmysql.properties")) {
                dbProps.load(fIn);
            } catch (IOException e) {
                e.printStackTrace();
            }

            conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();

//            String sql = "CREATE TABLE IF NOT EXISTS db_english_words(" +
//                    "id INT AUTO_INCREMENT PRIMARY KEY ," +
//                    "word VARCHAR(64) ," +
//                    "length INT NOT NULL );";
//
//            stmt.executeUpdate(sql);
//            System.out.println("Created table in given database...");


            //STEP 5: Alter table
//            sql = "ALTER TABLE db_english_words ";
//            for (char i = 'A'; i <= 'Z'; i++) {
//                sql += "ADD number_of_" + i + " INT NOT NULL, ";
//            }
//            sql = sql.replaceAll(", $", "");
//            sql += ";";
//            stmt.executeUpdate(sql);


            //STEP 6: Add records
            String sql = convert(conn);
            stmt.executeUpdate(sql);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }// do nothing
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }

    // add every words
    private String convert(Connection conn) {
        StringBuilder sqlBuilder1 = new StringBuilder("INSERT INTO db_english_letterpress_words " +
                "(word, length, ");
        for (char i = 'A'; i <= 'Z'; i++) {
            sqlBuilder1.append("number_of_").append(i).append(", ");
        }
        String sql = sqlBuilder1.toString();
        sql = sql.replaceAll(", $", "");
        sql += ") VALUES ";

        Scanner s = null;
        try {
            s = new Scanner(new File("en.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Character, Integer> numOfChar = new TreeMap<>();
        for (char i = 'a'; i <= 'z'; i++) {
            numOfChar.put(i, 0);
        }

        //StringBuffer sqlBuilder = new StringBuffer(sql);
        StringBuilder sqlBuilder = new StringBuilder(sql);
        char secondChar = ' ';
        while (s != null && s.hasNextLine()) {
            String word = s.next().trim();

            numOfChar.replaceAll((k, v) -> 0);

            sqlBuilder.append("('").append(word).append("', ").append(word.length()).append(", ");

            for (char c : word.toCharArray()) {
                Integer count = numOfChar.get(c);
                numOfChar.put(c, ++count);
            }

            for (char c : numOfChar.keySet()) {
                sqlBuilder.append(numOfChar.get(c)).append(", ");
            }

            sqlBuilder.setLength(sqlBuilder.length() - 2);    //trim last ', '
            sqlBuilder.append("), \n");


            // show results
            if (word.length() > 1) {
                if (word.charAt(1) != secondChar) {
                    System.out.println("Adding \t" + word);
                    //System.out.println(sqlBuilder.toString());
                    secondChar = word.charAt(1);
                }
            }
        }

        sqlBuilder.setLength(sqlBuilder.length() - 3);    //trim last ', \n'
        sqlBuilder.append(";");

        sql = sqlBuilder.toString();
        System.out.println(sql);
        return sql;
    }

}
