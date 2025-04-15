package group.fire_monitor.controller;

import group.fire_monitor.kimi.KimiApiCaller;
import group.fire_monitor.kimi.KimiApiResponseParser;
import group.fire_monitor.kimi.Prompt;
import group.fire_monitor.mapper.AutoGroupMapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.pojo.*;
import group.fire_monitor.pojo.form.AutoGroupForm;
import group.fire_monitor.pojo.form.KimiAnalyzeForm;
import group.fire_monitor.pojo.res.AutoRes;
import group.fire_monitor.pojo.res.KimiAnalyzeRes;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Api(tags = "6:分析")
@RequestMapping("/data")
public class AnalyzeController {
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private MonitorMapper monitorMapper;
    @Autowired
    private Prompt promptService;
    @Autowired
    private KimiApiCaller kimiApiCaller;
    @Autowired
    private KimiApiResponseParser kimiApiResponseParser;
    @Autowired
    private ResourceMapper resourceMapper;
    @PostMapping("/analyzeData")
    @ResponseBody
    @ApiOperation("分析数据")
    public UniversalResponse<?> analyzeData(@RequestBody KimiAnalyzeForm analyzeForm) {
        try {
            Monitor monitor = monitorMapper.selectById(analyzeForm.getMonitorId());
            if(analyzeForm.getEndTime()==null){
                analyzeForm.setEndTime(new Date());
            }
            if(analyzeForm.getStartTime()==null){

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(analyzeForm.getEndTime());
                    calendar.add(Calendar.MINUTE, -30); // 减去 30 分钟
                    analyzeForm.setStartTime(calendar.getTime());
            }
            List<PrometheusResult> resultList = monitorService.getSequenceMonitorData(monitor, analyzeForm.getStartTime(), analyzeForm.getEndTime());
            List<Resource> resourceList=resourceMapper.selectBatchIds(CommonUtil.stringToList(monitor.getMonitorResourceIds()));
            String prompt = promptService.getPrometheusAnalyzePrompt(resultList, monitor,resourceList, analyzeForm.getStartTime(), analyzeForm.getEndTime());
            String response = kimiApiCaller.getKimiResponse(prompt);
            KimiAnalyzeRes res = kimiApiResponseParser.parse(response);
            return new UniversalResponse<>().success(res);
        } catch (Exception e) {
            return new UniversalResponse<>().fail(e);
        }
    }
}