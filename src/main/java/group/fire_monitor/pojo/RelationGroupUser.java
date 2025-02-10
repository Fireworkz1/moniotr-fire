package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("relation_user_group")
public class RelationGroupUser {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer user_id;
    Integer group_id;
}
