package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 *
 * <p>Note: You can add to this class, but you may not alter signature of the existing methods.
 */
public class ChessPosition {

  private final int row;
  private final int col;

  public ChessPosition(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public static ChessPosition fromString(String positionString) {
    if (positionString == null || positionString.length() != 2) {
      throw new IllegalArgumentException(
          "Position string must be exactly 2 characters, e.g., 'e4'.");
    }

    positionString = positionString.toLowerCase();

    char columnChar = positionString.charAt(0);
    char rowChar = positionString.charAt(1);

    if (columnChar < 'a' || columnChar > 'h' || rowChar < '1' || rowChar > '8') {
      throw new IllegalArgumentException("Invalid position string: must be in range 'a1' to 'h8'.");
    }

    int col = columnChar - 'a' + 1;
    int row = rowChar - '0';

    return new ChessPosition(row, col);
  }

  /**
   * @return which row this position is in 1 codes for the bottom row
   */
  public int getRow() {
    return row;
  }

  /**
   * @return which column this position is in 1 codes for the left row
   */
  public int getColumn() {
    return col;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChessPosition that = (ChessPosition) o;
    return row == that.row && col == that.col;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, col);
  }

  @Override
  public String toString() {
    char columnChar = (char) ('a' + (col - 1));
    return "" + columnChar + row;
  }
}
