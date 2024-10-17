package serverclient;

import configurations.ConnectionFilters;
import dbhandler.DBInitializer;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyServer extends Thread {
    private static final Logger logger = Logger.getLogger(ProxyServer.class.getName());

    public static void main(String[] args) {
        // Main method implementation
    }

    private String host;
    private int port = -1;
    private int requestSize;
    private int responseSize;
    private InputStream proxyServerIPStream;
    private OutputStream proxyServerOPStream;
    private InputStream hostServerIPStream;
    private OutputStream hostServerOPStream;
    private final Socket proxySocket;
    private Socket hostServerSocket;
    private String method;
    private byte[] headInfo = new byte[1024];
    private String protocol;

    ProxyServer(Socket socket) {
        this.proxySocket = socket;
        this.start();
    }

    public void run() {
        try {
            proxyServerIPStream = proxySocket.getInputStream();
            proxyServerOPStream = proxySocket.getOutputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int c;
            for (c = proxyServerIPStream.read(); c != -1; c = proxyServerIPStream.read()) {
                bytes.write(c);
                if (c == (int) '\n') break;
            }
            String hostHeader = bytes.toString();
            requestSize += bytes.size();
            String[] hostDetails = hostHeader.split(" ");
            parseHostDetails(hostDetails);
            securityFilter();
            if (method.equalsIgnoreCase("connect")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(proxyServerIPStream));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    requestSize += temp.getBytes().length;
                    if (temp.isEmpty()) break;
                }
                processHTTPSRequest();
            } else {
                headInfo = bytes.toByteArray();
                processHTTPRequest();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error in run method", ex);
        } finally {
            closeResources();
        }
    }

    private void securityFilter() throws IOException {
        if (ConnectionFilters.isSiteBlocked(host) || ConnectionFilters.isPortBlocked(port) || ConnectionFilters.isProtocolBlocked(protocol)) {
            String blockMessage = "<html><body><h1> </h1><p> Site is Blocked </p></body></html>";
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyServerOPStream));
            writer.write("HTTP/1.1 400 Bad Request\r\n");
            writer.write("Content-Type:text/html\r\n\r\n");
            writer.write(blockMessage);
            writer.flush();
            writer.close();
        }
    }

    private void parseHostDetails(String[] hostDetail) {
        try {
            method = hostDetail[0];
            protocol = method.equalsIgnoreCase("connect") ? "https" : "http";
            port = protocol.equalsIgnoreCase("http") ? 80 : 443;
            String urlString = hostDetail[1];
            if ("http".regionMatches(0, urlString, 0, 3)) {
                urlString = urlString.substring(7);
            }
            if (urlString.contains(":")) {
                String temp = urlString.substring(urlString.indexOf(":") + 1);
                if (temp.contains("/")) {
                    temp = temp.substring(0, temp.indexOf("/"));
                }
                port = Integer.parseInt(temp);
                urlString = urlString.substring(0, urlString.indexOf(":"));
            } else if (urlString.contains("/")) {
                urlString = urlString.substring(0, urlString.indexOf("/"));
            }
            host = urlString;
        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.log(Level.WARNING, "Error parsing host details: " + Arrays.toString(hostDetail), ex);
        }
    }

    private void processHTTPRequest() {
        try {
            hostServerSocket = new Socket(host, port);
            hostServerOPStream = hostServerSocket.getOutputStream();
            hostServerIPStream = hostServerSocket.getInputStream();
            proxyRequest();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error processing HTTP request", ex);
        }
    }

    private void processHTTPSRequest() {
        try {
            hostServerSocket = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proxyServerOPStream));
            writer.write("HTTP/1.1 200 Connection established\r\n\r\n");
            writer.flush();
            hostServerOPStream = hostServerSocket.getOutputStream();
            hostServerIPStream = hostServerSocket.getInputStream();
            proxyRequest();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error processing HTTPS request", ex);
        }
    }

    private void proxyRequest() {
        try {
            logger.info("Host details: " + host);
            Thread t = new Thread(() -> {
                try {
                    if (!method.equalsIgnoreCase("connect") && headInfo != null) {
                        hostServerOPStream.write(headInfo, 0, headInfo.length);
                        hostServerOPStream.flush();
                    }
                    byte[] read = new byte[1024];
                    int in;
                    while ((in = proxyServerIPStream.read(read)) != -1) {
                        requestSize += in;
                        hostServerOPStream.write(read, 0, in);
                        hostServerOPStream.flush();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Exception in proxy to server method", ex);
                } finally {
                    closeStreams(proxyServerIPStream, hostServerOPStream);
                }
            });
            t.start();
            byte[] reply = new byte[1024];
            int out;
            while ((out = hostServerIPStream.read(reply)) != -1) {
                responseSize += out;
                proxyServerOPStream.write(reply, 0, out);
                proxyServerOPStream.flush();
            }
            t.join();
            logger.info("Response size: " + responseSize);
            DBInitializer.getInstance().runQuery(host, port, protocol, requestSize, responseSize);
        } catch (IOException | InterruptedException | SQLException ex) {
            logger.log(Level.SEVERE, "Exception in proxy to client method", ex);
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        closeStreams(proxyServerIPStream, hostServerIPStream, proxyServerOPStream, hostServerOPStream);
        try {
            if (proxySocket != null && !proxySocket.isClosed()) {
                proxySocket.close();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error closing proxy socket", ex);
        }
    }

    private void closeStreams(Closeable... streams) {
        for (Closeable stream : streams) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error closing stream", ex);
                }
            }
        }
    }
}