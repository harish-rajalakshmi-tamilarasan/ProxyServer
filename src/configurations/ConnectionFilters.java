package configurations;

import java.util.HashSet;

/**
 * @author Harish T
 */
public class ConnectionFilters {
    private static HashSet<String> blockedSites = new HashSet<>();
    private static HashSet<Integer> blockedPorts = new HashSet<>();
    private static HashSet<String> blockedProtocols = new HashSet<>();


    public static void addBlockedSite(String host) {
        if ("www.".equals(host.substring(0, 3))) {
            host = host.substring(4);
        }
        blockedSites.add(host);
    }

    public static void addBlockedPorts(int port) {
        if (port > 0 && port < 65543) {
            blockedPorts.add(port);
        }
    }

    public static void addBlockedProtocol(String protocol) {
        blockedProtocols.add(protocol);
    }

    public static boolean isSiteBlocked(String host) {
        String temp = host;
        if (host.length() > 2 && "www.".equals(host.substring(0, 3))) {
            temp = host.substring(4);
        }
        return blockedSites.contains(temp);
    }

    public static boolean isPortBlocked(int port) {
        return blockedPorts.contains(port);
    }

    public static boolean isProtocolBlocked(String port) {
        return blockedProtocols.contains(port);
    }


}

