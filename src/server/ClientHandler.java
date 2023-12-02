package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable, BeanValueListener {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private DataOutputStream dataOutputStream;
    private String clientUsername;
    private Bean clientCountBean;
    private VirtualTerminal virtualTerminal = new VirtualTerminal();
    private boolean isNextBroadcastCommand = false;
    private boolean isRunning = true;

    public ClientHandler(Socket socket, Bean bean) {
        this.clientCountBean = bean;
        this.clientCountBean.addListener(this);
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // create writer for binary data
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage(clientUsername + " has joined the chat", false);
            sendTerminalResponse("Welcome to the chat server, " + clientUsername + "!\nEnter your command> ");

        } catch (Exception e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    @Override
    public void awareOfChangeInValue(EventBeanValue ev) {
        int newValue = ev.getNewValue();
        sendTerminalResponse(ColorString.toRed("There are now " + newValue + " clients connected"));
    }

    private void runServerCommand(String command) {
        String[] commandParts = command.split("\\s+");
        switch (commandParts[0].trim()) {
            case "exit":
                sendTerminalResponse("Server closed. Enter any key to exit.");
                exit();
                break;
            case "msg":
                isNextBroadcastCommand = true;
                sendTerminalResponse("Enter your message to broadcast> ");
                break;
            case "download":
                try {
                    String filename = commandParts[1];
                    // send file size
                    // dataOutputStream.writeLong(virtualTerminal.getFileSize(filename));
                    // send file
                    virtualTerminal.sendFile(filename, dataOutputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendTerminalResponse("Error uploading file");
                }
                break;
            default:
                sendTerminalResponse("Invalid command '" + command + "', try again.");
                break;
        }
    }

    private void invokeCommand(String message) {
        if (isNextBroadcastCommand) {
            broadcastMessage(message, true);
            isNextBroadcastCommand = false;
            sendTerminalResponse("Message sent. Enter your command>");
            return;
        }
        // String response = Parser.parse(message, virtualTerminal);
        CommandType commandType = Parser.getCommandType(message);
        if (commandType == CommandType.TERMINAL) {
            String response = Parser.parseTerminal(message, virtualTerminal);
            if (response == null) {
                response = "Invalid command '" + message + "', try again.";
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
        while (socket.isConnected() && isRunning) {
            try {
                messageFromClient = bufferedReader.readLine();
                System.out.println(messageFromClient);
                // isRunning = false;
                if (messageFromClient == null) {
                    broadcastMessage(clientUsername + " has left the chat", false);
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
                e.printStackTrace();
                exit();
                break;
            }
        }
    }

    public void broadcastMessage(String message, boolean isUserMessage) {
        // color message to blue
        String prepend = isUserMessage ? clientUsername + ": " : "SERVER: ";
        message = "\u001B[34m" + prepend + message + "\u001B[0m";
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
        // if (!isRunning)
        // return;
        clientHandlers.remove(this);
        broadcastMessage(clientUsername + " has left the chat", false);
        closeEverything();
        System.out.println("Exiting");
        isRunning = false;
        this.clientCountBean.removeListener(this);
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
            System.out.println("Error closing client handler");
            e.printStackTrace();
        }
    }
}
