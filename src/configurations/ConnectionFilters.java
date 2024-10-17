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
        return blockedSites.contains(host);
    }

    public static boolean isPortBlocked(int port) {
        return blockedPorts.contains(port);
    }

    public static boolean isProtocolBlocked(String port) {
        return blockedProtocols.contains(port);
    }


}

