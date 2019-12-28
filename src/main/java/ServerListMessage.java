public class ServerListMessage {

    private final String[] serverList;

    public ServerListMessage() {
        serverList = null;
    }

    public ServerListMessage(String[] serverList) {
        this.serverList = serverList;
    }

    public String[] getServerList() {
        return serverList;
    }
}
