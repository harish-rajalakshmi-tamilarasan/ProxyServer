package dbhandler;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DBInitializer {
    private static Connection c = null;
    private static Statement statement = null;
    private static final Logger LOGGER = Logger.getLogger(DBInitializer.class.getName());
    private static DBInitializer instance;

    private DBInitializer() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DBInitializer getInstance() {
        if (instance == null) {
            instance = new DBInitializer();
        }
        return instance;
    }

    public void initDB() throws SQLException {
        getDBConnection();
        try {
            if (!isTableExits()) {
                createTable();
            }
        } catch (SQLException ex) {
            System.out.println("SQL Exception in Table creation");
            throw ex;
        }
    }

    private void getDBConnection() {
        File file = new File("..\\conf\\db.properties");  // Specify the relative path to the properties file
        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            String host = properties.getProperty("host");
            String port = properties.getProperty("port");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            String url = "jdbc:postgresql://" + host + ":" + port + "/proxyServer";
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            LOGGER.warning("Error in getting the DB connection: " + e.getMessage());
        }
    }

    private boolean isTableExits() throws SQLException {
        String tableCheckQuery = "select count(*) from information_schema.tables where table_name='httpdatadetails'";
        statement = c.createStatement();
        ResultSet result = statement.executeQuery(tableCheckQuery);
        int count = 0;
        while (result.next()) {
            count = result.getInt("COUNT");
            break;
        }
        return count != 0;
    }

    private void createTable() throws SQLException {
        String createTable = "CREATE TABLE HTTPDATADETAILS (\n" +
                "DOMAIN_NAME VARCHAR(255) NOT NULL,\n" +
                "PORT int NOT NULL,\n" +
                "IS_SSL BOOLEAN NOT NULL,\n" +
                "REQUEST_SIZE INT NOT NULL,\n" +
                "RESPONSE_SIZE INT NOT NULL,\n" +
                "CONSTRAINT PK_HTTPDATADETAILS PRIMARY KEY (DOMAIN_NAME, PORT))";
        statement.execute(createTable);
    }

    public void runQuery(String domainName, int port, String protocol, int requestsize, int responsesize) throws SQLException {
        boolean isSSL = protocol.equals("https");
        String temp=domainName;
        if("www.".regionMatches(0, temp, 0, 3)){
            temp=temp.substring(3);
        }
        String query =
                "INSERT INTO HTTPDATADETAILS (DOMAIN_NAME,PORT,IS_SSL,REQUEST_SIZE,RESPONSE_SIZE)" +
                        " VALUES('" + temp + "'," + port + "," + isSSL + "," + requestsize + "," + responsesize +
                        ")ON CONFLICT (DOMAIN_NAME,PORT) DO UPDATE SET REQUEST_SIZE=HTTPDATADETAILS.REQUEST_SIZE+EXCLUDED" +
                        ".REQUEST_SIZE,RESPONSE_SIZE=HTTPDATADETAILS.RESPONSE_SIZE+EXCLUDED.RESPONSE_SIZE;";
        runQuery(query);
    }

    public void runQuery(String query) throws SQLException {
        c.createStatement().execute(query);
    }

}