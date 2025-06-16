package cn.itcast.hilink;

import android.widget.TextView;
import java.io.*;
import java.net.*;

public class ShellServer {

    private static ServerSocket serverSocket;
    private static boolean isRunning = false;
    private static Socket latestClientSocket = null;

    public static void startServer(int port, TextView statusView) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;
                updateUI(statusView, "Shell Server started on port " + port);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    latestClientSocket = clientSocket;
                    updateUI(statusView, "客户端已连接: " + clientSocket.getInetAddress());
                    handleClient(clientSocket, statusView);
                }
            } catch (IOException e) {
                updateUI(statusView, "错误: " + e.getMessage());
            } finally {
                stopServer();
            }
        }).start();
    }

    public static Socket getClientSocket() {
        return latestClientSocket;
    }

    public static void stopServer() {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClient(Socket clientSocket, TextView statusView) {
        new Thread(() -> {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"))
            ) {
                out.write("已连接到控制端 Android shell\n");
                out.flush();

                String line;
                while ((line = in.readLine()) != null) {
                    updateUI(statusView, "被控端: " + line);
                }

            } catch (IOException e) {
                updateUI(statusView, "客户端错误: " + e.getMessage());
            }
        }).start();
    }

    private static void updateUI(TextView textView, String message) {
        textView.post(() -> textView.append("\n" + message));
    }
}
