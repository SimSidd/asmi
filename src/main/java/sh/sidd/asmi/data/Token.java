package sh.sidd.asmi.data;

/**
 * A single token of the Asmi language.
 */
public record Token(TokenType tokenType, String lexeme,
                    Object literal, int line) {}
