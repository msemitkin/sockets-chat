package com.msemitkin.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {
    private final Map<Integer, Socket> clients = new HashMap<>();
    private final Map<Integer, LinkedBlockingQueue<String>> messages = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new ChatServer().start(6666);
    }

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Started server on %d port%n", port);
            new Thread(this::processMessageQueues).start();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.put(clientSocket.getPort(), clientSocket);
                messages.put(clientSocket.getPort(), new LinkedBlockingQueue<>());
                new Thread(() -> listenToClient(clientSocket)).start();
                System.out.println("New client: " + clientSocket.getPort());
            }
        }
    }

    private void listenToClient(Socket socket) {
        try (
            InputStream socketInputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(socketInputStream);
            BufferedReader socketReader = new BufferedReader(inputStreamReader)
        ) {
            String line;
            while ((line = socketReader.readLine()) != null) {
                processMessage(line, socket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMessage(String message, Socket from) {
        System.out.printf("Received from %s: %s%n", from.getPort(), message);
        if (message.startsWith("/b ")) {
            broadcastMessage(message);
        } else {
            int receiver = parseReceiver(message);
            String content = parseContent(message);
            messages.get(receiver).add(content);
        }
    }

    private void broadcastMessage(String message) {
        messages.values().forEach(queue -> queue.add(message));
    }

    private int parseReceiver(String input) {
        int index = input.indexOf(" ");
        return Integer.parseInt(input.substring(1, index));
    }

    private String parseContent(String input) {
        return input.substring(input.indexOf(" ") + 1);
    }

    private void processMessageQueues() {
        while (true) {
            for (Map.Entry<Integer, LinkedBlockingQueue<String>> entry : messages.entrySet()) {
                Socket receiver = clients.get(entry.getKey());
                var messagesForReceiver = entry.getValue();
                if (!messagesForReceiver.isEmpty()) {
                    System.out.printf("%d has non-empty queue%n", receiver.getPort());
                    try {
                        PrintWriter printWriter = new PrintWriter(receiver.getOutputStream(), true);
                        while (!messagesForReceiver.isEmpty()) {
                            String message = messagesForReceiver.remove();
                            printWriter.println(message);
                            System.out.printf("Sent message %s to client %d%n", message, receiver.getPort());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }
}
