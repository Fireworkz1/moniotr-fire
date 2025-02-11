package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("relation_group_resource_permission")
public class RelationGroupResource {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer groupId;
    Integer resourceId;
}
