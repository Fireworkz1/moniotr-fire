package group.fire_monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.fire_monitor.pojo.RelationGroupResource;
import group.fire_monitor.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RelationGroupResourceMapper extends BaseMapper<RelationGroupResource> {
    @Select("<script>" +
            "SELECT DISTINCT r.resource_id " +
            "FROM relation_group_resource_permission r " +
            "<where>" +
            "   <if test='groupIds != null and !groupIds.isEmpty()'>" +
            "       r.group_id IN " +
            "       <foreach item='groupId' collection='groupIds' open='(' separator=',' close=')'>" +
            "           #{groupId}" +
            "       </foreach>" +
            "   </if>" +
            "</where>" +
            "</script>")
    List<Integer> selectPermittedResourceId(@Param("groupIds") List<Integer> groupIds);
}
