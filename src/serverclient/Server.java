package serverclient;

import java.io.*;
import java.net.ServerSocket;

/**
 * @author Harish T
 */
public class Server {
    public static void proxyListener() {
        try {
            ServerSocket proxySocket = new ServerSocket(9000);
            System.out.println(System.currentTimeMillis());
            while (true) {
                new ProxyServer(proxySocket.accept());
            }
        } catch (IOException ioexception) {
            System.out.println(System.currentTimeMillis());
            ioexception.printStackTrace();
        }
    }

}


