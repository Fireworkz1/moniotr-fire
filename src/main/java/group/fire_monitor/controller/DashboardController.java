package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.mapper.UserMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.res.DashRes;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.ReentrantLock;

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

    @GetMapping("/basicInfo")
    @ApiOperation("dashboard展示信息")
    @ResponseBody
    public UniversalResponse<?> info(){
        try{
            Integer userNum=userMapper.selectList(null).size();
            Integer serverNum=resourceMapper.selectList(new QueryWrapper<Resource>().eq("resource_type","server")).size();
            Integer softwareNum=resourceMapper.selectList(new QueryWrapper<Resource>().eq("resource_type","software")).size();
            Integer warnNum=warnPolicyMapper.selectList(null).size();
            DashRes res=new DashRes();
            res.setServerNum(serverNum);
            res.setSoftwareNum(softwareNum);
            res.setWarnNum(warnNum);
            res.setUserNum(userNum);
            return new UniversalResponse<>().success(res);
        } catch (Exception e) {
            return new UniversalResponse<>().fail(e);
        }

    }
}
