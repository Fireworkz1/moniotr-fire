package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddWarnForm;
import group.fire_monitor.service.WarnService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.util.enums.WarnNoticeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WarnServiceImpl implements WarnService {
    @Autowired
    WarnPolicyMapper warnPolicyMapper;
    @Autowired
    MonitorMapper monitorMapper;
    @Override
    @Transactional
    public void create(AddWarnForm form) {
        WarnPolicy warnPolicy=new WarnPolicy();
        warnPolicy.setWarnLevel(form.getWarnLevel());
        warnPolicy.setWarnSource(form.getWarnSource());
        warnPolicy.setWarnDescription(form.getWarnDescription());
        warnPolicy.setWarnName(form.getWarnName());
        warnPolicy.setWarnThreshold(form.getWarnThreshold());
        warnPolicy.setCompareType(form.getCompareType());
        warnPolicy.setMonitorId(form.getMonitorId());
        warnPolicy.setMonitorName(form.getMonitorName());
        warnPolicy.setNoticeUserIds(CommonUtil.listToString(form.getNoticeUserIds()));
//        warnPolicy.setCurrentStatus(WarnNoticeEnum.SAFE.getLevel());
        warnPolicy.setLastWarningTime(null);
        warnPolicy.setStartWarningTime(null);
        warnPolicy.setMonitorOn(0);
        warnPolicy.setIsActive(0);
        warnPolicy.setHasSentNotice(0);
        warnPolicyMapper.insert(warnPolicy);
        //TODO:根据告警等级添加powerjob任务


    }

    @Override
    public List<WarnPolicy> selectLike(String str) {
        QueryWrapper<WarnPolicy> wrapper=new QueryWrapper<>();
        wrapper.like("warn_name",str)
                .like("warn_description",str)
                .orderByDesc("monitor_on").orderByDesc("is_active")
                ;
        List<WarnPolicy> policies=warnPolicyMapper.selectList(wrapper)
                .stream().filter(policy-> {
                    return CommonUtil.stringToList(policy.getNoticeUserIds()).contains(JWTUtil.getCurrentUser().getId());
                })
                .collect(Collectors.toList());
        return policies;

    }

    @Override
    @Transactional
    public void delete(Integer id) {

        warnPolicyMapper.deleteById(id);
    }


}
