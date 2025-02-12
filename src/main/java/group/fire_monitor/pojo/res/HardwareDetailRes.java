package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class HardwareDetailRes {
    Integer prometheusUp;
    String prometheusJobname;
    String prometheusInstance;
    String prometheusCpuNums;
    Double prometheusTotalMemoryGBs;
    Double prometheusAvailableFileGBs;

    Integer resouceId;
    String resouceName;
    String resouceIp;
    String resourceType;
    String resourceDescription;
    String exporterType;
}
