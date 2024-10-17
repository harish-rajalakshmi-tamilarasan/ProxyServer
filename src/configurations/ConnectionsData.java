package configurations;

import java.util.HashMap;

/**
 * @author Harish T
 */
public class ConnectionsData {
    private static HashMap<String, Integer> requestMap = new HashMap<>();
    private static HashMap<String, Integer> responseMap = new HashMap<>();

    public static synchronized void addToRequestSize(String host, int size) {
        String temp = host;
        if (requestMap.containsKey(host)) {
            requestMap.put(temp, requestMap.get(host));
        } else requestMap.put(host, size);
    }

    public static synchronized void addToResponseSize(String host, int size) {
        String temp = host;
        if (responseMap.containsKey(host)) {
            responseMap.put(temp, responseMap.get(host));
        } else responseMap.put(host, size);

    }

}
