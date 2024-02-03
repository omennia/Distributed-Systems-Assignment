package ds.assign.util;

import java.io.PrintWriter;
import java.net.Socket;
import java.io.FileInputStream;
import java.util.Properties;
import ds.assign.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/* 
 * This class serves as an injector, 
 * sending a starting token to each peer
 * Apenas um peer com uma única responsabilidade
 */
public class Injector {

  public static void sendToken(String host, int port) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    StartServiceGrpc.StartServiceBlockingStub inj_stub = StartServiceGrpc.newBlockingStub(channel);

    StartToken start_msg = StartToken.newBuilder()
        .setStart(true)
        .build();

    inj_stub.sendStartToken(start_msg);
    channel.shutdown();
  }

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);

    String ip_addr = properties.getProperty("ip_addr");
    int base_port = Integer.parseInt(properties.getProperty("base_port"));
    int number_of_peers = Integer.parseInt(properties.getProperty("number_of_peers"));

    System.out.println("Starting injection on " + number_of_peers + " peers.");
    for (int i = 1; i <= number_of_peers; ++i) {
      int inject_on = base_port + i;
      sendToken(ip_addr, inject_on);
    }
    input.close();
  }
}