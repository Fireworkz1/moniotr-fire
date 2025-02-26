package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.WarnHistoryMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.WarnHistory;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.ChangeMonitorForm;
import group.fire_monitor.pojo.form.GraphDataForm;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "3:监控实例")
@RequestMapping("/monitor")
public class MonitorController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    MonitorMapper monitorMapper;
    @Autowired
    WarnPolicyMapper warnPolicyMapper;
    @Autowired
    WarnHistoryMapper warnHistoryMapper;
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("创建监控实例")
    public UniversalResponse<?> create(@RequestBody AddMonitorForm form) {
        return monitorService.create(form);
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation("修改监控侦测对象")
    public UniversalResponse<?> modify(@RequestBody ChangeMonitorForm form) {
        try{
            monitorService.update(form);
            return new UniversalResponse<>().success();
        } catch (Exception e) {
            return new UniversalResponse<>(500, e.getMessage());
        }

    }
    @PostMapping("/update")
    @Transactional
    @ResponseBody
    @ApiOperation("修改实例")
    public UniversalResponse<?> update(@RequestBody Monitor monitor) {
        try{
            monitorMapper.updateById(monitor);
            List<WarnPolicy> warnPolicys= warnPolicyMapper.selectList(new QueryWrapper<WarnPolicy>().eq("monitor_id",monitor.getId()));
            for(WarnPolicy warnPolicy:warnPolicys){
                warnPolicy.setMonitorName(monitor.getMonitorName());
            }
            warnPolicyMapper.updateById(warnPolicys);
            List<WarnHistory> warnHis= warnHistoryMapper.selectList(new QueryWrapper<WarnHistory>().eq("monitor_id",monitor.getId()));
            for(WarnHistory history:warnHis){
                history.setMonitorName(monitor.getMonitorName());
            }
            warnHistoryMapper.updateById(warnHis);

            return new UniversalResponse<>().success();
        } catch (Exception e) {
            return new UniversalResponse<>().fail(e);
        }


    }


    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("删除监控实例")
    public UniversalResponse<?> delete(@RequestParam Integer monitorId) {
            monitorMapper.deleteById(monitorId);
            return new UniversalResponse<>().success();
    }

    @PostMapping("/selectTableById")
    @ResponseBody
    @ApiOperation("查询某实例单次详细数据")
    public UniversalResponse<?> selectDetailByid(@RequestParam Integer id) {
        try{
            Monitor monitor= monitorMapper.selectById(id);
            return new UniversalResponse<>().success(monitorService.getSingleMonitorData(monitor));
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }


    @PostMapping("/selectGraphById")
    @ResponseBody
    @ApiOperation("查询某实例时间序列数据")
    public UniversalResponse<?> selectGraphByid(@RequestBody GraphDataForm form) {
        try{
            Monitor monitor= monitorMapper.selectById(form.getMonitorId());
            return new UniversalResponse<>().success(monitorService.getSequenceMonitorData(monitor,form.getStartTime(),form.getEndTime()));
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
