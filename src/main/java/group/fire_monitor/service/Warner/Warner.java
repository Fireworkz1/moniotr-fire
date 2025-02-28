package group.fire_monitor.service.Warner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.*;
import group.fire_monitor.pojo.*;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.service.emailsender.EmailSender;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.enums.WarnNoticeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Component
public class Warner {
    @Autowired
    WarnPolicyMapper warnPolicyMapper;
    @Autowired
    MonitorService monitorService;
    @Autowired
    MonitorMapper monitorMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    WarnHistoryMapper warnHistoryMapper;
    @Autowired
    WarnContentMapper warnContentMapper;

    private final ExecutorService executorService;

    public Warner() {
        // 创建一个固定大小的线程池，大小可以根据需求调整
        this.executorService = Executors.newFixedThreadPool(1);
    }




    @Scheduled(fixedRate = 120000)
    public void checkWarning() {
        try {
            System.out.println("当前系统时间：" + new java.util.Date() + "，正在告警侦测中...");
            warning();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void warning() {
        List<WarnPolicy> warnTargetList = warnPolicyMapper.selectList(new QueryWrapper<WarnPolicy>().eq("monitor_on", 1));
        List<WarnPolicy> updateWarnList = Collections.synchronizedList(new ArrayList<>());
        List<WarnPolicy> moveToHistoryList = Collections.synchronizedList(new ArrayList<>());
        System.out.println("当前监控monitor数量：" + warnTargetList.size());

        // 创建一个列表来保存所有异步任务的CompletableFuture
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (WarnPolicy policy : warnTargetList) {
            // 创建一个CompletableFuture任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Monitor monitor = monitorMapper.selectById(policy.getMonitorId());
                    List<PrometheusResult> resultList = fetchMonitorDataWithTimeout(monitor);
                    if (resultList == null) {
                        // 如果获取数据失败，直接忽略本次操作
                        System.out.println("获取监控数据失败，忽略本次操作。");
                        return;
                    }
                    String currentWarnTarget="";
                    String flag = "safe";
                    for (PrometheusResult result : resultList) {
                        Double value = Double.parseDouble((String) result.getValue().get(1));

                        // 处理告警逻辑
                        if (CommonUtil.needToWarn(policy.getCompareType(), value, policy.getWarnThreshold())) {
                            flag = "active";
                            currentWarnTarget+= result.getMetric().getInstance() +" value="+value+";\n";
                            System.out.println("当前规则正在告警，warnId=" + policy.getId() + "，policyName=" + policy.getWarnName() + " metric=" + result.getMetric());
                            // 如果需要告警
                            switch (policy.getWarnLevel()) {
                                case 0:
                                    break;
                                case 1:
                                    logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(), "[LOG_ONLY]");
                                    break;
                                case 2:
                                    logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(), "[LOG_TO_DB]");
                                    logToDB(policy, monitor, value);
                                    break;
                                case 3:
                                    logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), value, policy.getCompareType(), "[NOTICE_USER]");
                                    logToDB(policy, monitor, value);
                                    if (policy.getHasSentNotice() == 0) {
                                        sendNotice(policy, monitor, value);
                                        policy.setHasSentNotice(1);
                                    }
                                    break;
                                default:
                                    throw new RuntimeException("错误告警级别");
                            }
                        }
                    }

                    if (flag.equals("active")) {
                        policy.setIsActive(1);
                        policy.setHasSentNotice(1);
                        policy.setWarnRepeatTimes(policy.getWarnRepeatTimes() + 1);
                        Date date = new Date();
                        if (policy.getStartWarningTime() == null)
                            policy.setStartWarningTime(date);
                        policy.setLastWarningTime(date);
                    } else {
                        if (policy.getIsActive() == 1) {
                            moveToHistoryList.add(policy);
                            policy.setIsActive(0);
                            policy.setHasSentNotice(0);
                            policy.setWarnRepeatTimes(0);
                            policy.setLastWarningTime(null);
                            policy.setStartWarningTime(null);
                        }
                    }
                    policy.setCurrentWarnTarget(currentWarnTarget);
                    System.out.println("当前规则warnId=" + policy.getId() + "，已经处理完毕" + new Date());
                    updateWarnList.add(policy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有CompletableFuture任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.get(); // 等待所有任务完成
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // 所有任务完成后打印 updateWarnList
        System.out.println(updateWarnList);

        // 更新告警策略
        updateWarnList.forEach(policy -> warnPolicyMapper.updateById(policy));

        // 将告警移动到历史记录
        List<WarnHistory> warnHistoryList = new ArrayList<>();
        for (WarnPolicy policyhis : moveToHistoryList) {
            WarnHistory history = new WarnHistory();
            BeanUtils.copyProperties(policyhis, history);
            history.setWarnPolicyId(history.getId());
            history.setId(null);
            warnHistoryList.add(history);
        }
        warnHistoryMapper.insert(warnHistoryList);
    }

    private List<PrometheusResult> fetchMonitorDataWithTimeout(Monitor monitor) {
        try {
            // 使用 CompletableFuture 设置超时
            CompletableFuture<List<PrometheusResult>> future = CompletableFuture.supplyAsync(() ->
                    monitorService.getSingleMonitorData(monitor)
            );
            return future.get(15, TimeUnit.SECONDS); // 超时时间为 15 秒
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
        } catch (InterruptedException | TimeoutException e) {
            System.out.println("请求超时，忽略本次操作。");
        }
        return null;
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
        System.out.println("发送邮件逻辑仍未完成");
//        String title="fire-monitor监控系统告警信息";
//        String content="warning[LEVEL=SEND_NOTICE]:"+monitor.getMonitorName()+":value="+value+" "+policy.getCompareType()
//                +"threshold="+ policy.getWarnThreshold();
//        EmailSender emailSender = new EmailSender();
//        List<Integer> userIds= (List<Integer>) CommonUtil.stringToList(policy.getNoticeUserIds());
//        List<User> userList= userMapper.selectBatchIds(userIds);
//        for(User user:userList){
//            emailSender.sendEmail(user.getEmail(),title,content);
//        }


    }

    private void logToDB(WarnPolicy policy,Monitor monitor,Double value) {
        WarnContent warnContent=new WarnContent();
        warnContent.setWarnSource(policy.getWarnSource());
        warnContent.setWarnDescription(policy.getWarnDescription());
        warnContent.setWarnContent("warning[LEVEL=LOG_TO_DB]:"+monitor.getMonitorName()+":value="+value+" "+policy.getCompareType()
                +"threshold="+ policy.getWarnThreshold());
        warnContent.setWarnLevel(policy.getWarnLevel());
        warnContent.setWarnName(policy.getWarnName());
        warnContent.setWarnSourcetype(policy.getWarnSourceType());
        warnContent.setWarnSource(policy.getWarnSource());
        warnContent.setWarnTime(new Date());
        warnContentMapper.insert(warnContent);
    }


    private void logToTerminal(String monitorName,Double threshold,Double value,String compareType,String type){
        System.out.println("warning"+type+":"+monitorName+":value="+value+" "+compareType
                +"threshold="+ threshold);
    }
//    @Scheduled(fixedRate = 600000)
//    public void printCurrentTime() {
//        //warning();
////        System.out.println("当前系统时间：" + new java.util.Date());
//    }


}
