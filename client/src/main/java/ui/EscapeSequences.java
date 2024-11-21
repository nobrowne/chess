package ui;

/**
 * This class contains constants and functions relating to ANSI Escape Sequences that are useful in
 * the Client display
 */
public class EscapeSequences {
  // Escape codes
  private static final String UNICODE_ESCAPE = "\u001b";
  // Erasing stuff
  public static final String ERASE_SCREEN = UNICODE_ESCAPE + "[H" + UNICODE_ESCAPE + "[2J";
  public static final String ERASE_LINE = UNICODE_ESCAPE + "[2K";
  // Text formatting
  public static final String SET_TEXT_BOLD = UNICODE_ESCAPE + "[1m";
  public static final String SET_TEXT_FAINT = UNICODE_ESCAPE + "[2m";
  public static final String RESET_TEXT_BOLD_FAINT = UNICODE_ESCAPE + "[22m";
  public static final String SET_TEXT_ITALIC = UNICODE_ESCAPE + "[3m";
  public static final String RESET_TEXT_ITALIC = UNICODE_ESCAPE + "[23m";
  public static final String SET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[4m";
  public static final String RESET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[24m";
  public static final String SET_TEXT_BLINKING = UNICODE_ESCAPE + "[5m";
  public static final String RESET_TEXT_BLINKING = UNICODE_ESCAPE + "[25m";
  public static final String RESET_TEXT_COLOR = UNICODE_ESCAPE + "[39m";
  public static final String RESET_BG_COLOR = UNICODE_ESCAPE + "[49m";

  // RGB text escape code
  private static final String RGB_TEXT_COLOR = UNICODE_ESCAPE + "[38;2;";

  // RGB text
  public static final String SET_TEXT_COLOR_PURPLE = RGB_TEXT_COLOR + "199;119;224m";
  public static final String SET_TEXT_COLOR_YELLOW = RGB_TEXT_COLOR + "222;193;119m";
  public static final String SET_TEXT_COLOR_ORANGE = RGB_TEXT_COLOR + "202;154;97m";
  public static final String SET_TEXT_COLOR_GREEN = RGB_TEXT_COLOR + "151;196;116m";
  public static final String SET_TEXT_COLOR_BLUE = RGB_TEXT_COLOR + "112;162;225m";
  public static final String SET_TEXT_COLOR_DARK_BLUE = RGB_TEXT_COLOR + "41;44;52m";
  public static final String SET_TEXT_COLOR_WHITE = RGB_TEXT_COLOR + "255;255;255m";
  public static final String SET_TEXT_COLOR_RED = RGB_TEXT_COLOR + "216;106;116m";

  // RGB background escape code
  private static final String RGB_BG_COLOR = UNICODE_ESCAPE + "[48;2;";

  // RGB background
  public static final String SET_BG_COLOR_PURPLE = RGB_BG_COLOR + "199;119;224m";
  public static final String SET_BG_COLOR_YELLOW = RGB_BG_COLOR + "222;193;119m";
  public static final String SET_BG_COLOR_ORANGE = RGB_BG_COLOR + "202;154;97m";
  public static final String SET_BG_COLOR_GREEN = RGB_BG_COLOR + "151;196;116m";
  public static final String SET_BG_COLOR_BLUE = RGB_BG_COLOR + "112;162;225m";
  public static final String SET_BG_COLOR_DARK_BLUE = RGB_BG_COLOR + "41;44;52m";
  public static final String SET_BG_COLOR_WHITE = RGB_BG_COLOR + "255;255;255m";
  public static final String SET_BG_COLOR_RED = RGB_BG_COLOR + "216;106;116m";
  public static final String SET_BG_COLOR_GRAY = RGB_BG_COLOR + "68;69;74m";
  public static final String SET_BG_COLOR_LIGHT_GRAY = RGB_BG_COLOR + "153;159;174m";

  public static String moveCursorToLocation(int x, int y) {
    return UNICODE_ESCAPE + "[" + y + ";" + x + "H";
  }
}
