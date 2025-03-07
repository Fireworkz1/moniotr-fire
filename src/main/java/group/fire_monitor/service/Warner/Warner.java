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

import java.security.Policy;
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
    @Autowired
    WarnEntityMapper warnEntityMapper;

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
        List<WarnPolicy> updateWarnPolicyList = Collections.synchronizedList(new ArrayList<>());
        List<WarnEntity> updateWarnEntityList = Collections.synchronizedList(new ArrayList<>());
        List<WarnEntity> deleteToHistoryList = Collections.synchronizedList(new ArrayList<>());
        List<WarnHistory> insertToHistoryList = Collections.synchronizedList(new ArrayList<>());
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
                    StringBuilder currentWarnTarget= new StringBuilder();
                    String flag = "safe";
                    for (PrometheusResult result : resultList) {
                        Double value = Double.parseDouble((String) result.getValue().get(1));
                        //通过flag判断要不要告警
                        if (CommonUtil.needToWarn(policy.getCompareType(), value, policy.getWarnThreshold())) {
                            //获取当前正在告警的对象的子对象（一个resource可能有多个分区告警）
                            flag = "active";
                            currentWarnTarget.append(result.getMetric().getInstance()).append(" value=").append(value).append(";\n");
                        }
                    }

                    if (flag.equals("active")) {
                        //如果需要告警，更新数据库信息
                        if(policy.getIsActive()==0) {policy.setIsActive(1); updateWarnPolicyList.add(policy);}
                        //如果当前policy需要告警，进行告警操作；0为忽略，1为输出到终端（后台），2为存储进数据库，3为通知用户（邮件等），4为在系统中通知
                        WarnEntity warnEntity;
                        //获取告警实体WarnEntity
                           warnEntity= warnEntityMapper.selectOne(new QueryWrapper<WarnEntity>().eq("warn_policy_id",policy.getId()));
                           if(warnEntity==null){
                               warnEntity=new WarnEntity();
                               BeanUtils.copyProperties(policy,warnEntity);
                               warnEntity.setWarnPolicyId(policy.getId());
                               warnEntity.setId(null);
                               warnEntity.setWarnRepeatTimes(0);
                               warnEntity.setCurrentStatus("正在告警中");
                               warnEntity.setStartWarningTime(new Date());
                               warnEntity.setHasSentNotice(0);
                               warnEntity.setHasIgnored(0);
                               warnEntityMapper.insert(warnEntity);
                           }

                           if(warnEntity.getHasIgnored()==0){
                               //如果告警未被忽略则更新状态，并生成日志；如果已经被忽略则不做这些操作
                              Integer warnEntityId= warnEntity.getId();
                               BeanUtils.copyProperties(policy,warnEntity);
                               warnEntity.setCurrentWarningTarget(currentWarnTarget.toString());
                               warnEntity.setId(warnEntityId);
                               warnEntity.setLastWarningTime(new Date());
                               warnEntity.setWarnRepeatTimes(warnEntity.getWarnRepeatTimes()+1);
                               updateWarnEntityList.add(warnEntity);

                               // 如果需要告警，发送告警
                               switch (warnEntity.getWarnLevel()) {
                                   case 0:
                                       break;
                                   case 1:
                                       logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), currentWarnTarget.toString(), policy.getCompareType(), "[LOG_ONLY]");
                                       break;
                                   case 2:
                                       logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), currentWarnTarget.toString(), policy.getCompareType(), "[LOG_TO_DB]");
                                       logToDB(policy, monitor, currentWarnTarget.toString());
                                       break;
                                   case 3:
                                       logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), currentWarnTarget.toString(), policy.getCompareType(), "[NOTICE_USER]");
                                       logToDB(policy, monitor, currentWarnTarget.toString());

                                       if (warnEntity.getHasSentNotice() == 0) {
                                           sendToConsole(warnEntity,currentWarnTarget.toString());
                                           sendNotice(warnEntity, monitor, currentWarnTarget.toString());
                                           warnEntity.setHasSentNotice(1);
                                       }

                                       break;
                                   case 4:
                                       logToTerminal(monitor.getMonitorName(), policy.getWarnThreshold(), currentWarnTarget.toString(), policy.getCompareType(), "[SEND_CONSOLE]");
                                       logToDB(policy, monitor, currentWarnTarget.toString());
                                       if (warnEntity.getHasSentNotice() == 0) {
                                           sendToConsole(warnEntity,currentWarnTarget.toString());
                                           warnEntity.setHasSentNotice(1);
                                       }
                                       break;
                                   default:
                                       throw new RuntimeException("错误告警级别");
                               }
                           }
                    } else {
                        //如果flag不为active，当前policy不需要告警。如果之前也不是active则忽略，如果之前不是则修改为正常逻辑
                        if (policy.getIsActive() == 1) {
                            policy.setIsActive(0);
                            updateWarnPolicyList.add(policy);
                            WarnEntity entity=warnEntityMapper.selectOne(new QueryWrapper<WarnEntity>().eq("warn_policy_id",policy.getId()));
                            if(entity!=null){
                                if(entity.getHasIgnored()==1){
                                    //被忽略的对象一开始就加入了告警历史，所以这里直接删除
                                    deleteToHistoryList.add(entity);
                                }else{
                                    //未被忽略的对象一开始就加入了告警历史，所以这里直接删除
                                    deleteToHistoryList.add(entity);
                                    WarnHistory history= new WarnHistory();
                                    BeanUtils.copyProperties(entity,history);
                                    history.setId(null);
                                    history.setWarnEntityId(entity.getId());
                                    history.setCurrentStatus("自动恢复");
                                    insertToHistoryList.add(history);
                                }
                            }
                        }
                    }
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

        //
        // 更新全部策略
        warnPolicyMapper.updateById(updateWarnPolicyList);
        warnEntityMapper.updateById(updateWarnEntityList);
        for(WarnEntity entity:deleteToHistoryList){
            warnEntityMapper.deleteById(entity);
        }
        warnHistoryMapper.insert(insertToHistoryList);
        System.out.println("此轮告警处理已完成");
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

    private void sendNotice(WarnEntity entity,Monitor monitor,String target) {
        switch (entity.getNoticeWay()){
            case "email":
                sendEmail(entity,monitor,target);
                break;
            case "message":
                ;
                break;
            default:
                System.out.println("不支持方式:"+entity.getNoticeWay());
        }
    }

    private void sendEmail(WarnEntity entity, Monitor monitor, String target) {
        System.out.println("发送邮件逻辑仍未完成");
//        String title="fire-monitor监控系统告警信息";
//        String content="warning[LEVEL=SEND_NOTICE]:"+monitor.getMonitorName()+":value="+value+" "+policy.getCompareType()
//                +"threshold="+ policy.getWarnThreshold();
//        EmailSender emailSender = new EmailSender();
//        List<Integer> userIds= (List<Integer>) CommonUtil.stringToList(policy.getNoticeGroupIds());
//        List<User> userList= userMapper.selectBatchIds(userIds);
//        for(User user:userList){
//            emailSender.sendEmail(user.getEmail(),title,content);
//        }


    }

    private void logToDB(WarnPolicy policy,Monitor monitor,String target) {
        WarnContent warnContent=new WarnContent();
        warnContent.setWarnSource(policy.getWarnSource());
        warnContent.setWarnDescription(policy.getWarnDescription());
        warnContent.setWarnContent("warning[LEVEL=LOG" +
                "_TO_DB]:"+monitor.getMonitorName()+":target="+target+" "+policy.getCompareType()
                +"threshold="+ policy.getWarnThreshold());
        warnContent.setWarnLevel(policy.getWarnLevel());
        warnContent.setWarnName(policy.getWarnName());
        warnContent.setWarnSourceType(policy.getWarnSourceType());
        warnContent.setWarnSource(policy.getWarnSource());
        warnContent.setWarnTime(new Date());
        warnContentMapper.insert(warnContent);
    }


    private void logToTerminal(String monitorName,Double threshold,String target,String compareType,String type){
        System.out.println("warning"+type+":"+monitorName+":target="+target+" "+compareType
                +"threshold="+ threshold);
    }
//    @Scheduled(fixedRate = 600000)
//    public void printCurrentTime() {
//        //warning();
////        System.out.println("当前系统时间：" + new java.util.Date());
//    }
private void sendToConsole(WarnEntity entity,String target){
//TODO:发送到控制台页面
}

}
