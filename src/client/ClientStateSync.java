package client;

public class ClientStateSync {
    private boolean isDownloading = false;
    private boolean serverDisconnected = false;
    private String filename = null;

    public synchronized boolean isServerConnected() {
        return !serverDisconnected;
    }

    public synchronized void setServerDisconnected() {
        this.serverDisconnected = true;
    }

    public synchronized boolean isDownloading() {
        return isDownloading;
    }

    public synchronized String getFilename() {
        return filename;
    }

    public synchronized void setDownloading(String filename) {
        this.isDownloading = true;
        this.filename = filename;
    }

    public synchronized void unsetDownloading() {
        this.isDownloading = false;
        this.filename = null;
    }
}