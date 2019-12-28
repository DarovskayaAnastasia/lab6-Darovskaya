import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.setup.ActorSystemSetup;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.ServerBinding;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.actor.TypedActor.context;

public class AnonymizerApp {

    public static void main(String[] args) throws IOException {

        ActorSystem system = ActorSystem.create("routes", /*ActorSystemSetup.empty()*/);

        final Http http = Http.get(context().system());

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        ActorRef configurationActor = system.actorOf(ConfigurationKeeperActor.props(), "configurationActor");

//        Patterns.ask(configurationActor, new ConfigurationKeeperActor("localhost:8008"), Duration.ofMillis(2000L));

        Server server = new Server(http, 8008, configurationActor);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = server.createRoute().flow(system, materializer);


        CompletionStage<HttpResponse> fetch (String url){
            return http.singleRequest(HttpRequest.create(url));
        }
    }
}

//
//    ZooKeeper(String connectString,
//              int sessionTimeout,
//              Watcher watcher)
//
//    ZooKeeper zoo = new ZooKeeper("1MB27.0.0.1MB:21MB81MB", 3000, this);
//zoo.create("/servers/s","data".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
//        List<String> servers=zoo.getChildren("/servers",this);
//        for(
//        String s:servers)
//
//        {
//        byte[]data=zoo.getData("/servers/"+s,false,null);
//        System.out.println("server "+s+" data="+new String(data


class Server {
}