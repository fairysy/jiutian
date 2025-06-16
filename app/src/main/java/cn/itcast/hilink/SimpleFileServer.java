package cn.itcast.hilink;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleFileServer {
    private ServerSocket serverSocket;
    private boolean isRunning;
    private File fileToServe;
    private int port;

    public SimpleFileServer(int port, File fileToServe) {
        this.port = port;
        this.fileToServe = fileToServe;
    }

    public void start() {
        isRunning = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (OutputStream output = clientSocket.getOutputStream();
             FileInputStream fileInput = new FileInputStream(fileToServe);
             BufferedInputStream bis = new BufferedInputStream(fileInput)) {

            // 构建HTTP响应头
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Length: " + fileToServe.length() + "\r\n" +
                    "Content-Disposition: attachment; filename=\"" + fileToServe.getName() + "\"\r\n\r\n";

            output.write(header.getBytes());

            // 发送文件内容
            byte[] buffer = new byte[8192];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                output.write(buffer, 0, count);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        isRunning = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}