package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("basic_group")
public class Group {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer defaultPermissionLevel;
    private Integer groupLeaderId;

}
