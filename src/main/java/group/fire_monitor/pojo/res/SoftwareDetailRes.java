package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class SoftwareDetailRes {
    Integer prometheusUp;
    String prometheusJobname;
    String prometheusInstance;
    Integer resouceId;
    String resouceName;
    String resouceIp;
    String resourceType;
    String exporterType;
    String resourceDescription;

    String resourcePort;
    String startMode;


}
