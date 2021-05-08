package sh.sidd.asmi.data;

/** The type of either a literal or an expression. */
public enum ValueType {
  SHORT,
  INT,
  LONG,
  FLOAT,
  DOUBLE,
  STRING,

  UNKNOWN;

  /**
   * Extracts the type from a given literal value.
   *
   * @param value The value to check.
   * @return The type of the value.
   */
  public static ValueType fromLiteral(Object value) {
    if (value instanceof Short) {
      return SHORT;
    } else if (value instanceof Integer) {
      return INT;
    } else if (value instanceof Long) {
      return LONG;
    } else if (value instanceof Float) {
      return FLOAT;
    } else if (value instanceof Double) {
      return DOUBLE;
    } else if (value instanceof String) {
      return STRING;
    }

    return UNKNOWN;
  }

  /** Checks whether this type is a number. */
  public boolean isNumeric() {
    return this == SHORT || this == INT || this == LONG || this == FLOAT || this == DOUBLE;
  }

  /** Checks whether this type is a floating point number. */
  public boolean isFloating() {
    return this == FLOAT || this == DOUBLE;
  }

  /** Attempts to find the correct resulting type for implicitly cast operations. */
  public static ValueType findImplicitCastType(ValueType typeOne, ValueType typeTwo) {
    if(typeOne == typeTwo) {
      return typeOne;
    }

    if (typeOne.isFloating() || typeTwo.isFloating()) {
      if (typeOne == ValueType.DOUBLE || typeTwo == ValueType.DOUBLE) {
        return DOUBLE;
      } else {
        return FLOAT;
      }
    }

    if(typeOne == LONG || typeTwo == LONG) {
      return LONG;
    }

    if(typeOne == INT || typeTwo == INT) {
      return INT;
    }

    if(typeOne == SHORT || typeTwo == SHORT) {
      return INT;
    }

    return UNKNOWN;
  }

  /**
   * Converts this value type to a bytecode descriptor.
   */
  public String toDescriptor() {
    return switch (this) {
      case SHORT -> "S";
      case INT -> "I";
      case LONG -> "L";
      case FLOAT -> "F";
      case DOUBLE -> "D";
      case STRING -> "Ljava/lang/String;";
      default -> null;
    };
  }
}
