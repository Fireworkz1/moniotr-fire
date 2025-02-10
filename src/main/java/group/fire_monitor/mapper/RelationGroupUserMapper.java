package group.fire_monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.fire_monitor.pojo.RelationGroupUser;
import group.fire_monitor.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RelationGroupUserMapper extends BaseMapper<RelationGroupUser> {
    @Select("SELECT u.* FROM basic_user u JOIN relation_user_group rug ON u.id = rug.user_id WHERE rug.group_id = #{groupId}")
    List<User> selectUsersByGroupId(Integer groupId);
}
