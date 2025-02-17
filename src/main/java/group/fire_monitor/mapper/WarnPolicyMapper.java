package group.fire_monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.fire_monitor.pojo.WarnPolicy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarnPolicyMapper extends BaseMapper<WarnPolicy> {
}
