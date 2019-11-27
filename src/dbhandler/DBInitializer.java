package dbhandler;

/**
 * @author Harish T
 */

import java.sql.*;

public class DBInitializer {
    private static Connection c = null;
    private static Statement statement = null;

    public static void initDB() throws SQLException {
        getDBConnection();
        try {
            if (!isTableExits()) {
                createTable();
            }
        } catch (SQLException ex) {
            System.out.println("SQL Exception in Table creation");
            throw ex;
        }
        c.close();
    }

    private static void getDBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/proxyServer",
                    "Postgres", "Messi123");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private static boolean isTableExits() throws SQLException {
        String tableCheckQuery = "select count(*) from information_schema.tables  where table_name='httpdatadetails'";
        statement = c.createStatement();
        ResultSet result = statement.executeQuery(tableCheckQuery);
        int count = 0;
        while (result.next()) {
            count = result.getInt("COUNT");
            break;
        }
        return count != 0;
    }

    private static void createTable() throws SQLException {
        String createTable = "CREATE TABLE HTTPDATADETAILS (\n" +
                "DOMAIN_NAME VARCHAR(255) NOT NULL,\n" +
                "PORT int NOT NULL,\n" +
                "IS_SSL BOOLEAN NOT NULL,\n" +
                "REQUEST_SIZE INT NOT NULL,\n" +
                "RESPONSE_SIZE INT NOT NULL,\n" +
                "CONSTRAINT PK_HTTPDATADETAILS PRIMARY KEY (DOMAIN_NAME, PORT))";
        ResultSet resultSet = statement.executeQuery(createTable);
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = resultSet.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
        }
    }
}


