package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private VirtualTerminal virtualTerminal = new VirtualTerminal();
    private boolean isNextBroadcastCommand = false;
    private boolean isRunning = true;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has joined the chat");
            sendTerminalResponse("Welcome to the chat server, " + clientUsername + "!\nEnter your command> ");

        } catch (Exception e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    private void runServerCommand(String command) {
        switch (command.trim()) {
            case "exit":
                sendTerminalResponse("Server closed. Enter any key to exit.");
                exit();
                break;
            case "msg":
                isNextBroadcastCommand = true;
                sendTerminalResponse("Enter your message to broadcast> ");
                break;
            default:
                break;
        }
    }

    private void invokeCommand(String message) {
        if (isNextBroadcastCommand) {
            broadcastMessage(message);
            isNextBroadcastCommand = false;
            sendTerminalResponse("Message sent. Enter your command>");
            return;
        }
        // String response = Parser.parse(message, virtualTerminal);
        CommandType commandType = Parser.getCommandType(message);
        if (commandType == CommandType.TERMINAL) {
            String response = Parser.parseTerminal(message, virtualTerminal);
            // if (response == null) {
            // if (Parser.isBroadcastCommand(message)) {
            // isNextBroadcastCommand = true;
            // response = "Enter your message to broadcast> ";
            // } else {
            // response = "Invalid command, try again.";
            // }
            // }
            if (response == null) {
                response = "Invalid command " + message + ", try again.";
            }
            sendTerminalResponse(response);
        } else if (commandType == CommandType.SERVER) {
            runServerCommand(message.substring(1));
        }
    }

    private void sendTerminalResponse(String response) {
        try {
            bufferedWriter.write(response);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected() || isRunning) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    broadcastMessage("SERVER: " + clientUsername + " has left the chat");
                    closeEverything();
                    break;
                }
                // if (messageFromClient.equals("exit")) {
                // closeEverything(socket, bufferedReader, bufferedWriter);
                // } else {
                // broadcastMessage(clientUsername + ": " + messageFromClient);
                // }
                invokeCommand(messageFromClient);
            } catch (Exception e) {
                // e.printStackTrace();
                exit();
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        // color message to blue
        message = "\u001B[34m" + clientUsername + ": " + message + "\u001B[0m";
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                closeEverything();
            }
        }
    }

    public void exit() {
        if (!isRunning)
            return;
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
        closeEverything();
        isRunning = false;
    }

    public void closeEverything() {
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
