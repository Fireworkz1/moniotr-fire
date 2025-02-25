package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class HardwareDetailRes {
    Integer prometheusUp;
    String prometheusJobname;
    String prometheusInstance;
    Integer resouceId;
    String resouceName;
    String resouceIp;
    String resourceType;
    String exporterType;
    String resourceDescription;

    String prometheusCpuNums;
    Double prometheusTotalMemoryGBs;
    Double prometheusAvailableFileGBs;
    String prometheusServerloadtime;
    String machine;
    String sysname;
    String version;
    String nodename;
}
