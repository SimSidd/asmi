package sh.sidd.asmi.compiler;

import java.util.HashMap;
import java.util.Map;
import sh.sidd.asmi.data.ValueType;

/** Stores metadata to currently accessible variables. */
public class VariableEnv {

  private record VariableEntry(ValueType valueType, int index) {}

  private final VariableEnv enclosingEnv;
  private final Map<String, VariableEntry> variables;
  private int currentLocalVariableSize;

  public VariableEnv() {
    this(null);
  }

  public VariableEnv(VariableEnv enclosingEnv) {
    this.enclosingEnv = enclosingEnv;
    this.variables = new HashMap<>();
  }

  /**
   * Returns the type for a given identifier.
   *
   * @param identifier The identifier to look up.
   * @return The type of the identifier.
   */
  public ValueType getVariableType(String identifier) throws VariableEnvException {
    if (!variables.containsKey(identifier)) {
      throw new VariableEnvException("Unknown variable: " + identifier);
    }

    return variables.get(identifier).valueType();
  }

  /**
   * Returns the index for a given identifier.
   *
   * @param identifier The identifier to look up.
   * @return The index of the identifier.
   */
  public int getVariableIndex(String identifier) throws VariableEnvException {
    if (variables.containsKey(identifier)) {
      return variables.get(identifier).index();
    }

    if (enclosingEnv != null) {
      return enclosingEnv.getVariableIndex(identifier);
    }

    throw new VariableEnvException("Unknown variable: " + identifier);
  }

  /**
   * Defines a new variable and sets its type.
   *
   * @param identifier The identifier to set.
   * @param valueType The type to set.
   */
  public void defineVariable(String identifier, ValueType valueType) throws VariableEnvException {
    if (variables.containsKey(identifier)) {
      throw new VariableEnvException("Identifier already exists.");
    }

    variables.put(identifier, new VariableEntry(valueType, currentLocalVariableSize++));
  }
}
