package server.model;

import java.net.Socket;

public class ThreadIDUserName {
    private long threadID;
    private Socket clientSocket;
    private String userName;

    public ThreadIDUserName(long threadID, String userName) {
        this.threadID = threadID;
        this.userName = userName;
    }

    public long getThreadID() {
        return threadID;
    }

    public void setThreadID(long threadID) {
        this.threadID = threadID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
