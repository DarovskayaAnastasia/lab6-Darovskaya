import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.Random;

public class ConfigurationKeeperActor extends AbstractActor {

    private String[] servers;

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(ServerListMessage.class, msg -> {
            servers = msg.getServerList();
        }).
                match(RandomServerRequestMessage.class, msg -> sender().tell(new Random().nextInt(servers.length), self())
                ).build();
    }
}
