package server;

public class Parser {
    public static CommandType getCommandType(String message) {
        if (message.startsWith("/")) {
            return CommandType.SERVER;
        }
        return CommandType.TERMINAL;
    }

    public static String parseTerminal(String message, VirtualTerminal virtualTerminal) {
        String[] messageArray = message.split("\\s+");
        String command = messageArray[0];
        String argument = null;
        if (messageArray.length > 1) {
            argument = messageArray[1];
        }
        // String argument = messageArray[1];
        String response = "";
        switch (command) {
            case "ls":
                response = virtualTerminal.ls();
                break;
            case "pwd":
                response = virtualTerminal.pwd();
                break;
            case "cd":
                response = virtualTerminal.cd(argument);
                break;
            case "r--r":
                response = "rerouting to file";
                break;
            // case "mkdir":
            // response = VirtualTerminal.mkdir(argument);
            // break;
            // case "rm":
            // response = VirtualTerminal.rm(argument);
            // break;
            // case "cp":
            // response = VirtualTerminal.cp(argument);
            // break;
            // case "mv":
            // response = VirtualTerminal.mv(argument);
            // break;
            // case "cat":
            // response = VirtualTerminal.cat(argument);
            // break;
            // case "exit":
            // response = VirtualTerminal.exit();
            // break;
            default:
                response = null;
                break;
        }
        return response;
    }
}
