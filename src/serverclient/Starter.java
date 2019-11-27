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

    public static void main(String[] args) {
        try {
            JSONObject jsonObject = new JSONObject(new FileReader(new File("E:\\ProxyServer\\conf\\Rules.json")));
        } catch (IOException ex) {
            System.out.println("JSON Exception" + ex);
        }
        try {
            DBInitializer.initDB();
        } catch (SQLException ex) {
            System.exit(0);
        }
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
