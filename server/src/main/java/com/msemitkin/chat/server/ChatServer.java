package com.msemitkin.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    public static void main(String[] args) throws IOException {
        new ChatServer().start(6666);
    }

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Started server on %d port%n", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> listenToClient(clientSocket)).start();
                System.out.println("New client: " + clientSocket.getPort());
            }
        }
    }

    private void listenToClient(Socket socket) {
        try (
            InputStream socketInputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(socketInputStream);
            BufferedReader socketReader = new BufferedReader(inputStreamReader);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        ) {
            String line;
            while ((line = socketReader.readLine()) != null) {
                System.out.printf("Received from %s: %n".concat(line), socket.getPort());
                printWriter.println("Received from you: " + line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
