package dbhandler;

import java.sql.*;

/**
 * @author Harish T
 */
public class DBOperationUtil {
    private Connection connection;

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
        getDbConnection();
        connection.createStatement().execute(query);
        connection.close();
    }

    private void getDbConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/proxyServer", "Postgres", "Messi123");
    }
}
