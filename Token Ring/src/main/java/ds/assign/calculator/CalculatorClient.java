package ds.assign.calculator;

import ds.assign.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {
  private final CalculatorServiceGrpc.CalculatorServiceBlockingStub stub;
  private final ManagedChannel channel;

  public CalculatorClient(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
    this.stub = CalculatorServiceGrpc.newBlockingStub(channel);
  }

  public CalculationRequest getRequest(double val1, double val2) {
    return CalculationRequest.newBuilder()
        .setValue1(val1)
        .setValue2(val2)
        .build();
  }

  public double add(double val1, double val2) {
    return stub.add(getRequest(val1, val2)).getResult();
  }

  public double sub(double val1, double val2) {
    return stub.sub(getRequest(val1, val2)).getResult();
  }

  public double mult(double val1, double val2) {
    return stub.mult(getRequest(val1, val2)).getResult();
  }

  public double div(double val1, double val2) {
    return stub.div(getRequest(val1, val2)).getResult();
  }

  public void shutdown() {
    if (this.channel != null) {
      this.channel.shutdown();
    }
  }
}
