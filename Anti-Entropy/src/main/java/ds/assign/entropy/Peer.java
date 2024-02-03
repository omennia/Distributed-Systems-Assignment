package ds.assign.entropy;

import java.lang.Thread;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.*;
import ds.assign.grpc.*;
import io.grpc.*;
import ds.assign.util.PoissonProcess;

import java.nio.file.Files;
import java.io.*;

public class Peer {
  static String host;
  static ConcurrentSkipListSet<String> event_queue;
  static Set<Integer> my_peers;
  static int my_port;
  static List<String> lines = null;
  static String[] list_of_words;

  static volatile boolean token;

  public Peer(String hostname, Integer port) {
    host = hostname;
    token = false;
    my_port = port;
    my_peers = new HashSet<Integer>();

    Peer.event_queue = new ConcurrentSkipListSet<>();
    try {
      new Thread(new PeerServer(hostname, port)).start();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) throws Exception {
    /* We are going to use the properties config file to read */
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);
    String ip = properties.getProperty("ip_addr");

    int my_port = Integer.parseInt(properties.getProperty(args[0]));

    input.close();

    new Peer(ip, my_port);

    for (int i = 1; i < args.length; ++i) {
      Peer.my_peers.add(Integer.parseInt(properties.getProperty(args[i])));
    }
    System.out.printf("new peer @ host=%s\n", args[0]);
    try {
      lines = Files.readAllLines(new File("dictionary.txt").toPath());
      list_of_words = lines.toArray(new String[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

class PeerServer implements Runnable {
  private final String host;
  private final int port;
  private Server grpcServer;

  public PeerServer(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void run() {
    try {
      grpcServer = ServerBuilder.forPort(port)
          .addService(new Start())
          .addService(new Connection())
          .build().start();
      grpcServer.awaitTermination();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    if (grpcServer != null) {
      grpcServer.shutdown();
    }
  }
}

class Start extends StartServiceGrpc.StartServiceImplBase {
  @Override
  public void sendStartToken(StartToken can_start, io.grpc.stub.StreamObserver<StartToken> responseObserver) {
    try {
      new Thread(new PeerClient(Peer.host, Peer.my_port)).start();
      StartToken response = StartToken.newBuilder()
          .setStart(true)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class Connection extends TokenServiceGrpc.TokenServiceImplBase { // For receiving a connection
  @Override
  public void sendToken(TokenRequest request, io.grpc.stub.StreamObserver<TokenResponse> responseObserver) {

    System.out.println("\n====================================================\n");
    System.out.println("Received push-pull operation from peer " + request.getSourceIdentifier());

    // Extract the set of words from the request
    ConcurrentSkipListSet<String> receivedSet = new ConcurrentSkipListSet<>(request.getWordsList());

    // Prepare the merged set for the response
    TokenResponse response = TokenResponse.newBuilder()
        .addAllWords(Peer.event_queue)
        .setConfirmation("Token and words received!")
        .build();

    // Merge the received set with the current peer's set
    PeerHelper.mergeSets(receivedSet);

    PeerHelper.print_set_of_words();
    System.out.println("====================================================\n");

    // Send the response
    responseObserver.onNext(response);
    responseObserver.onCompleted();

    PeerHelper.make_sleep(5);
  }
}

class PeerClient implements Runnable {
  String host;
  Scanner scanner;
  Integer port;

  private TokenServiceGrpc.TokenServiceBlockingStub stub_token;
  private PushPullServiceGrpc.PushPullServiceBlockingStub stub_queue_send;

  public PeerClient(String host, int port)
      throws Exception {
    this.host = host;
    this.port = port;
    this.scanner = new Scanner(System.in);

    // Initialize the gRPC channel and stub
    // gRPC channel

    // Start generating operations to add to a queue
    new Thread(new EventGenerator()).start();
  }

  @Override
  public void run() {

    Random rng = new Random();

    double lambda = 5; // rate parameter
    PoissonProcess my_process = new PoissonProcess(lambda, rng);

    while (true) {
      try {
        double t = my_process.timeForNextEvent();
        Thread.sleep((long) (t * 60 * 1000));

        int next_con = PeerHelper.choose_random_peer(Peer.my_peers);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, next_con)
            .usePlaintext()
            .build();
        stub_token = TokenServiceGrpc.newBlockingStub(channel);
        stub_queue_send = PushPullServiceGrpc.newBlockingStub(channel);

        TokenRequest request = TokenRequest.newBuilder()
            .setSourceIdentifier(String.valueOf(this.port)) // Set the source
            .setMessage("Here is your token :-)")
            .addAllWords(Peer.event_queue)
            .build();

        System.out.println("\n====================================================");
        System.out.println("Started push-pull operation with peer " + String.valueOf(next_con));

        TokenResponse response = stub_token.sendToken(request);

        // Update the local set with the received words
        PeerHelper.mergeSets(new ConcurrentSkipListSet<>(response.getWordsList()));
        PeerHelper.print_set_of_words();
        System.out.println("====================================================\n");

        Peer.token = false;

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}