package ds.assign.ring;

import ds.assign.calculator.CalculatorClient;

public class PeerHelper {

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

  /**
   * Performs the specified operation using the given calculator client.
   */
  public static void perform_operation(CalculatorClient calculator, Operation cur_op) {
    switch (cur_op.op) {
      case "add":
        System.out.println("Result of " + cur_op.left + "+" + cur_op.right + " = "
            + calculator.add(cur_op.left, cur_op.right));
        break;
      case "sub":
        System.out.println("Result of " + cur_op.left + "-" + cur_op.right + " = "
            + calculator.sub(cur_op.left, cur_op.right));
        break;
      case "div":
        System.out.println("Result of " + cur_op.left + "/" + cur_op.right + " = "
            + calculator.div(cur_op.left, cur_op.right));
        break;
      case "mult":
        System.out.println("Result of " + cur_op.left + "*" + cur_op.right + " = "
            + calculator.mult(cur_op.left, cur_op.right));
        break;
    }
  }
}
