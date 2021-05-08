package sh.sidd.asmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import sh.sidd.asmi.antlr.AsmiLexer;
import sh.sidd.asmi.antlr.AsmiParser;

/** CLI for the Asmi tool. */
@Slf4j
@SuppressWarnings({"PMD.SystemPrintln", "PMD.DoNotTerminateVM"})
public class AsmiCli {
  private final List<String> args;

  public AsmiCli(List<String> args) {
    this.args = List.copyOf(args);
  }

  /** Runs the CLI tool. */
  public void run() {
    if (args.size() > 1) {
      System.out.println("Usage: asmi [script]");
      System.exit(1);
    } else if (args.size() == 1) {
      runFile(args.get(0));
    } else {
      runPrompt();
    }
  }

  /**
   * Runs a *.asmi file.
   *
   * @param path The path to the file to run.
   */
  private void runFile(String path) {
    try {
      run(Files.readString(Paths.get(path)));
    } catch (IOException ex) {
      log.error("Failed to run file: " + path, ex);
      System.exit(1);
    }
  }

  /** Runs an interactive Asmi REPL. */
  private void runPrompt() {
    var reader = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      try {
        System.out.print("> ");
        var line = reader.readLine();

        if (line == null) {
          break;
        }

        run(line);
      } catch (IOException ex) {
        break;
      }
    }
  }

  /**
   * Runs the given Asmi source code.
   *
   * @param source The source code to run.
   */
  private void run(String source) {
    var chars = CharStreams.fromString(source);
    var lexer = new AsmiLexer(chars);
    var tokens = new CommonTokenStream(lexer);
    var parser = new AsmiParser(tokens);

    parser.setBuildParseTree(true);

    var tree = parser.expression();

    System.out.println(tree.equality().toStringTree(parser));

    //    var errorHandler = new ErrorHandler();
    //    var scanner = new Scanner(source, errorHandler);
    //    var tokens = scanner.scanTokens();
    //    var parser = new Parser(errorHandler, tokens);
    //
    //    System.out.println(parser.parse());
  }
}
