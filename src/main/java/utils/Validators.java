package utils;

public class Validators {

    /**
     * Detects whether a string is a number or not
     * @param strNum the string given
     * @return true if number, false if not
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
