package group.fire_monitor.service.prometheus;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PrometheusQueryExecutor {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String prometheusUrl = "http://8.130.20.137:9090/api/v1/query";

    private PrometheusResponse executeQuery(String query) throws Exception {


            String url = prometheusUrl + "?query={query}";
            String ans=restTemplate.getForObject(url,String.class,query);
            return PrometheusResponseParser.parse(ans);


    }

    /*
    * 单个采集器是否在线
    * */
    public PrometheusResponse up_single(String ip, String port) throws Exception {
        String query = String.format("up{instance=\"%s:%s\"}", ip, port);
        return executeQuery(query);
    }
    /*
     * 全部采集器
     * */
    public PrometheusResponse up() throws Exception {
        String query = "up";
        return executeQuery(query);
    }
    /*
     * 服务器cpu数量
     * */
    public PrometheusResponse server_cpu_nums() throws Exception {
        String query = "count(node_cpu_seconds_total{mode=\"idle\"})by(instance)";
        return executeQuery(query);
    }
    /*
     * 单个服务器cpu数量
     * */
    public PrometheusResponse server_cpu_nums_single(String ip) throws Exception {
        String query = "count(node_cpu_seconds_total{mode=\"idle\",instance=\""+ip+":9100\"})by(instance)";
        return executeQuery(query);
    }
    /*
     * 服务器总内存
     * */
    public PrometheusResponse server_memory_gb(String ip) throws Exception {
        String query = "node_memory_MemTotal_bytes/1024/1024/1024";
        return executeQuery(query);
    }
    /*
     * 单个服务器总内存
     * */
    public PrometheusResponse server_memory_gb_single(String ip) throws Exception {
        String query = "node_memory_MemTotal_bytes{instance=\""+ip+":9100\"}/1024/1024/1024";
        return executeQuery(query);
    }
    /*
     * 服务器总磁盘剩余用量
     * */
    public PrometheusResponse server_file_free_gb() throws Exception {
        String query = "sum(node_filesystem_free_bytes{fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return executeQuery(query);
    }
    /*
     * 单个服务器总磁盘剩余用量
     * */
    public PrometheusResponse server_file_free_gb_single(String ip) throws Exception {
        String query = "sum(node_filesystem_free_bytes{instance=\""+ip+":9100\",fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return executeQuery(query);
    }
    /*
     * 服务器总磁盘大小
     * */
    public PrometheusResponse server_file_size_gb() throws Exception {
        String query = "sum(node_filesystem_size_bytes{fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return executeQuery(query);
    }
    /*
     * 单个服务器总磁盘大小
     * */
    public PrometheusResponse server_file_size_gb_single(String ip) throws Exception {
        String query = "sum(node_filesystem_size_bytes{instance=\""+ip+":9100\",fstype!=\"rootfs\",fstype!=\"overlay\"})by(instance)/1024/1024/1024";
        return executeQuery(query);
    }

    /*
         * 服务器一分钟平均负载
         * 平均负载值是一个无量纲的数值，没有单位。
    平均负载值可以用于触发告警，例如，如果平均负载值超过某个阈值，可以表示系统可能过载。
         * */
    public PrometheusResponse server_load_1min() throws Exception {
        String query = "node_load1";
        return executeQuery(query);
    }
    /*
     * 单个服务器一分钟平均负载
     * 平均负载值是一个无量纲的数值，没有单位。
平均负载值可以用于触发告警，例如，如果平均负载值超过某个阈值，可以表示系统可能过载。
     * */
    public PrometheusResponse server_load_1min_single(String ip) throws Exception {
        String query = "node_load1{instance=\""+ip+"\":9100\"}";
        return executeQuery(query);
    }

    /*
     * 服务器cpu占用核数
     * */
    public PrometheusResponse server_cpu_usage_1min() throws Exception {
        String query = "(1 - avg(irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) by (instance)) * 100";
        return executeQuery(query);
    }
    public PrometheusResponse server_cpu_usage_1min_single(String ip) throws Exception {
        String query = "(1 - avg(irate(node_cpu_seconds_total{instance=\""+ip+":9100\",mode=\"idle\"}[1m])) by (instance)) * 100";
        return executeQuery(query);
    }
    /*
     * 服务器内存占用率
     * */
    public PrometheusResponse server_memory_usage_1min() throws Exception {
        String query = "(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100";
        return executeQuery(query);
    }
    public PrometheusResponse server_memory_usage_1min_single(String ip) throws Exception {
        String query = "(node_memory_MemTotal_bytes{instance=\""+ip+":9100\"}" +
                " - node_memory_MemAvailable_bytes{instance=\""+ip+":9100\"})" +
                " / node_memory_MemTotal_bytes{instance=\""+ip+":9100\"} * 100";

        return executeQuery(query);
    }

    /*
     * 服务器基本数据
     * */
    public PrometheusResponse server_basic_data() throws Exception {
        String query = "node_uname_info";
        return executeQuery(query);
    }
    public PrometheusResponse server_basic_data_single(String ip) throws Exception {
        String query = "node_uname_info{instance=\""+ip+":9100\"}";
        return executeQuery(query);
    }


    public PrometheusResponse server_running_seconds() throws Exception {
        String query = "time() - node_boot_time_seconds";
        return executeQuery(query);
    }
    public PrometheusResponse server_running_seconds_single(String ip) throws Exception {
        String query = "time() - node_boot_time_seconds{instance=\""+ip+":9100\"}";
        return executeQuery(query);
    }
}
