package ds.assign.chat;
import java.util.*;


class Message implements Comparable<Message> {
  String message;
  Integer timestamp;
  Integer peer_id;

  Message(String msg, Integer timestamp, Integer peer_id) {
    this.message = msg;
    this.timestamp = timestamp;
    this.peer_id = peer_id;
  }

  @Override
  public int compareTo(Message other) {
    int timestampComparison = this.timestamp.compareTo(other.timestamp);
    if (timestampComparison == 0) {
      return this.peer_id.compareTo(other.peer_id);
    }
    return timestampComparison;
  }

}

class MessageKey implements Comparable<MessageKey> {
  private final Integer timestamp;
  private final Integer peer_id;

  MessageKey(Integer timestamp, Integer peer_id) {
    this.timestamp = timestamp;
    this.peer_id = peer_id;
  }

  @Override
  public int compareTo(MessageKey other) {
    int timestampComparison = this.timestamp.compareTo(other.timestamp);
    if (timestampComparison == 0) {
      return this.peer_id.compareTo(other.peer_id);
    }
    return timestampComparison;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MessageKey that = (MessageKey) o;
    return Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(peer_id, that.peer_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, peer_id);
  }
}