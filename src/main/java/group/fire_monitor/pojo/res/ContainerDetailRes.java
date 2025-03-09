package group.fire_monitor.pojo.res;

import lombok.Data;

import java.util.Date;

@Data
public class ContainerDetailRes {
    private String status;
    private String driver;
    private String createdAt;
    private String startedAt;
    private String name;
    private String containerId;
    private String platform;
    private String command;
    private String image;
    private String ports;
    private String IPAdress;
}
