package ds.assign.entropy;
import ds.assign.util.PoissonProcess;

import java.util.*;

class EventGenerator implements Runnable {
  private Random rng = new Random();

  @Override
  public void run() {
    double lambda = 6; // 6 events per minute or (1 event per 10 seconds)
    PoissonProcess my_process = new PoissonProcess(lambda, rng);

    while (true) {
      try {
        double t = my_process.timeForNextEvent();
        Thread.sleep((long) (t * 60 * 1000));
        Peer.event_queue.add(PeerHelper.getRandomWord());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}