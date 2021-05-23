package sedonac.util;

public class AsciiColor {
    private static final String ESC = "\033";
    // Reset
    public static final String RESET = ESC + "[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = ESC + "[0;30m";   // BLACK
    public static final String RED = ESC + "[0;31m";     // RED
    public static final String GREEN = ESC + "[0;32m";   // GREEN
    public static final String YELLOW = ESC + "[0;33m";  // YELLOW
    public static final String BLUE = ESC + "[0;34m";    // BLUE
    public static final String PURPLE = ESC + "[0;35m";  // PURPLE
    public static final String CYAN = ESC + "[0;36m";    // CYAN
    public static final String WHITE = ESC + "[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = ESC + "[1;30m";  // BLACK
    public static final String RED_BOLD = ESC + "[1;31m";    // RED
    public static final String GREEN_BOLD = ESC + "[1;32m";  // GREEN
    public static final String YELLOW_BOLD = ESC + "[1;33m"; // YELLOW
    public static final String BLUE_BOLD = ESC + "[1;34m";   // BLUE
    public static final String PURPLE_BOLD = ESC + "[1;35m"; // PURPLE
    public static final String CYAN_BOLD = ESC + "[1;36m";   // CYAN
    public static final String WHITE_BOLD = ESC + "[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = ESC + "[4;30m";  // BLACK
    public static final String RED_UNDERLINED = ESC + "[4;31m";    // RED
    public static final String GREEN_UNDERLINED = ESC + "[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = ESC + "[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = ESC + "[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = ESC + "[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = ESC + "[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = ESC + "[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = ESC + "[40m";  // BLACK
    public static final String RED_BACKGROUND = ESC + "[41m";    // RED
    public static final String GREEN_BACKGROUND = ESC + "[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = ESC + "[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = ESC + "[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = ESC + "[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = ESC + "[46m";   // CYAN
    public static final String WHITE_BACKGROUND = ESC + "[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = ESC + "[0;90m";  // BLACK
    public static final String RED_BRIGHT = ESC + "[0;91m";    // RED
    public static final String GREEN_BRIGHT = ESC + "[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = ESC + "[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = ESC + "[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = ESC + "[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = ESC + "[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = ESC + "[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = ESC + "[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = ESC + "[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = ESC + "[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = ESC + "[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = ESC + "[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = ESC + "[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = ESC + "[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = ESC + "[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = ESC + "[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = ESC + "[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = ESC + "[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = ESC + "[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = ESC + "[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = ESC + "[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = ESC + "[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = ESC + "[0;107m";   // WHITE


}
