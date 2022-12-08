package com.msemitkin.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;

public class ChatClient {

    public static void main(String[] args) throws IOException {
        new ChatClient().start();
    }

    void start() throws IOException {
        Socket server = new Socket("localhost", 6666);
        System.out.println("Successfully connected to server");
        new Thread(() -> sendUpdatesToServer(server)).start();
        new Thread(() -> listenToUpdatesFromServer(server)).start();
    }

    private void listenToUpdatesFromServer(Socket serverSocket) {
        try (
            var serverInputStreamReader = new InputStreamReader(serverSocket.getInputStream());
            BufferedReader serverReader = new BufferedReader(serverInputStreamReader)
        ) {
            System.out.println("Waiting for updates from the server");
            String line;
            while ((line = serverReader.readLine()) != null) {
                System.out.println("> " + line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void sendUpdatesToServer(Socket server) {
        try (
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            OutputStream outputStream = server.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true)
        ) {
            System.out.println("Waiting for updates from user");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("Waiting for input");
                printWriter.println(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
