package ds.assign.calculator;

import ds.assign.grpc.*;
import ds.assign.grpc.CalculationRequest;
import ds.assign.grpc.CalculationResponse;
import java.io.FileInputStream;
import java.util.Properties;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class CalculatorServer {

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    properties.load(input);

    String server_ip = properties.getProperty("server_runs_on_ip");
    int server_port = Integer.parseInt(properties.getProperty("server_runs_on_port"));

    input.close();

    Server server = ServerBuilder.forPort(server_port)
        .addService(new CalculatorServiceImpl())
        .build();

    server.start();
    System.out.println("Server started at " + server.getPort());
    server.awaitTermination();
  }

  static class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void add(CalculationRequest req, StreamObserver<CalculationResponse> responseObserver) {
      double result = req.getValue1() + req.getValue2();
      System.out.println("Calculating " + req.getValue1() + " + " + req.getValue2() + " = " + result);
      CalculationResponse response = CalculationResponse.newBuilder().setResult(result).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    @Override
    public void sub(CalculationRequest req, StreamObserver<CalculationResponse> responseObserver) {
      double result = req.getValue1() - req.getValue2();
      System.out.println("Calculating " + req.getValue1() + " - " + req.getValue2() + " = " + result);
      CalculationResponse response = CalculationResponse.newBuilder().setResult(result).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    @Override
    public void mult(CalculationRequest req, StreamObserver<CalculationResponse> responseObserver) {
      double result = req.getValue1() * req.getValue2();
      System.out.println("Calculating " + req.getValue1() + " * " + req.getValue2() + " = " + result);
      CalculationResponse response = CalculationResponse.newBuilder().setResult(result).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    @Override
    public void div(CalculationRequest req, StreamObserver<CalculationResponse> responseObserver) {
      double result = req.getValue1() / req.getValue2();
      System.out.println("Calculating " + req.getValue1() + " / " + req.getValue2() + " = " + result);
      CalculationResponse response = CalculationResponse.newBuilder().setResult(result).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }
}
