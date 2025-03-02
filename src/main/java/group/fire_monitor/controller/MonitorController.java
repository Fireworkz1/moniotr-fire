package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.WarnHistoryMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.MonitorWithListPojo;
import group.fire_monitor.pojo.WarnHistory;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.ChangeMonitorForm;
import group.fire_monitor.pojo.form.GraphDataForm;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.dic.MetricDictionary;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

//    @PostMapping("/update")
//    @ResponseBody
//    @ApiOperation("修改监控侦测对象")
//    public UniversalResponse<?> modify(@RequestBody ChangeMonitorForm form) {
//        try{
//            monitorService.update(form);
//            return new UniversalResponse<>().success();
//        } catch (Exception e) {
//            return new UniversalResponse<>(500, e.getMessage());
//        }
//
//    }
    @PostMapping("/update")
    @Transactional
    @ResponseBody
    @ApiOperation("修改实例")
    public UniversalResponse<?> update(@RequestBody MonitorWithListPojo monitorWithListPojo) {

        try{
            Monitor monitor=new Monitor();
            BeanUtils.copyProperties(monitorWithListPojo,monitor);
            monitor.setMonitorResourceIds(CommonUtil.listToString(monitorWithListPojo.getMonitorResourceIds()));
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
    @Transactional
    @ApiOperation("删除监控实例")
    public UniversalResponse<?> delete(@RequestParam Integer monitorId) {
        try{
            monitorMapper.deleteById(monitorId);
            warnPolicyMapper.delete(new QueryWrapper<WarnPolicy>().eq("monitor_id",monitorId));
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }

    @PostMapping("/selectById")
    @ResponseBody
    @ApiOperation("查询监控实例")
    public UniversalResponse<?> selectId(@RequestParam Integer monitorId) {
        MonitorWithListPojo pojo=new MonitorWithListPojo();
        Monitor monitor=monitorMapper.selectById(monitorId);
        BeanUtils.copyProperties(monitor,pojo);
        pojo.setMonitorResourceIds(CommonUtil.stringToList(monitor.getMonitorResourceIds()));
        return new UniversalResponse<>().success(pojo);
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
    public UniversalResponse<?> selectByName(@RequestParam(required = false) String str,@RequestParam(required = false) String type) {
        List<Monitor> monitorList= monitorService.selectLike(str,type);
        List<MonitorWithListPojo> monitorWithListPojos=new ArrayList<>();
        for(Monitor monitor:monitorList){
            MonitorWithListPojo pojo=new MonitorWithListPojo();
            BeanUtils.copyProperties(monitor,pojo);
            pojo.setMonitorResourceIds(CommonUtil.stringToList(monitor.getMonitorResourceIds()));
            monitorWithListPojos.add(pojo);
        }
        return new UniversalResponse<>().success(monitorWithListPojos);
    }

    @GetMapping("/metricList")
    @ResponseBody
    @ApiOperation("获取指标")
    public UniversalResponse<?> metric(){
        return new UniversalResponse<>().success(MetricDictionary.getMetricList());
    }
}
