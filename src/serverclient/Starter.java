package serverclient;

import configurations.ConnectionFilters;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;

/**
 * @author Harish T
 */
public class Starter {
    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = new JSONObject(new FileReader(new File("E:\\ProxyServer\\conf\\Rules.json")));
        addRules(jsonObject);
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
