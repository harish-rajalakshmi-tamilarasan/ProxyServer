package serverclient;

import configurations.ConnectionFilters;

import java.io.*;
import java.net.*;


/**
 * @author Harish T
 */
public class ProxyServer extends Thread {
    public static void main(String[] args) {

    }

    private String host;
    private int port;
    private int requestSize;
    private int responseSize;
    private InputStream proxyToClientIP;
    private OutputStream proxyToClientOP;
    private InputStream proxyToServerIP;
    private OutputStream proxyToServerOP;
    private Socket socket;
    private Socket socketFromProxyServer;
    private String method;
    private byte[] headInfo = new byte[1024];
    private String protocol;


    ProxyServer(Socket socket) {
        this.socket = socket;
        this.start();
    }

    public void run() {
        try {
            proxyToClientIP = socket.getInputStream();
            proxyToClientOP = socket.getOutputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int c;
            //Read First Line line ending with /r/n to get Host Details.
            for (c = proxyToClientIP.read(); c != -1; c = proxyToClientIP.read()) {
                bytes.write(c);
                if (c == (int) '\n') break;
            }
            String hostHeader = bytes.toString();
            requestSize += bytes.size();
            String[] hostDetails = hostHeader.split(" ");
            parseHostDetails(hostDetails);
            //Write Custom Output
            if (ConnectionFilters.isSiteBlocked(host)) {
                String blockMessage = "<html>\n" +
                        "<body>\n" +
                        "<h1>Error!!</h1>\n" +
                        "<div style=\"display:center;color:red;\">Site harish-4072 is Blocked </div>\n" +
                        "</body>\n" +
                        "</html>";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyToClientOP));
                writer.write("HTTP/1.1 200 Connection established\r\n");
                writer.write("Content-Type:text/html\r\n\r\n");
                writer.write(blockMessage);
                writer.flush();
                writer.close();
            } else if (ConnectionFilters.isPortBlocked(port)) {
                String blockMessage = "<html><body><h1> </h1><p> Site " + port + " is Blocked </p></body></html>";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyToClientOP));
                writer.write("HTTP/1.1 200 Connection established\r\n");
                writer.write("Content-Type:text/html\r\n\r\n");
                writer.write(blockMessage);
                writer.flush();
                writer.close();
            } else if (ConnectionFilters.isProtocolBlocked(protocol)) {
                String blockMessage = "<html><body><h1> </h1><p> Site " + port + " is Blocked </p></body></html>";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyToClientOP));
                writer.write("HTTP/1.1 200 Connection established\r\n");
                writer.write("Content-Type:text/html\r\n\r\n");
                writer.write(blockMessage);
                writer.flush();
                writer.close();
            } else if (method.equalsIgnoreCase("connect")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(proxyToClientIP));
                String temp;
                //CleanUp Connect request Stream
                while ((temp = reader.readLine()) != null) {
                    requestSize += temp.getBytes().length;
                    if (temp.equals("")) break;
                }
                processHTTPSRequest();
            } else {
                headInfo = bytes.toByteArray();
                processHTTPRequest();
            }

        } catch (IOException ex) {
            System.out.println(" Error in Run method" + ex);
        } finally {
            try {
                proxyToClientIP.close();
                proxyToServerIP.close();
                proxyToClientOP.close();
                proxyToServerOP.close();
                socket.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void parseHostDetails(String[] hostDetail) {
        try {
            method = hostDetail[0];
            if (method.equals("connect") || method.equals("CONNECT")) {
                port = 443;
                protocol = "https";
            } else {
                port = 80;
                protocol = "http";
            }
            String urlString = hostDetail[1];
            if ("http".regionMatches(0, urlString, 0, 3)) {
                urlString = urlString.substring(7);
            }
            if (urlString.contains(":")) {
                String temp = urlString.substring(urlString.indexOf(":") + 1);
                if (temp.contains("/")) {
                    temp = temp.substring(0, temp.indexOf("/"));
                }
                port = Integer.valueOf(temp);
                urlString = urlString.substring(0, urlString.indexOf(":"));
            } else if (urlString.contains("/")) {
                urlString = urlString.substring(0, urlString.indexOf("/"));
            }
            host = urlString;
        } catch (ArrayIndexOutOfBoundsException ex) {
            for (String temp : hostDetail) {
                System.out.println(temp);
            }
            throw ex;
        }
    }


    private void processHTTPRequest() {
        try {
            socketFromProxyServer = new Socket(host, port);
            proxyToServerOP = socketFromProxyServer.getOutputStream();
            proxyToServerIP = socketFromProxyServer.getInputStream();
            proxyRequest();
        } catch (IOException ex) {
            System.out.println("In Processing Get Method" + ex);
        }
    }

    /**
     * Need to Send Success response after Connecting to Server to create HTTPS Tunnel.
     */
    private void processHTTPSRequest() {
        try {
            socketFromProxyServer = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyToClientOP));
            writer.write("HTTP/1.1 200 Connection established\r\n" + "\r\n");
            writer.flush();
            proxyToServerOP = socketFromProxyServer.getOutputStream();
            proxyToServerIP = socketFromProxyServer.getInputStream();
            proxyRequest();
        } catch (IOException ex) {
            System.out.println("In processing connect" + ex);
        }
    }


    private void proxyRequest() {
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (!method.equalsIgnoreCase("connect") && headInfo != null) {
                            proxyToServerOP.write(headInfo, 0, headInfo.length);
                            proxyToServerOP.flush();
                        }
                        byte[] read = new byte[1024];
                        int in;
                        while ((in = proxyToClientIP.read(read)) != -1) {
                            requestSize += in;
                            proxyToServerOP.write(read, 0, in);
                            proxyToServerOP.flush();
                        }
                        System.out.println("requestSze" + requestSize);
                        proxyToServerOP.close();
                        proxyToClientIP.close();
                    } catch (IOException ex) {
                        System.out.println("Exception in Proxy to Server Method" + ex);
                    }
                }
            }.start();
            byte[] reply = new byte[1024];
            int out;
            while ((out = proxyToServerIP.read(reply)) != -1) {
                responseSize += out;
                proxyToClientOP.write(reply, 0, out);
                proxyToClientOP.flush();
            }
            System.out.println("responseSize = " + responseSize);
            proxyToClientOP.close();
            proxyToServerIP.close();
        } catch (IOException ex) {
            System.out.println("Exception in Proxy to Client method" + ex);
        }
    }
}

