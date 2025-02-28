package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("monitor_instance")
public class Monitor {
    @TableId(type = IdType.AUTO)
    private Integer id;
//    private Integer resourceId;
    private String monitorName;
    private String monitorDescription;
    private String monitorNotpresetPromql;
    private String monitorPresetTarget;
    private Integer monitorIspreset;
    private String monitorDemonstration;//table/graph
    private Integer monitorPermissionLevel;
    private String monitorGroupIds;
    private String monitorResourceIds;
    private String monitorType;
    private Date monitorAddedTime;
}
