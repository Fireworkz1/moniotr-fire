package group.fire_monitor.util.dic;

import group.fire_monitor.pojo.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetricDictionary {
    public static List<MetricType> getMetricList() {
        List<MetricType> metricList = new ArrayList<>();
        metricList.add(new MetricType("server", "up", "资源是否在线（1为是，0为否）"));
        metricList.add(new MetricType("software", "up", "资源是否在线（1为是，0为否）"));
        metricList.add(new MetricType("mysql", "up", "资源是否在线（1为是，0为否）"));
        metricList.add(new MetricType("redis", "up", "资源是否在线（1为是，0为否）"));
// 服务器指标
        metricList.add(new MetricType("server", "server_file_free_gb", "服务器磁盘剩余空间（GB）"));
        metricList.add(new MetricType("server", "server_load_1min", "服务器1分钟负载"));
        metricList.add(new MetricType("server", "server_cpu_usage_1min", "服务器1分钟CPU使用率"));
        metricList.add(new MetricType("server", "server_memory_usage_1min", "服务器1分钟内存使用率"));
        metricList.add(new MetricType("server", "server_cpu_context_switches_5m", "服务器5分钟CPU上下文切换次数"));
        metricList.add(new MetricType("server", "server_disk_input_rate_5m", "服务器5分钟磁盘输入速率"));
        metricList.add(new MetricType("server", "server_disk_output_rate_5m", "服务器5分钟磁盘输出速率"));
        metricList.add(new MetricType("server", "node_forks_total", "服务器系统中创建进程的总数"));

        // 软件指标
        metricList.add(new MetricType("server", "software_jvm_threads_states_threads", "JVM线程状态"));
        metricList.add(new MetricType("software", "software_jvm_nonheap_memory_usage", "JVM非堆内存使用率"));
        metricList.add(new MetricType("software", "software_jvm_heap_memory_usage", "JVM堆内存使用率"));
        metricList.add(new MetricType("software", "software_jvm_gc_times_5min", "JVM 5分钟内垃圾回收次数"));
        metricList.add(new MetricType("software", "software_jvm_threads_number", "JVM当前线程数量"));

        // MySQL指标
        metricList.add(new MetricType("mysql", "mysql_qps", "MySQL每秒查询次数（QPS）"));
        metricList.add(new MetricType("mysql", "mysql_slow_queries", "MySQL每秒慢查询次数"));
        metricList.add(new MetricType("mysql", "mysql_connections", "MySQL当前连接数"));
        metricList.add(new MetricType("mysql", "mysql_available_connections", "MySQL剩余可用连接数"));

        //redis
        metricList.add(new MetricType("redis", "redis_connected_clients", "Redis连接的客户端数量"));
        metricList.add(new MetricType("redis", "redis_operations_per_second", "Redis每秒执行的操作数（OPS）"));
        metricList.add(new MetricType("redis", "redis_memory_usage", "Redis内存使用情况（字节）"));
//        metricList.add(new MetricType("redis", "redis_keyspace_hits_and_misses", "Redis键空间命中和未命中的总数"));
        metricList.add(new MetricType("redis", "redis_memory_fragmentation_ratio", "Redis内存碎片率"));
//        metricList.add(new MetricType("redis", "redis_network_traffic", "Redis每秒网络流量（接收+发送）"));
        metricList.add(new MetricType("redis", "redis_command_latency", "Redis命令平均延迟"));
        metricList.add(new MetricType("redis", "redis_uptime_in_seconds", "Redis实例运行时间（秒）"));
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
