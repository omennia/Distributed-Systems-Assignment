package ds.assign.entropy;

import java.util.concurrent.*;

import java.util.Random;
import java.util.Set;

public class PeerHelper {

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  private static final Random random = new Random();

  public static String getRandomWord() {
    Random rand = new Random();
    // String line = Peer.lines.get(rand.nextInt(Peer.lines.size()));
    // String[] words = line.split(" ");
    // String chosen_word = words[rand.nextInt(words.length)];
    // while(chosen_word.length() == 0){
    // chosen_word = words[rand.nextInt(words.length)];
    // }
    // String[] chosen_line = Peer.wordsX[rand.nextInt(Peer.wordsX.length)].split(" ");
    // String chosen_word = chosen_line[rand.nextInt(chosen_line.length)];
    // while (chosen_word.length() == 0) {
    //   chosen_word = chosen_line[rand.nextInt(chosen_line.length)];
    // }
    String chosen_word = Peer.list_of_words[rand.nextInt(Peer.list_of_words.length)];
    System.out.println("Chose: " + chosen_word);
    return chosen_word;
  }

  // Method to generate a random word of length up to 5 characters
  public static String generateRandomWord() {
    int wordLength = random.nextInt(5) + 1; // Length from 1 to 5
    StringBuilder word = new StringBuilder(wordLength);

    for (int i = 0; i < wordLength; i++) {
      int index = random.nextInt(ALPHABET.length());
      char randomChar = ALPHABET.charAt(index);
      word.append(randomChar);
    }

    System.out.println("Generated: " + word.toString());
    return word.toString();
  }

  /**
   * Sleeps for the specified number of seconds.
   *
   * @param seconds number of seconds to sleep.
   */
  public static void make_sleep(int seconds) {
    try {
      Thread.sleep(1000L * seconds);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static int choose_random_peer(Set<Integer> my_peers) {
    Random rand = new Random();
    int size = my_peers.size();
    int pick = rand.nextInt(size);

    int i = 0;
    for (Integer con : my_peers) {
      if (i == pick)
        return con;
      i++;
    }
    return -1; // Error
  }

  public static void mergeSets(ConcurrentSkipListSet<String> otherPeerSet) {
    Peer.event_queue.addAll(otherPeerSet);
  }

  public static void print_set_of_words() {
    System.out.println("Current set of words:");
    for (String s : Peer.event_queue) {
      System.out.print(s + " ");
    }
    System.out.println();
  }
}
