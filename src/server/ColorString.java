package server;

public class ColorString {
    public static String toBlue(String message) {
        return wrap(message, "\u001B[34m");
    }

    public static String toRed(String message) {
        return wrap(message, "\u001B[31m");
    }

    public static String toGreen(String message) {
        return wrap(message, "\u001B[32m");
    }

    private static String wrap(String message, String color) {
        return (color + message + "\u001B[0m");
    }
}
