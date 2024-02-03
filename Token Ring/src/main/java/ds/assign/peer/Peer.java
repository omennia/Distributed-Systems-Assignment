package ds.assign.ring;

import java.lang.Thread;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import ds.assign.grpc.*;
import io.grpc.*;
import ds.assign.calculator.CalculatorClient;
import java.util.Scanner;
import java.util.logging.Logger;

public class Peer {
  Logger logger;
  String host;
  Integer target;
  static String server_ip;
  static int server_port;
  static volatile boolean token;
  static volatile int itr;
  static ConcurrentLinkedQueue<Operation> event_queue;
  static Semaphore tokenSemaphore;

  public Peer(String hostname, Integer port, Integer target, String server_ip, int server_port) {
    host = hostname;
    this.target = target;
    itr = 0;
    token = false;
    Peer.server_ip = server_ip;
    Peer.server_port = server_port;
    logger = Logger.getLogger("logfile");
    event_queue = new ConcurrentLinkedQueue<>();
    tokenSemaphore = new Semaphore(0); // Avoids the busy waiting
    try {
      new Thread(new PeerServer(hostname, port, logger)).start();
      new Thread(new PeerClient(hostname, port, target, server_ip, server_port, logger)).start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    /* We are going to use the properties config file to read */
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);
    int num_machines = Integer.parseInt(properties.getProperty("num_machines"));
    int base_port = Integer.parseInt(properties.getProperty("base_port"));
    
    String server_ip = properties.getProperty("server_runs_on_ip");
    int server_port = Integer.parseInt(properties.getProperty("server_runs_on_port"));
    String my_ip_addr = properties.getProperty("ip_addr");

    input.close();

    Integer machine_name = Integer.parseInt(args[0].substring(1)) - 1;
    Integer my_port = machine_name + base_port;
    Integer target = ((machine_name + 1) % num_machines) + base_port;

    new Peer(my_ip_addr, my_port, target, server_ip, server_port);
    System.out.printf("new peer @ host=%s\n", my_ip_addr);

  }
}

class PeerServer implements Runnable {
  private final String host;
  private final int port;
  private Server grpcServer;
  private final Logger logger;

  public PeerServer(String host, int port, Logger logger) {
    this.host = host;
    this.port = port;
    this.logger = logger;
  }

  @Override
  public void run() {
    try {
      grpcServer = ServerBuilder.forPort(port).addService(new Connection()).build().start();
      logger.info("Server started at " + port);
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

class Connection extends TokenServiceGrpc.TokenServiceImplBase { // Substitui a thread connection
  @Override
  public void sendToken(TokenRequest request, io.grpc.stub.StreamObserver<TokenResponse> responseObserver) {
    Peer.token = true;
    Peer.tokenSemaphore.release(); // The token is available

    String receivedMessage = request.getMessage();
    System.out.println("Received: " + receivedMessage);

    TokenResponse response = TokenResponse.newBuilder()
        .setConfirmation("Token received!")
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}


class PeerClient implements Runnable {
  String server_ip;
  int server_port;
  String host;
  Logger logger;
  Scanner scanner;
  Integer port;
  Integer target;

  private TokenServiceGrpc.TokenServiceBlockingStub stub;

  public PeerClient(String host, int port, Integer target, String server_ip, int server_port, Logger logger)
      throws Exception {
    this.server_ip = server_ip;
    this.server_port = server_port;
    this.host = host;
    this.logger = logger;
    this.port = port;
    this.target = target;
    this.scanner = new Scanner(System.in);

    // Initialize the gRPC channel and stub
    // gRPC channel
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, target)
            .usePlaintext()
            .build();
    stub = TokenServiceGrpc.newBlockingStub(channel);

    // Start generating operations to add to a queue
    new Thread(new EventGenerator()).start();
  }

  @Override
  public void run() {
    while (true) {
      try {
        Peer.tokenSemaphore.acquire(); // Vamos usar o token
        CalculatorClient calculator = new CalculatorClient(Peer.server_ip, Peer.server_port);

        while (!Peer.event_queue.isEmpty()) {
          PeerHelper.perform_operation(calculator, Peer.event_queue.poll());
        }
        calculator.shutdown();

        PeerHelper.make_sleep(1); // Sleep for one second in order not to run too fast

        // Send token
        TokenRequest request = TokenRequest.newBuilder()
            .setMessage("Here is your token :-)")
            .build();

        TokenResponse response = stub.sendToken(request);
        // System.out.println("Response from target peer: " + response.getConfirmation());
        Peer.token = false;

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}