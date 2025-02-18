package group.fire_monitor.service.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
public class PrometheusQueryExecutor {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String prometheusUrl = "http://8.130.20.137:9090";

    public PrometheusResponse executeQuery(String query, Date start, Date end) {
        // 将 Date 转换为 Unix 时间戳（秒）
        long startTimestamp = start.getTime() / 1000; // 转换为秒
        long endTimestamp = end.getTime() / 1000;     // 转换为秒

        // 构造查询 URL
        String url = prometheusUrl + "/api/v1/query_range?query={query}&start={start}&end={end}&step=60s";

        // 发起 HTTP 请求
        String response = restTemplate.getForObject(url, String.class, query, startTimestamp, endTimestamp);

        // 解析响应
        return PrometheusResponseParser.parse(response);
    }

    public PrometheusResponse executeQuery(String query) {


        String url = prometheusUrl + "/api/v1/query?query={query}";
        String ans=restTemplate.getForObject(url,String.class,query);
        return PrometheusResponseParser.parse(ans);


    }



    /*
     * 自定义Promql
     * */
    public String custom(String promql) throws RuntimeException {
        return promql;
    }
    /*
    * 单个采集器是否在线
    * */
    public String up_single(String ip, String port) throws Exception {
        String query = String.format("up{instance=\"%s:%s\"}", ip, port);
        return query;
    }
    /*
     * 全部采集器
     * */
    public String up() throws Exception {
        String query = "up";
        return query;
    }
    /*
     * 服务器cpu数量
     * */
    public String server_cpu_nums() throws Exception {
        String query = "count(node_cpu_seconds_total{mode=\"idle\"})by(instance)";
        return query;
    }
    /*
     * 单个服务器cpu数量
     * */
    public String server_cpu_nums_single(String ip) throws Exception {
        String query = "count(node_cpu_seconds_total{mode=\"idle\",instance=\""+ip+":9100\"})by(instance)";
        return query;
    }
    /*
     * 服务器总内存
     * */
    public String server_memory_gb(String ip) throws Exception {
        String query = "node_memory_MemTotal_bytes/1024/1024/1024";
        return query;
    }
    /*
     * 单个服务器总内存
     * */
    public String server_memory_gb_single(String ip) throws Exception {
        String query = "node_memory_MemTotal_bytes{instance=\""+ip+":9100\"}/1024/1024/1024";
        return query;
    }
    /*
     * 服务器总磁盘剩余用量
     * */
    public String server_file_free_gb()  {
        String query = "sum(node_filesystem_free_bytes{fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return query;
    }
    /*
     * 单个服务器总磁盘剩余用量
     * */
    public String server_file_free_gb_single(String ip)  {
        String query = "sum(node_filesystem_free_bytes{instance=\""+ip+":9100\",fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return query;
    }
    /*
     * 服务器总磁盘大小
     * */
    public String server_file_size_gb(){
        String query = "sum(node_filesystem_size_bytes{fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return query;
    }
    /*
     * 单个服务器总磁盘大小
     * */
    public String server_file_size_gb_single(String ip) {
        String query = "sum(node_filesystem_size_bytes{instance=\""+ip+":9100\",fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return query;
    }

    /*
         * 服务器一分钟平均负载
         * 平均负载值是一个无量纲的数值，没有单位。
    平均负载值可以用于触发告警，例如，如果平均负载值超过某个阈值，可以表示系统可能过载。
         * */
    public String server_load_1min()  {
        String query = "node_load1";
        return query;
    }
    /*
     * 单个服务器一分钟平均负载
     * 平均负载值是一个无量纲的数值，没有单位。
平均负载值可以用于触发告警，例如，如果平均负载值超过某个阈值，可以表示系统可能过载。
     * */
    public String server_load_1min_single(String ip) {
        String query = "node_load1{instance=\""+ip+"\":9100\"}";
        return query;
    }

    /*
     * 服务器cpu占用核数
     * */
    public String server_cpu_usage_1min() {
        String query = "(1 - avg(irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) by (instance)) * 100";
        return query;
    }
    public String server_cpu_usage_1min_single(String ip){
        String query = "(1 - avg(irate(node_cpu_seconds_total{instance=\""+ip+":9100\",mode=\"idle\"}[1m])) by (instance)) * 100";
        return query;
    }
    /*
     * 服务器内存占用率
     * */
    public String server_memory_usage_1min()  {
        String query = "(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100";
        return query;
    }
    public String server_memory_usage_1min_single(String ip) {
        String query = "(node_memory_MemTotal_bytes{instance=\""+ip+":9100\"}" +
                " - node_memory_MemAvailable_bytes{instance=\""+ip+":9100\"})" +
                " / node_memory_MemTotal_bytes{instance=\""+ip+":9100\"} * 100";

        return query;
    }

    /*
     * 服务器基本数据
     * */
    public String server_basic_data()  {
        String query = "node_uname_info";
        return query;
    }
    public String server_basic_data_single(String ip)  {
        String query = "node_uname_info{instance=\""+ip+":9100\"}";
        return query;
    }

    /*
     * 服务器运行时间
     * */
    public String server_running_seconds() {
        String query = "time() - node_boot_time_seconds";
        return query;
    }
    public String server_running_seconds_single(String ip)  {
        String query = "time() - node_boot_time_seconds{instance=\""+ip+":9100\"}";
        return query;
    }

    /*
     * CPU 上下文切换次数
     * */
    public String server_cpu_context_switches_5m() {
        String query = "rate(node_context_switches_total[5m])";
        return query;
    }

    /*
     * 磁盘I速率
     * */
    public String server_disk_input_rate_5m() {
        String query = "rate(node_disk_read_bytes_total[5m])";
        return query;
    }

    /*
     * 磁盘O速率
     * */
    public String server_disk_output_rate_5m() {
        String query = "rate(node_disk_written_bytes_total[5m])";
        return query;
    }

    /*
     * 进程数量
     * */
    public String server_processes_running() {
        String query = "node_processes_running";
        return query;
    }
    /*
     * 线程数量
     * */
    public String server_threads_running() {
        String query = "node_threads_running";
        return query;
    }




    /*
     * 软件指标
     *
     *
     * */

    /*
    *JVM非堆内存使用率
    * */
    public String software_jvm_nonheap_memory_usage() {
        String query = "100 * (jvm_memory_used_bytes{area=\"nonheap\"} / jvm_memory_max_bytes{area=\"nonheap\"})";
        return query;
    }

    /*
     *JVM堆内存使用率
     * */
    public String software_jvm_heap_memory_usage() {
        String query = "100 * (jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"})";
        return query;
    }

    /*
     *JVM gc次数
     * */
    public String software_jvm_gc_times_5min() {
        String query = "rate(jvm_gc_collection_count[5m])";
        return query;
    }

    /*
     *JVM线程数量
     * */
    public String software_jvm_threads_number() {
        String query = "jvm_threads_live_threads";
        return query;
    }

}
