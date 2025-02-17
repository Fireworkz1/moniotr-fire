package group.fire_monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.PrometheusResult;
import group.fire_monitor.pojo.WarnContent;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.enums.WarnNoticeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Warner {
    @Autowired
    WarnPolicyMapper warnPolicyMapper;
    @Autowired
    MonitorService monitorService;
    @Autowired
    MonitorMapper monitorMapper;

    private final ExecutorService executorService;

    public Warner() {
        // 创建一个固定大小的线程池，大小可以根据需求调整
        this.executorService = Executors.newFixedThreadPool(10);
    }




    @Scheduled(fixedRate = 600000)
    public void checkWarning() {
        try{
            warning();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void warning() {
        List<WarnPolicy> warnTargetList=warnPolicyMapper.selectList(new QueryWrapper<WarnPolicy>().eq("is_active",1));
        List<WarnPolicy> updateWarnList = Collections.synchronizedList(new ArrayList<>());
        for (WarnPolicy policy : warnTargetList) {
            executorService.submit(() -> {
                Monitor monitor = monitorMapper.selectById(policy.getMonitorId());
                List<PrometheusResult> resultList = monitorService.getMonitorData(monitor);
                String flag="safe";
                for (PrometheusResult result : resultList) {
                    Double value= (Double) result.getValue().get(1);

                    //处理告警逻辑
                    if(CommonUtil.needToWarn(policy.getCompareType(),value, policy.getWarnThreshold())){
                        flag="active";
                        //如果需要告警
                            //再处理告警
                        switch(policy.getWarnLevel()){
                            case 0:
                                break;
                            case 1:
                                logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(),"[LOG_ONLY]");
                            case 2:
                                logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(),"[LOG_TO_DB]");
                                logToDB(policy,monitor,value);
                            case 3:
                                logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(),"[NOTICE_USER]");
                                logToDB(policy,monitor,value);
                                if(policy.getHasSentNotice()==0){
                                    sendNotice(policy,monitor,value);
                                    policy.setHasSentNotice(1);
                                }


                            default:
                                throw new RuntimeException("错误告警级别");
                        }
                    }else{
                        //如果不需要告警且以前有告警记录，需要清除告警次数
                        //处理要更新的表字段
                        if(policy.getIsActive()==1){

                        }
                    };
                    if(flag.equals("active")){
                        policy.setIsActive(1);
                        policy.setHasSentNotice(1);
                        policy.setWarnRepeatTimes(policy.getWarnRepeatTimes()+1);
                        policy.setCurrentStatus(WarnNoticeEnum.WARNING.getLevel());
                    }else{
                        if(policy.getIsActive()==1){
                            policy.setIsActive(0);
                            policy.setHasSentNotice(0);
                            policy.setWarnRepeatTimes(0);
                            policy.setCurrentStatus(WarnNoticeEnum.SAFE.getLevel());
                        }
                    }
                updateWarnList.add(policy);
                }
            });
        }
        warnPolicyMapper.updateById(updateWarnList);
    }

    private void sendNotice(WarnPolicy policy,Monitor monitor,Double value) {
        switch (policy.getNoticeWay()){
            case "email":
                sendEmail(policy,monitor,value);
                break;
            case "message":
                ;
                break;
            default:
                System.out.println("不支持方式:"+policy.getNoticeWay());
        }
    }

    private void sendEmail(WarnPolicy policy, Monitor monitor, Double value) {
        String title="fire-monitor监控系统告警信息";
        String content="warning[LEVEL=SEND_NOTICE]:"+monitor.getMonitorName()+":value="+value+" "+policy.getCompareType()
                +"threshold="+ policy.getWarnThreshold();
    }

    private void logToDB(WarnPolicy policy,Monitor monitor,Double value) {
        WarnContent warnContent=new WarnContent();
        warnContent.setWarnSource(policy.getWarnSource());
        warnContent.setWarnDescription(policy.getWarnDescription());
        warnContent.setWarnContent("warning[LEVEL=LOG_TO_DB]:"+monitor.getMonitorName()+":value="+value+" "+policy.getCompareType()
                +"threshold="+ policy.getWarnThreshold());
    }


    private void logToTerminal(String monitorName,Double threshold,Double value,String compareType,String type){
        System.out.println("warning"+type+":"+monitorName+":value="+value+" "+compareType
                +"threshold="+ threshold);
    }
    @Scheduled(fixedRate = 600000)
    public void printCurrentTime() {
        //warning();
        System.out.println("当前系统时间：" + new java.util.Date());
    }


}
