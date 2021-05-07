package sh.sidd.asmi;

import java.util.Arrays;

public class AsmiMain {

  public static void main(String[] args) {
    var cli = new AsmiCli(Arrays.asList(args));
    cli.run();
  }
}
