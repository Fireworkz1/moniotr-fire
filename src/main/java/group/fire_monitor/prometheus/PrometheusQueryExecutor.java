package group.fire_monitor.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class PrometheusQueryExecutor {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String prometheusUrl = "http://8.130.20.137:9090/api/v1/query";

    public PrometheusResponse executeQuery(String query) {
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        try{
            return PrometheusResponseParser.parse(restTemplate.getForObject(prometheusUrl, String.class, params));
        }catch (Exception e){
            e.printStackTrace();
            return new PrometheusResponse();
        }

    }
    public PrometheusResponse isMicroserviceOnline(String ip,String port){
        String query = "up{instance=\""+ip+":"+port+"\"}";
        return executeQuery(query);
    }
    public PrometheusResponse up()  {
        String query = "up";
        return executeQuery(query);
    }
}
