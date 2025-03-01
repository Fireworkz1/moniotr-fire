package group.fire_monitor.util;

import java.util.*;

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
    public static boolean hasValue(List<?> list) {
        return list != null&&!list.isEmpty();
    }
    public static String listToString(List<?> list){
        StringBuilder buffer=new StringBuilder();
        for(Object t:list){
            buffer.append(t);
            buffer.append(',');
        }
        return buffer.toString();
    }
    public static List<Integer> stringToList(String string){
        if(string==null) return new ArrayList<>();
        List<Integer> list = new ArrayList<>();

        // 按逗号分隔字符串
        String[] parts = string.split(",");

        // 遍历数组，将每个字符串转换为整数并添加到列表中
        for (String part : parts) {
           list.add(Integer.parseInt(part.trim())); // 使用 trim() 去除可能的空格
        }
        return list;
    }
    public static Boolean hasIntersection(List<?> list1, List<?> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return false;
        }

        Set<Object> set = new HashSet<>();
        for (Object obj : list1) {
            set.add(obj); // 将第一个列表的元素添加到 Set 中
        }

        for (Object obj : list2) {
            if (set.contains(obj)) { // 检查第二个列表的元素是否在 Set 中
                return true;
            }
        }
        return false; // 遍历完后没有发现交集
    }

    public static boolean needToWarn(String compareType,Double value,Double threshold){
        switch(compareType){
            case ">=":
                System.out.println(">=?");
                return value >= threshold;
            case "<=":
                return value <= threshold;
            case "==":
                return Objects.equals(value, threshold);
            default:
                throw new RuntimeException("目前只支持>=,<=,=三种判断条件");

        }
    }
}
