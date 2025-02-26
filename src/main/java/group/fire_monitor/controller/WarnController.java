package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.WarnHistoryMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.WarnHistory;
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
import java.util.stream.Collectors;

@RestController
@Api(tags = "4:告警管理")
@RequestMapping("/warn")
public class WarnController {
    @Autowired
    private WarnService warnService;
    @Autowired
    private WarnPolicyMapper warnPolicyMapper;
    @Autowired
    private WarnHistoryMapper warnHistoryMapper;
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

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation("修改告警策略")
    public UniversalResponse<?> update(@RequestBody WarnPolicy form) {
        try{
            warnPolicyMapper.updateById(form);
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

    @PostMapping("/getWarn")
    @ResponseBody
    @ApiOperation("查询告警（告警中优先）")
    public UniversalResponse<?> selectByName(@RequestParam(required = false) String str) {
        try{
            List<WarnPolicy> policies= warnService.selectLike(str);
            return new UniversalResponse<>().success(policies);
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }
    @PostMapping("/getWarnByid")
    @ResponseBody
    @ApiOperation("查询告警（byid）")
    public UniversalResponse<?> selectById(@RequestParam Integer id) {
        try{
            WarnPolicy policy= warnPolicyMapper.selectById(id);
            return new UniversalResponse<>().success(policy);
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @PostMapping("/changeMonitorOn")
    @ResponseBody
    @ApiOperation("更改是否监控状态")
    public UniversalResponse<?> changeActive(@RequestParam Integer id,@RequestParam Integer monitorOn) {
        try{
            WarnPolicy policie= warnPolicyMapper.selectById(id);
            policie.setMonitorOn(monitorOn);
            warnPolicyMapper.updateById(policie);
            return new UniversalResponse<>().success();
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }




    @PostMapping("/warnhistory")
    @ResponseBody
    @ApiOperation("查看历史")
    public UniversalResponse<?> getHistory(@RequestParam(required = false) String str) {

        List<WarnHistory> histories=warnHistoryMapper.selectList(new QueryWrapper<WarnHistory>().like("warn_name",str)
                .like("warn_description",str)
                .orderByDesc("last_warning_time"));

        return new UniversalResponse<>().success(histories);
    }
}
