package com.codetraininglab.catalog.application;

import java.util.Comparator;

/** Compares runtime version strings (e.g. Java 25/26, Python 3.12, Go 1.23). */
public final class RuntimeVersionOrder {

  private RuntimeVersionOrder() {}

  public static int compare(String left, String right) {
    if (left == null && right == null) {
      return 0;
    }
    if (left == null) {
      return -1;
    }
    if (right == null) {
      return 1;
    }
    String[] leftParts = left.split("[.\\-_+]");
    String[] rightParts = right.split("[.\\-_+]");
    int length = Math.max(leftParts.length, rightParts.length);
    for (int i = 0; i < length; i++) {
      String leftPart = i < leftParts.length ? leftParts[i] : "";
      String rightPart = i < rightParts.length ? rightParts[i] : "";
      int numeric = comparePart(leftPart, rightPart);
      if (numeric != 0) {
        return numeric;
      }
    }
    return left.compareTo(right);
  }

  public static Comparator<String> newestFirst() {
    return (left, right) -> compare(right, left);
  }

  private static int comparePart(String leftPart, String rightPart) {
    Integer leftNumber = parseUnsignedInt(leftPart);
    Integer rightNumber = parseUnsignedInt(rightPart);
    if (leftNumber != null && rightNumber != null) {
      return leftNumber.compareTo(rightNumber);
    }
    return leftPart.compareToIgnoreCase(rightPart);
  }

  private static Integer parseUnsignedInt(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    int index = 0;
    while (index < value.length() && Character.isDigit(value.charAt(index))) {
      index++;
    }
    if (index == 0) {
      return null;
    }
    try {
      return Integer.parseInt(value.substring(0, index));
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
