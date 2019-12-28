import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.setup.ActorSystemSetup;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.Query;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.AllDirectives;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.ServerBinding;
import akka.stream.javadsl.Flow;
import jdk.internal.vm.compiler.collections.Pair;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.actor.TypedActor.context;

public class AnonymizerApp {

    public static void main(String[] args) throws IOException {

        ActorSystem system = ActorSystem.create("routes"/*, ActorSystemSetup.empty()*/);

        final Http http = Http.get(context().system());

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        ActorRef configurationActor = system.actorOf(ConfigurationKeeperActor.props(), "configurationActor");

        Patterns.ask(configurationActor, new ConfigurationKeeperActor(), Duration.ofMillis(2000L)).thenCompose(url -> fetch(Uri.create(url)
                .query(Query.create(
                        Pair.create(URL_PARAM_NAME, queryUrl),
                        Pair.create(COUNT_PARAM_NAME, Integer.toString(count - 1))
                ))
                .toString();));

        Server server = new Server(http, 8008, configurationActor);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = server.createRoute().flow(system, materializer);


        CompletionStage<HttpResponse> fetch (String url){
            return http.singleRequest(HttpRequest.create(url));
        }
    }

    private void generateUrl(String url, String queryUrl, int count) {
        
    }
}
class Server implements Watcher {
    private static final String ZOOKEEPER_SERVER_URL = "127.0.0.1:2181";

    private Http http;
    private ActorRef configurationActor;
    private int port;
    private ZooKeeper zoo;

    public Server(final Http http, ActorRef configurationActor) throws IOException {
        this.http = http;
        this.configurationActor = configurationActor;

        this.zoo = new ZooKeeper(ZOOKEEPER_SERVER_URL, 2000, null);
    }

    public void creaeteServer() throws KeeperException, InterruptedException {
        String s = zoo.create("/servers/s", ("http://localhost:" + port).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            List<String> serverNodes = zoo.getChildren("/servers", this);

            List<String> servers = new ArrayList<>();
            for (String s : serverNodes) {
                byte[] data = zoo.getData("/servers/" + s, false, null);
                System.out.println("server " + s + " data=" + new String(data));
                servers.add(new String(data));
            }

            configurationActor.tell(new ServerListMessage(servers.toArray(new String[0])), ActorRef.noSender());
        }
        catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}