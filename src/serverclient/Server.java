package serverclient;

import java.io.*;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * @author Harish T
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    public static void proxyListener() {
        try (ServerSocket proxySocket = new ServerSocket(9000)) {
            while (true) {
                new ProxyServer(proxySocket.accept());
            }
        } catch (IOException ioexception) {
           LOGGER.warning("Error with the proxy server socket: " + ioexception.getMessage());
        }
    }

}


