package ds.assign.chat;

class PriorityQueueProcessor implements Runnable {
  public PriorityQueueProcessor() {
  }

  @Override
  public void run() {
    while (true) {
      synchronized (Peer.LamportPQ) {
        if (hasMessagesFromAllPeers()) {
          Message message = Peer.LamportPQ.pollFirst();
          if (message != null) {
            processMessage(message);
          }
        }

        try {
          Peer.LamportPQ.wait(2000); // Wait with notify or timeout
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private boolean hasMessagesFromAllPeers() {
    if (Peer.LamportPQ == null || Peer.LamportPQ.isEmpty()) {
      return false;
    }

    Message msg = Peer.LamportPQ.first();
    if (msg == null) {
      return false;
    }

    // Check if the first message in the queue has enough acknowledgments
    MessageKey curKey = new MessageKey(msg.timestamp, msg.peer_id);
    Integer ackCount = Peer.ackCountMap.get(curKey);

    // Check if the acknowledgment count meets the threshold
    return ackCount != null && ackCount >= getRequiredAckCount();
  }

  private int getRequiredAckCount() {
    return Peer.setOfPeers.size();
  }

  private void processMessage(Message message) {
    Peer.ackCountMap.remove(new MessageKey(message.timestamp, message.peer_id));
    System.out.println(message.message);
  }
}