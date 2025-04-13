package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("auto_policy")
public class Auto {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String autoName;
    private String autoPolicy;
    private String compareType;
    private Double warnThreshold;
    private Integer resourceId;
    private Integer monitorOn;
    private String monitorPresetTarget;
    private Date modifiedTime;
    private Integer triggerTimes;
    private String targetIds;
    private String type;
}
