package sh.sidd.asmi;

import java.util.Arrays;

public final class AsmiMain {

  private AsmiMain() {}

  public static void main(String[] args) {
    final var cli = new AsmiCli(Arrays.asList(args));
    cli.run();
  }
}
