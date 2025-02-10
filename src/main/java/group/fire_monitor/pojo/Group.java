package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("basic_group")
public class Group {
    private Integer id;
    private String name;
    private Integer defaultPermissionLevel;
    private Integer groupLeaderId;

}
