package br.com.victor.justcode.utilities;

// START GENET
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class F13Sender {

  public static void main(String[] args) throws Exception {
    boolean showExample = false;
    for (String arg : args) {
      if ("--example".equals(arg) || "-help".equals(arg)) {
        showExample = true;
      }
    }

    if (showExample) {
      showExample();
      return;
    }

    // Default stop time
    LocalTime stopTime = LocalTime.of(19, 0);

    // Parse command line for stop time
    for (int i = 0; i < args.length; i++) {
      if ("--stop-time".equals(args[i]) && i + 1 < args.length) {
        try {
          stopTime = LocalTime.parse(args[i + 1], DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
          System.err.println("Invalid time format. Use HH:mm (e.g. 18:30)");
          return;
        }
        i++; // skip next arg
      }
    }

    System.out
        .println("Script started at: " + LocalTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    System.out.println("Configured stop time: " + stopTime);

    Robot robot = new Robot();

    while (true) {
      LocalTime now = LocalTime.now();

      if (!now.isBefore(stopTime)) {
        System.out.println("Stop time reached at: "
            + LocalTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ", Exiting.");
        break;
      }

      System.out
          .println("Sending F13 at: " + LocalTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

      robot.keyPress(KeyEvent.VK_F13);
      robot.keyRelease(KeyEvent.VK_F13);

      Thread.sleep(5 * 60 * 1000); // 5 minutes in milliseconds
    }
  }

  private static void showExample() {
    System.out.println("""
            Usage: F13Sender [--stop-time HH:mm] [--show-example|-help]

            Options:
              --stop-time    Set the time to stop sending F13 key (24h format, e.g. 18:30).
              --show-example Show this help and example usage.
              -help          Alias for --show-example.

            Example:
              java F13Sender --stop-time 18:30
        """);
  }
}
