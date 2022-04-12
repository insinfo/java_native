package demo;

public class Utils {
 public static String reverseString(String str) {
        if (str.isEmpty())
            return str;
        return reverseString(str.substring(1)) + str.charAt(0);
    }
}