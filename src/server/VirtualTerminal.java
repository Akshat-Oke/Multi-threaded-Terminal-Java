package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;
import java.nio.file.Path;

public class VirtualTerminal {
    private Path currentDirectory;
    // The 'cat' command supports pagination through the use of the 'more' command.
    // store the state of the cat command
    // a page is defined as 10 lines

    // collect all of the above variables in a private class
    // use a nameless class
    private class CatCommand {
        private String catCommandFileName;
        private int lineNum = 0;

        public CatCommand(String filename) {
            this.catCommandFileName = filename;
        }
    } // end of CatCommand class

    private CatCommand catCommand = null;

    public VirtualTerminal() {
        // this.currentDirectory = System.getProperty("user.dir");
        this.currentDirectory = Path.of(System.getProperty("user.dir"));
    }

    public String more() {
        String response = "";
        if (catCommand == null) {
            response = "[more] No file to paginate";
        } else {
            response = cat(catCommand.catCommandFileName, true);
        }
        return response;
    }

    public String cat(String argument, boolean isMore) {
        if (argument == null) {
            return "[cat] No file specified";
        }
        String response = "";
        if (catCommand == null || !isMore) {
            catCommand = new CatCommand(argument);
        }
        int startLine = catCommand.lineNum;
        try (BufferedReader br = new BufferedReader(new FileReader(currentDirectory.resolve(argument).toString()))) {
            String line;
            for (int i = 0; i < startLine; i++) {
                br.readLine();
            }
            while ((line = br.readLine()) != null && ++catCommand.lineNum <= startLine + 10) {
                response += line + "\n";
            }
            if (line == null) {
                catCommand = null;
                response += "[cat: End of file]";
            }
        } catch (IOException e) {
            catCommand = null;
            response = "[cat] File not found";
        }
        return response;
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

    public void sendFile(String fileName, DataOutputStream writer) {
        try {
            // writer.writeChars(fileName + ": download\n");
            // write binary data into writer
            DataInputStream reader = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(new File(currentDirectory.resolve(fileName).toString()))));
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
