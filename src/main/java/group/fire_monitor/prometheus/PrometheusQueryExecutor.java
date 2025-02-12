package group.fire_monitor.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

@Service
public class PrometheusQueryExecutor {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String prometheusUrl = "http://8.130.20.137:9090/api/v1/query";

    public PrometheusResponse executeQuery(String query) throws Exception {


            String url = prometheusUrl + "?query={query}";
            String ans=restTemplate.getForObject(url,String.class,query);
            return PrometheusResponseParser.parse(ans);


    }
    public PrometheusResponse isMicroserviceOnline(String ip,String port) throws Exception {
        String query = String.format("up{instance=\"%s:%s\"}", ip, port);
        return executeQuery(query);
    }
    public PrometheusResponse up() throws Exception {
        String query = "up";
        return executeQuery(query);
    }
}
