package group.fire_monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.fire_monitor.pojo.WarnPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WarnPolicyMapper extends BaseMapper<WarnPolicy> {
    // 自定义查询方法：连表查询并支持模糊搜索
    @Select("<script>" +
            "SELECT wp.*, mi.monitor_preset_target, mi.monitor_notpreset_promql " +
            "FROM warn_policy wp " +
            "LEFT JOIN monitor_instance mi ON wp.monitor_id = mi.id " +
            "<where>" +
            "   <if test='str != null and str != \"\"'>" +
            "       (wp.warn_name LIKE CONCAT('%', #{str}, '%') " +
            "       OR mi.monitor_name LIKE CONCAT('%', #{str}, '%') " +
            "       OR mi.monitor_preset_target LIKE CONCAT('%', #{str}, '%') " +
            "       OR mi.monitor_notpreset_promql LIKE CONCAT('%', #{str}, '%'))" +
            "   </if>" +
            "</where> " +
            "ORDER BY wp.monitor_on DESC, wp.is_active DESC " +
            "</script>")
    List<WarnPolicy> selectPoliciesWithMonitorInstance(@Param("str") String str);
}