package group.fire_monitor.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.fire_monitor.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
