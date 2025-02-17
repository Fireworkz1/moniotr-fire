package group.fire_monitor.controller;

import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.AddWarnForm;
import group.fire_monitor.service.WarnService;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "4:告警管理")
@RequestMapping("/warn")
public class WarnController {
    @Autowired
    private WarnService warnService;
    @Autowired
    private WarnPolicyMapper warnPolicyMapper;
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("创建告警策略")
    public UniversalResponse<?> create(@RequestBody AddWarnForm form) {
        try{
            warnService.create(form);
            return new UniversalResponse<>().success();
        }catch (RuntimeException e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }




    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("删除告警策略")
    public UniversalResponse<?> delete(@RequestParam Integer warnPolicyId) {
        try{
            warnService.delete(warnPolicyId);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

//    @PostMapping("/selectById")
//    @ResponseBody
//    @ApiOperation("查询某实例详细数据")
//    public UniversalResponse<?> selectDetailByid(@RequestParam Integer id) {
//        try{
//            WarnPolicy policy = warnPolicyMapper.selectById(id);
//            return new UniversalResponse<>().success(monitorService.getMonitorData(monitor));
//        } catch (Exception e) {
//            return new UniversalResponse<>(500,e.getMessage());
//        }
//    }

    @PostMapping("/selectLike")
    @ResponseBody
    @ApiOperation("模糊查询告警实例")
    public UniversalResponse<?> selectByName(@RequestParam(required = false) String str) {
        try{
            List<WarnPolicy> policies= warnService.selectLike(str);
            return new UniversalResponse<>().success(policies);
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @PostMapping("/changeActive")
    @ResponseBody
    @ApiOperation("更改活跃状态")
    public UniversalResponse<?> changeActive(@RequestParam Integer id,@RequestParam Integer changeTo) {
        try{
            WarnPolicy policie= warnPolicyMapper.selectById(id);
            policie.setIsActive(changeTo);
            warnPolicyMapper.updateById(policie);
            return new UniversalResponse<>().success();
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

}
