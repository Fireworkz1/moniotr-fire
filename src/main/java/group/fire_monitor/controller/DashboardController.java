package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.*;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.WarnEntity;
import group.fire_monitor.pojo.WarnHistory;
import group.fire_monitor.pojo.res.DashRes;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@Api(tags = "3:监控实例")
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private WarnPolicyMapper warnPolicyMapper;
    @Autowired
    private WarnHistoryMapper warnHistoryMapper;
    @Autowired
    private WarnEntityMapper warnEntityMapper;

    @GetMapping("/basicInfo")
    @ApiOperation("dashboard展示信息")
    @ResponseBody
    public UniversalResponse<?> info(){
        try{
            Integer userNum=userMapper.selectList(null).size();
            List<Map<String, Object>> results = resourceMapper.selectMaps(
                    new QueryWrapper<Resource>()
                            .inSql("resource_type", "'server', 'software', 'mysql', 'redis'")
                            .groupBy("resource_type")
                            .select("resource_type, COUNT(*) as count")
            );

// 将结果存入 Map
            Map<String, Integer> resourceCounts = new HashMap<>();
            for (Map<String, Object> result : results) {
                String resourceType = (String) result.get("resource_type");
                Integer count = ((Number) result.get("count")).intValue();
                resourceCounts.put(resourceType, count);
            }

// 获取统计结果
            Integer serverNum = resourceCounts.getOrDefault("server", 0);
            Integer softwareNum = resourceCounts.getOrDefault("software", 0);
            Integer dbNum = resourceCounts.getOrDefault("mysql", 0);
            Integer cacheNum = resourceCounts.getOrDefault("redis", 0);
            Integer warnNum=warnPolicyMapper.selectList(null).size();


            Integer hisWarningNum= Math.toIntExact(warnHistoryMapper.selectCount(null));
            Integer totalWarningNum=warnHistoryMapper.selectList(null).stream().mapToInt(WarnHistory::getWarnRepeatTimes).sum();
            totalWarningNum+=warnEntityMapper.selectList(null).stream().mapToInt(WarnEntity::getWarnRepeatTimes).sum();
            totalWarningNum=totalWarningNum*12;
            DashRes res=new DashRes();
            res.setServerNum(serverNum);
            res.setSoftwareNum(softwareNum);
            res.setWarnNum(warnNum);
            res.setUserNum(userNum);
            res.setDbNum(dbNum);
            res.setCacheNum(cacheNum);
            res.setHisWarningNum(hisWarningNum);
            res.setTotalWarningNum(totalWarningNum);
            return new UniversalResponse<>().success(res);
        } catch (Exception e) {
            return new UniversalResponse<>().fail(e);
        }

    }
}
