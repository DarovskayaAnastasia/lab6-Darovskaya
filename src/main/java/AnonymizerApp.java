import akka.actor.ActorSystem;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

public class AnonymizerApp {

    public static void main(String[] args) throws IOException {

        ActorSystem system = ActorSystem.create("")
    }
}

    ZooKeeper(String connectString,
              int sessionTimeout,
              Watcher watcher)

    ZooKeeper zoo = new ZooKeeper("1MB27.0.0.1MB:21MB81MB",3000,this);
zoo.create("/servers/s", "data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE ,CreateMode.EPHEMERAL_SEQUENTIAL);
    List<String> servers = zoo.getChildren("/servers", this);
for(
    String s :servers)

    {
        byte[] data = zoo.getData("/servers/" + s, false, null);
        System.out.println("server " + s + " data=" + new String(data
}
