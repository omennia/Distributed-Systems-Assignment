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
 * starting the cycle by giving the token
 * to the selected peer 
 * Apenas um peer com uma única responsabilidade
 */
public class Injector {

  private String host;
  private int port;
  private ManagedChannel channel;

  public Injector(String host, int port) {
    this.host = host;
    this.port = port;
    this.channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
  }

  public void sendToken() {
    TokenServiceGrpc.TokenServiceBlockingStub stub = TokenServiceGrpc.newBlockingStub(channel);

    TokenRequest request = TokenRequest.newBuilder()
        .setMessage("First token by injection. Starting the process..")
        .build();

    TokenResponse response = stub.sendToken(request);
    System.out.println("Response from server: " + response.getConfirmation());

    channel.shutdown();
  }

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);

    String ip_addr = properties.getProperty("ip_addr");
    int inject_on = Integer.parseInt(properties.getProperty("inject_on"));

    Injector injector = new Injector(ip_addr, inject_on);
    injector.sendToken();
    input.close();
  }
}