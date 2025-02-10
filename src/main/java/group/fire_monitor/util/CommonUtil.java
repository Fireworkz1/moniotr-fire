package group.fire_monitor.util;

public class CommonUtil {
    public static boolean hasValue(Object obj) {
        return obj != null;
    }
    public static boolean hasValue(String str) {
        return str != null && !str.isEmpty();
    }
    public static boolean hasValue(Integer num) {
        return num != null;
    }
}
