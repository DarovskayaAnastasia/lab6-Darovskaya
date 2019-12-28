import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

public class ConfigurationKeeperActor extends AbstractActor {



    @Override
    public Receive createRreceive() {
        return ReceiveBuilder.create().match().build();
    }
}
