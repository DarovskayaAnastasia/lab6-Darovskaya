public class ServerListMessage {

    private final String[] serverList;

    public ServerListMessage() {
        serverList = NULL;
    }

    public ServerListMessage(String[] serverList) {
        this.serverList = serverList;
    }

    public String[] getServerList() {
        return serverList;
    }
}
