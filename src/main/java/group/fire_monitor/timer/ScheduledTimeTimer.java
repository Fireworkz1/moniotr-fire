package group.fire_monitor.timer;

import group.fire_monitor.service.ResourceService;
import group.fire_monitor.service.Warner.Warner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTimeTimer {
    @Autowired
    private Warner warner;
    @Autowired
    private ResourceService resourceService;
    //监测定时项目
    @Scheduled(fixedRate = 120000)
    public void checkWarning() {
        try {
            System.out.println("当前系统时间：" + new java.util.Date() + "，正在告警侦测中...");
            warner.warning();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 30000)
    public void checkAuto() {
        try {
            System.out.println("当前系统时间：" + new java.util.Date() + "，正在侦测自动化对象...");
            warner.checkAuto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Scheduled(fixedRate = 300000)
    public void checkResourceUp(){
        try {
            System.out.println("当前系统时间：" + new java.util.Date() +"，正在监测资源在线情况...");
            resourceService.checkResourceActivity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
