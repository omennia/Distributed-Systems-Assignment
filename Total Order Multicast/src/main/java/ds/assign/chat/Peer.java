package ds.assign.chat;

import java.lang.Thread;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.*;
import ds.assign.grpc.*;
import io.grpc.*;
import java.util.logging.Logger;
import ds.assign.util.PoissonProcess;
//import ds.assign.chat.Message;
//import ds.assign.chat.MessageKey;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

public class Peer {
  Logger logger;
  static String host;
  static ConcurrentSkipListSet<Message> LamportPQ;
  static ConcurrentSkipListMap<MessageKey, Message> messageMap;
  static ConcurrentHashMap<MessageKey, Integer> ackCountMap;
  static Set<Integer> setOfPeers;
  static int my_port;
  static List<String> lines = null;
  static String[] list_of_words;
  static String myIp;
  static AtomicInteger local_lamport_counter = new AtomicInteger(0);
  static volatile boolean token;

  public Peer(String hostname, Integer port) {
    Peer.host = hostname;
    token = false;
    my_port = port;
    setOfPeers = new HashSet<Integer>();

    logger = Logger.getLogger("logfile");
    try {
      new Thread(new PeerServer(hostname, port, logger)).start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void addMessageToQueue(Message message) {
    LamportPQ.add(message);
  }

  public static void incrementAckCount(Integer timestamp, Integer peer_id) {
    // synchronized (messageMap) {
    synchronized (Peer.LamportPQ) { // We synchronize to avoid being in busy waiting
      MessageKey key = new MessageKey(timestamp, peer_id);
      ackCountMap.merge(key, 1, Integer::sum); // adds one to the value
      if (ackCountMap.get(key) == (setOfPeers.size())) {
        Peer.LamportPQ.notify();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    /* We are going to use the properties config file to read */
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);
    String ip = properties.getProperty("ip_addr");
    Integer number_of_peers = Integer.parseInt(properties.getProperty("number_of_peers"));
    Integer base_port = Integer.parseInt(properties.getProperty("base_port"));
    Peer.myIp = ip;
    int my_port = Integer.parseInt(properties.getProperty(args[0]));

    input.close();

    Peer.LamportPQ = new ConcurrentSkipListSet<>();
    Peer.messageMap = new ConcurrentSkipListMap<>();
    Peer.ackCountMap = new ConcurrentHashMap<>();

    new Peer(ip, my_port);

    try {
      lines = Files.readAllLines(new File("dictionary.txt").toPath());
      list_of_words = lines.toArray(new String[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (int i = 1; i <= number_of_peers; ++i) {
      Peer.setOfPeers.add(base_port + i);
    }

    PriorityQueueProcessor processor = new PriorityQueueProcessor();
    new Thread(processor).start();

    System.out.printf("new peer @ host=%s\n", args[0]);

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
      grpcServer = ServerBuilder.forPort(port)
          .addService(new Connection()) // Connection service
          .addService(new Ack()) // Ack service
          .addService(new Start()) // Just to start running
          .build()
          .start();
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

class Start extends StartServiceGrpc.StartServiceImplBase {
  @Override
  public void sendStartToken(StartToken can_start, io.grpc.stub.StreamObserver<LamportReply> responseObserver) {
    try {
      new Thread(new PeerClient(Peer.host, Peer.my_port)).start();
      LamportReply response = LamportReply.newBuilder()
          .setConfirmation("Started sucessfully!")
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class Ack extends AckServiceGrpc.AckServiceImplBase {
  @Override
  public void sendAck(LamportMessage ack, io.grpc.stub.StreamObserver<LamportReply> responseObserver) {
    Integer timestamp = ack.getTimestamp();
    String word = ack.getWord();
    int peerId = Integer.parseInt(ack.getSourceIdentifier());

    Peer.local_lamport_counter.set(Math.max(ack.getTimestampForAcks(), Peer.local_lamport_counter.get()) + 1);

    Peer.incrementAckCount(timestamp, peerId);

    // Prepare the merged set for the response
    LamportReply response = LamportReply.newBuilder()
        .setConfirmation("Ack received!")
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}

class Connection extends TokenServiceGrpc.TokenServiceImplBase {
  private AckServiceGrpc.AckServiceBlockingStub stub_ack;

  public void broadcastAck(LamportMessage ack) {
    for (Integer peerPort : Peer.setOfPeers) {
      // if (!peerPort.equals(Peer.my_port)) { // Send acks to everyone but me
      // Create a new channel for each peer
      ManagedChannel channel = ManagedChannelBuilder.forAddress(Peer.myIp, peerPort)
          .usePlaintext()
          .build();

      AckServiceGrpc.AckServiceBlockingStub stub = AckServiceGrpc.newBlockingStub(channel);

      stub.sendAck(ack);
      channel.shutdown();
      // }
    }
  }

  @Override
  public void sendToken(LamportMessage request, io.grpc.stub.StreamObserver<LamportReply> responseObserver) {
    int timestamp = request.getTimestamp();
    String word = request.getWord();
    int source = Integer.parseInt(request.getSourceIdentifier());

    
    Peer.local_lamport_counter.set(Math.max(timestamp, Peer.local_lamport_counter.get()) + 1);

    Message curMessage = new Message(word /* + " timestamp: " + String.valueOf(Peer.local_lamport_counter.get() )*/, timestamp, source);
    Peer.addMessageToQueue(curMessage);
    
    // Prepare the merged set for the response
    LamportReply response = LamportReply.newBuilder()
        .setConfirmation("Token and words received!")
        .build();

    LamportMessage ack = LamportMessage.newBuilder()
        .setSourceIdentifier(request.getSourceIdentifier()) // Set the source
        .setTimestamp(timestamp)
        .setTimestampForAcks(Peer.local_lamport_counter.get())
        .setWord(String.valueOf(String.valueOf(Peer.my_port)) + " ACK")
        .build();

    broadcastAck(ack);

    // Send the response
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}

class PeerClient implements Runnable {
  String host;
  Logger logger;
  Scanner scanner;
  Integer port;

  private TokenServiceGrpc.TokenServiceBlockingStub stub_token;

  public PeerClient(String host, int port)
      throws Exception {
    this.host = host;
    this.port = port;
    this.scanner = new Scanner(System.in);
  }

  public void broadcastMessage(LamportMessage request) {
    for (Integer peerPort : Peer.setOfPeers) {
      ManagedChannel channel = ManagedChannelBuilder.forAddress(host, peerPort)
          .usePlaintext()
          .build();

      TokenServiceGrpc.TokenServiceBlockingStub stub = TokenServiceGrpc.newBlockingStub(channel);

      // Send the token
      stub.sendToken(request);
      channel.shutdown();
    }
  }

  @Override
  public void run() {
    // This thread only starts after the injector sends the start message
    Random rng = new Random();

    // Colocar a 60
    double lambda = 60000; // rate parameter - 60 events per minute or (1 event per second)
    PoissonProcess my_process = new PoissonProcess(lambda, rng);

    while (true) {
      try {
        double t = my_process.timeForNextEvent();
        Thread.sleep((long) (t * 60 * 1000));

        // Incrementing the Lamport clock
        int timestamp = Peer.local_lamport_counter.incrementAndGet();

        LamportMessage request = LamportMessage.newBuilder()
            .setSourceIdentifier(String.valueOf(Peer.my_port)) // Set the source
            .setTimestamp(timestamp)
            .setWord(String.valueOf(Peer.my_port) + PeerHelper.getRandomWord())
            .build();

        broadcastMessage(request);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}