package ds.assign.ring;
import ds.assign.util.PoissonProcess;

import java.util.*;

class Operation {
  public String op;
  public double left;
  public double right;

  public Operation(String op, double left, double right) {
    this.op = op;
    this.left = left;
    this.right = right;
  }
}

class EventGenerator implements Runnable {
  static String[] operations = { "add", "sub", "mult", "div" };
  private Random rng = new Random();

  @Override
  public void run() {
    double lambda = 4; // 4 events every minute
    PoissonProcess my_process = new PoissonProcess(lambda, rng);

    while (true) {
      try {
        double t = my_process.timeForNextEvent();
        Thread.sleep((long) (t * 60 * 1000));

        int op_num = rng.nextInt(4);
        double lo = rng.nextDouble() * 5.0;
        double hi = rng.nextDouble() * 5.0;

        Operation instance = new Operation(operations[op_num], lo, hi);
        Peer.event_queue.add(instance);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}