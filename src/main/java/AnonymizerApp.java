import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.ServerBinding;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.actor.TypedActor.context;

public class AnonymizerApp {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        System.out.println("start!");

        int port = 8001;
        String host = "localhost";
        if(args.length > 0){
            host = args[0];
        }
        if (args.length > 1){
            port = Integer.parseInt(args[1]);
        }


        ActorSystem system = ActorSystem.create("routes"/*, ActorSystemSetup.empty()*/);

        final Http http = Http.get(system);

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        ActorRef configurationActor = system.actorOf(ConfigurationKeeperActor.props(), "configurationActor");


        HttpServer server = new HttpServer(http, configurationActor, port);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = server.createRoute().flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", port), materializer);

        System.out.println("Server started on port: " + port);
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());

    }

}

//create Node with server url and watchs itself
class ZooClient implements Watcher {
    private static final String ZOOKEEPER_SERVER_URL = "127.0.0.1:2181";

    //    private Http http;
    private ActorRef configurationActor;
    //    private int port;
    private ZooKeeper zoo;

    public ZooClient(/*final Http http,*/ ActorRef configurationActor) throws IOException {
//        this.http = http;

        this.configurationActor = configurationActor;
        this.zoo = new ZooKeeper(ZOOKEEPER_SERVER_URL, 2000, null);

    }

    public void creaeteServer(String serverUrl) throws KeeperException, InterruptedException {
        String s = zoo.create("/servers/s", serverUrl.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
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
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class HttpServer extends AllDirectives {

    private final static String LOCALHOST = "http://localhost:";

    private Http http;
    private ActorRef configurationActor;
    private String serverUrl;

    public HttpServer(final Http http, ActorRef configurationActor, int port) throws KeeperException, InterruptedException, IOException {
        this.http = http;
        this.configurationActor = configurationActor;
        serverUrl = LOCALHOST + port;

        ZooClient zookeeperService = new ZooClient(configurationActor);
        zookeeperService.creaeteServer(serverUrl);
    }

    private CompletionStage<HttpResponse> fetch(String url) {
        return http.singleRequest(HttpRequest.create(url));
    }

    public Route createRoute() {
        return get(() ->
                parameter("url", url ->
                        parameter("count", count -> {
                            int c = Integer.parseInt(count);

                            return c == 0 ?
                                    completeWithFuture(fetch(url)) :
                                    completeWithFuture(redirectToServer(url, c));
                        })
                )
        );
    }

    private CompletionStage<HttpResponse> redirectToServer(String url, int count) {
        return Patterns.ask(configurationActor, new ConfigurationKeeperActor(), Duration.ofMillis(2000L))
                .thenCompose(urlLambda -> fetch(generateUrl((String) urlLambda, url, count)));
    }

    private String generateUrl(String url, String queryUrl, int count) {
        return Uri.create(url)
                .query(Query.create(
                        Pair.create("url", queryUrl),
                        Pair.create("count", Integer.toString(count - 1))
                ))
                .toString();
    }
}