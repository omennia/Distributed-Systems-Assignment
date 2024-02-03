package ds.assign.chat;

import java.util.Random;
import java.util.Set;

public class PeerHelper {

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  private static final Random random = new Random();

  public static String getRandomWord() {
    Random rand = new Random();
    return Peer.list_of_words[rand.nextInt(Peer.list_of_words.length)];
  }


  // Method to generate a random word of length up to 5 characters (( Já não usamos nenhum dos métodos abaixo ))
  public static String generateRandomWord() {
    int wordLength = random.nextInt(10) + 1; // Length from 1 to 10
    StringBuilder word = new StringBuilder(wordLength);

    for (int i = 0; i < wordLength; i++) {
      int index = random.nextInt(ALPHABET.length());
      char randomChar = ALPHABET.charAt(index);
      word.append(randomChar);
    }
    return word.toString();
  }

  /**
   * Sleeps for the specified number of seconds.
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
}
