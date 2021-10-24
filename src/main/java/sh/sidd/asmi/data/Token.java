package sh.sidd.asmi.data;

/**
 * A single token of the Asmi language.
 *
 * See {@link sh.sidd.asmi.scanner.Scanner} on how tokens are generated.
 */
public record Token(TokenType tokenType, String lexeme,
                    Object literal, int line) {}
