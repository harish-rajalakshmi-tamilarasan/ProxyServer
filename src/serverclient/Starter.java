package serverclient;

import configurations.ConnectionFilters;
import dbhandler.DBInitializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Harish T
 */
public class Starter {

    public static void main(String[] args) throws SQLException {
        try {
            JSONObject jsonObject = new JSONObject(new FileReader("conf\\Rules.json"));
            addRules(jsonObject);
        } catch (IOException ex) {
            System.out.println("JSON Exception" + ex);
        }
        DBInitializer dbInitializer = DBInitializer.getInstance();
        dbInitializer.initDB();
        Server.proxyListener();
    }

    private static void addRules(JSONObject jsonObject) {
        try {
            String[] sites = (String[]) jsonObject.get("sites");
            for (String site : sites) {
                ConnectionFilters.addBlockedSite(site);
            }
            String[] protocol = (String[]) jsonObject.get("protocol");
            for (String prot : protocol) {
                ConnectionFilters.addBlockedProtocol(prot);
            }
            int[] ports = (int[]) jsonObject.get("ports");
            for (int port : ports) {
                try {
                    ConnectionFilters.addBlockedPorts(port);
                } catch (ArithmeticException ex) {
                    //do nothing.
                }
            }
        } catch (JSONException ex) {

        }
    }

}
