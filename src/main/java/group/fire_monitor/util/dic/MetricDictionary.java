package group.fire_monitor.util.dic;

import group.fire_monitor.pojo.MetricType;

import java.util.ArrayList;
import java.util.List;

public class MetricDictionary {
    public static List<MetricType> getMetricList() {
        List<MetricType> metricList = new ArrayList<>();

        // 服务器指标
        metricList.add(new MetricType("server", "server_file_free_gb"));
        metricList.add(new MetricType("server", "server_load_1min"));
        metricList.add(new MetricType("server", "server_cpu_usage_1min"));
        metricList.add(new MetricType("server", "server_memory_usage_1min"));
        metricList.add(new MetricType("server", "server_cpu_context_switches_5m"));
        metricList.add(new MetricType("server", "server_disk_input_rate_5m"));
        metricList.add(new MetricType("server", "server_disk_output_rate_5m"));
        metricList.add(new MetricType("server", "server_processes_running"));
        metricList.add(new MetricType("server", "server_threads_running"));

        // 软件指标
        metricList.add(new MetricType("software", "software_jvm_nonheap_memory_usage"));
        metricList.add(new MetricType("software", "software_jvm_heap_memory_usage"));
        metricList.add(new MetricType("software", "software_jvm_gc_times_5min"));
        metricList.add(new MetricType("software", "software_jvm_threads_number"));

        return metricList;
    }
}
