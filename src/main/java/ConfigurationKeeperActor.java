import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import java.util.Random;

public class ConfigurationKeeperActor extends AbstractActor {

    private String[] servers;

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(ServerListMessage.class, msg -> {
            servers = msg.getServerList();
        }).
                match(RandomServerRequestMessage.class, msg -> sender().tell(newServer(), self())
                ).build();
    }

    public static Props props() {
        return Props.create(ConfigurationKeeperActor.class);
    }

    private String newServer() {
        String serverUrl = servers[new Random().nextInt(servers.length)];
        System.out.println("redirect to " + serverUrl);
        return serverUrl;
    }
}
