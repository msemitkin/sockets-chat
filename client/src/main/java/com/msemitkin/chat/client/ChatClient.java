package com.msemitkin.chat.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        try (
            Socket server = new Socket("localhost", 6666);
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            OutputStream outputStream = server.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true)
        ) {
            System.out.println("Successfully connected to server");
            while (true) {
                System.out.println("Waiting for input");
                String input = bufferedReader.readLine();
                printWriter.println(input);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
