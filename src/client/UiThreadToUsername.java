package client;

public class UiThreadToUsername {
    private long threadID;
    private String userName;

    public UiThreadToUsername(long threadID, String userName) {
        this.threadID = threadID;
        this.userName = userName;
    }

    public long getThreadID() {
        return threadID;
    }

    public static long getThreadIdByUserName(String userName) {
        for (UiThreadToUsername uiThreadToUsername : Client.uiThreadToUsernameList) {
            if (uiThreadToUsername.getUserName().equals(userName)) {
                return uiThreadToUsername.getThreadID();
            }
        }
        return -1;
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
