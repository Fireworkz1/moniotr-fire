package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class SoftwareDetailRes {
    Integer prometheusUp;
    String prometheusJobname;
    String prometheusInstance;
    Integer resourceId;
    String resourceName;
    String resourceIp;
    String resourceType;
    String exporterType;
    String resourceDescription;

    String resourcePort;
    String startMode;


}
