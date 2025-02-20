package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.pojo.Group;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@Api(tags = "3:监控实例")
@RequestMapping("/monitor")
public class MonitorController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    MonitorMapper monitorMapper;
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("创建监控实例")
    public UniversalResponse<?> create(@RequestBody AddMonitorForm form) {
        return monitorService.create(form);
    }

//    @PostMapping("/update")
//
//    @ResponseBody
//    @ApiOperation("修改实例")
//    public UniversalResponse<?> update(@RequestBody Monitor monitor) {
//        return null;
//    }


    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("删除监控实例")
    public UniversalResponse<?> delete(@RequestParam Integer monitorId) {
            monitorMapper.deleteById(monitorId);
            return new UniversalResponse<>().success();
    }

    @PostMapping("/selectById")
    @ResponseBody
    @ApiOperation("查询某实例详细数据")
    public UniversalResponse<?> selectDetailByid(@RequestParam Integer id) {
        try{
            Monitor monitor= monitorMapper.selectById(id);
            return new UniversalResponse<>().success(monitorService.getMonitorData(monitor));
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @PostMapping("/selectLike")
    @ResponseBody
    @ApiOperation("模糊查询监控实例")
    public UniversalResponse<?> selectByName(@RequestParam(required = false) String str,@RequestParam String type) {
        List<Monitor> monitorList= monitorService.selectLike(str,type);

        return new UniversalResponse<>().success(monitorList);
    }
}
