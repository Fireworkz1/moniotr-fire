package group.fire_monitor.util.dic;

import group.fire_monitor.pojo.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetricDictionary {
    public static List<MetricType> getMetricList() {
        List<MetricType> metricList = new ArrayList<>();

// 服务器指标
        metricList.add(new MetricType("server", "server_file_free_gb", "服务器磁盘剩余空间（GB）"));
        metricList.add(new MetricType("server", "server_load_1min", "服务器1分钟负载"));
        metricList.add(new MetricType("server", "server_cpu_usage_1min", "服务器1分钟CPU使用率"));
        metricList.add(new MetricType("server", "server_memory_usage_1min", "服务器1分钟内存使用率"));
        metricList.add(new MetricType("server", "server_cpu_context_switches_5m", "服务器5分钟CPU上下文切换次数"));
        metricList.add(new MetricType("server", "server_disk_input_rate_5m", "服务器5分钟磁盘输入速率"));
        metricList.add(new MetricType("server", "server_disk_output_rate_5m", "服务器5分钟磁盘输出速率"));
        metricList.add(new MetricType("server", "server_processes_running", "服务器运行中的进程数"));
        metricList.add(new MetricType("server", "server_threads_running", "服务器运行中的线程数"));

// 软件指标
        metricList.add(new MetricType("software", "software_jvm_nonheap_memory_usage", "JVM非堆内存使用情况"));
        metricList.add(new MetricType("software", "software_jvm_heap_memory_usage", "JVM堆内存使用情况"));
        metricList.add(new MetricType("software", "software_jvm_gc_times_5min", "JVM 5分钟内垃圾回收次数"));
        metricList.add(new MetricType("software", "software_jvm_threads_number", "JVM当前线程数"));

        return metricList;
    }
    public static String getDescriptionByTarget(String target){
        if (target.startsWith("target=")) {
            target = target.substring(7); // 从第 8 个字符开始截取（"target=" 的长度为 7）
        }
        List<MetricType> metrics=getMetricList();
        for(MetricType item:metrics){
            if(Objects.equals(item.getTarget(), target)){
                return item.getDescription();
            }
        }return "暂无描述";
    }
}
