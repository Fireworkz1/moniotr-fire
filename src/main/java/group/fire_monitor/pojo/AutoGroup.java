package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("auto_policy_groups")
public class AutoGroup {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String autoGroupName;
    private String autoPolicyIds;
    private String resourceIds;
    private String description;

}