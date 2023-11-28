package client;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;

public class CommandRunner {
    private ClientStateSync downloadStateSync;
    // a hack to make sure downloaded data is not printed to terminal
    private BufferedWriter bufferedWriter;

    public CommandRunner(ClientStateSync downloadStateSync, BufferedWriter bufferedWriter) {
        this.downloadStateSync = downloadStateSync;
        this.bufferedWriter = bufferedWriter;
    }

    public CommandParseResult parseCommand(String command) {
        String[] commandArray = command.split("\\s+");
        String commandWord = commandArray[0];
        // String argument = null;
        // if (commandArray.length > 1) {
        // argument = commandArray[1];
        // }
        if (commandWord.startsWith("/")) {
            commandWord = commandWord.substring(1);
            switch (commandWord) {
                case "reroute":
                    String filename = null;
                    if (commandArray.length == 2) {
                        filename = commandArray[1];
                    } else {
                        return new CommandParseResult("[error] /reroute [filename]", false);
                    }
                    this.downloadStateSync.setDownloading(filename);
                    return new CommandParseResult("r--r " + filename, true);
                case "std":
                    this.downloadStateSync.unsetDownloading();
                    return new CommandParseResult("output to terminal", false);
                default:
                    break;
            }
        }
        return new CommandParseResult(command, true);
    }
}
