package sh.sidd.asmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

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
    try (var reader = new BufferedReader(new InputStreamReader(System.in))) {

      while (true) {
        System.out.print("> ");
        final var line = reader.readLine();

        if (line == null) {
          break;
        }

        run(line);
      }
    } catch (IOException ignored) {
      // Ignore and exit normally
    }
  }

  /**
   * Runs the given Asmi source code.
   *
   * @param source The source code to run.
   */
  private void run(String source) {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast);

    compiler.compile();
    compiler.run();

    System.out.println();
  }
}
