package server;

import java.io.BufferedWriter;
import java.util.stream.Stream;
import java.nio.file.Path;

public class VirtualTerminal {
    private Path currentDirectory;

    public VirtualTerminal() {
        // this.currentDirectory = System.getProperty("user.dir");
        this.currentDirectory = Path.of(System.getProperty("user.dir"));
    }

    public String pwd() {
        return currentDirectory.toString();
    }

    public String cd(String argument) {
        String response = "";
        if (argument.equals("..")) {
            currentDirectory = currentDirectory.getParent();
            response = "Changed directory to " + currentDirectory;
        } else {
            Path newD = currentDirectory.resolve(argument);
            if (newD.toFile().exists() && newD.toFile().isDirectory()) {
                currentDirectory = newD;
                response = "Changed directory to " + currentDirectory;
            } else {
                response = "Directory does not exist or is not a directory";
            }
        }
        return response;
    }

    public String ls(/* String argument */) {
        String response = "";
        // if (argument.equals("-l")) {
        // response = listFilesWithDetails();
        // } else {
        // response = listFiles();
        // }
        var files = Stream.of(currentDirectory.toFile().listFiles())
                .map(file -> {
                    if (file.isDirectory()) {
                        return file.getName() + "/";
                    } else {
                        return file.getName();
                    }
                })
                .toArray();
        for (Object file : files) {
            response += file + "\n";
        }
        return response;
    }

    public void sendFile(String fileName, BufferedWriter writer) {
        try {
            writer.write("file");
            writer.newLine();
            writer.flush();
            writer.write(fileName);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
