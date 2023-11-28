package client;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

class CommandParseResult {
    public String message;
    public boolean valid;

    public CommandParseResult(String message, boolean valid) {
        this.message = message;
        this.valid = valid;
    }
}

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private DataInputStream dataInputStream;
    private String username;
    private ClientStateSync clientStateSync = new ClientStateSync();
    private CommandRunner commandRunner = new CommandRunner(clientStateSync, this.bufferedWriter);

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // this.bufferedReader = new BufferedReader(new
            // InputStreamReader(socket.getInputStream()));
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.username = username;
        } catch (Exception e) {
            closeEverything(socket, dataInputStream, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scan = new Scanner(System.in);
            while (socket.isConnected() && clientStateSync.isServerConnected()) {
                var parseResult = commandRunner.parseCommand(scan.nextLine());
                if (!parseResult.valid) {
                    System.out.println(parseResult.message);
                    continue;
                } else {
                    bufferedWriter.write(parseResult.message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            scan.close();
        } catch (Exception e) {
            closeEverything(socket, dataInputStream, bufferedWriter);
        }
    }

    private void downloadFromReader() {
        String filename = clientStateSync.getFilename();
        // create file if it does not exist

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename, true)) {
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
        } catch (IOException e1) {
            System.out.println("Could not download file");
            // empty the data input stream
            try {
                while (dataInputStream.available() > 0) {
                    dataInputStream.read();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    private boolean printFromReader() {
        try {
            int c = dataInputStream.read();
            if (c == -1)
                return false;
            System.out.print((char) c);
            while (dataInputStream.available() > 0) {
                c = dataInputStream.read();
                if (c == -1)
                    break;
                System.out.print((char) c);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void listenForMessage() {
        new Thread(() -> {
            String messageFromChat;
            while (socket.isConnected()) {
                try {
                    if (clientStateSync.isDownloading()) {
                        downloadFromReader();
                        clientStateSync.unsetDownloading();
                        continue;
                    } else {
                        boolean isPrinted = printFromReader();
                        if (!isPrinted) {
                            closeEverything();
                            clientStateSync.setServerDisconnected();
                            System.out.println("Server closed");
                            return;
                        }
                    }
                    // messageFromChat = bufferedReader.readLine();
                    // if (messageFromChat == null) {
                    // closeEverything(socket, bufferedReader, bufferedWriter);
                    // break;
                    // }
                    // System.out.println(messageFromChat);
                } catch (Exception e) {
                    closeEverything();
                    clientStateSync.setServerDisconnected();
                    System.out.println("Server closed");
                    return;
                }
            }
        }).start();
    }

    public void closeEverything() {
        closeEverything(socket, dataInputStream, bufferedWriter);
    }

    public void closeEverything(Socket socket, DataInputStream reader, BufferedWriter writer) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scan.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
        scan.close();
        // client.closeEverything();
        System.out.println("Client closed");
    }
}
// malloc for struct, calloc, realloc, free
// struct, union
// linked list, stack, queue, binary tree
// file handling