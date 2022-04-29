package demo;

public class Utils {
    public static String reverseString(String str) {
        if (str.isEmpty())
            return str;
        return reverseString(str.substring(1)) + str.charAt(0);
    }

    public static String getGroupFromLongname(String val) {
        if(val == null){
            return "";
        }
        if(!val.contains(" ")){
            return "";
        }

        var result = "";
        String afterVal = val.trim().replaceAll(" +", " ");
   
        var items = afterVal.split(" ");
        if(items.length > 4){
            result = items[3];
        }

        return result;
    }

    public static String getUserFromLongname(String val) {
        if(val == null){
            return "";
        }
        if(!val.contains(" ")){
            return "";
        }

        var result = "";
        String afterVal = val.trim().replaceAll(" +", " ");
   
        var items = afterVal.split(" ");
        if(items.length > 4){
            result = items[2];
        }

        return result;
    }
}