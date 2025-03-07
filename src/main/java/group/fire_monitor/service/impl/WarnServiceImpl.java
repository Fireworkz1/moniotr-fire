package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.WarnEntityMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.WarnEntity;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddWarnForm;
import group.fire_monitor.service.WarnService;
import group.fire_monitor.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarnServiceImpl implements WarnService {
    @Autowired
    WarnPolicyMapper warnPolicyMapper;
    @Autowired
    MonitorMapper monitorMapper;
    @Autowired
    WarnEntityMapper warnEntityMapper;
    @Override
    @Transactional
    public void create(AddWarnForm form) {
        WarnPolicy warnPolicy=new WarnPolicy();
        warnPolicy.setWarnLevel(form.getWarnLevel());
        warnPolicy.setWarnSource(form.getWarnSource());
        warnPolicy.setWarnSourceType(form.getWarnSourceType());
        warnPolicy.setWarnDescription(form.getWarnDescription());
        warnPolicy.setWarnName(form.getWarnName());
        warnPolicy.setWarnThreshold(form.getWarnThreshold());
        warnPolicy.setCompareType(form.getCompareType());
        warnPolicy.setMonitorId(form.getMonitorId());
        warnPolicy.setMonitorName(form.getMonitorName());
        warnPolicy.setNoticeGroupIds(CommonUtil.listToString(form.getNoticeGroupIds()));
//        warnPolicy.setCurrentStatus(WarnNoticeEnum.SAFE.getLevel());
        warnPolicy.setMonitorOn(0);
        warnPolicy.setIsActive(0);
        warnPolicyMapper.insert(warnPolicy);
        //TODO:根据告警等级添加powerjob任务


    }

    @Override
    public List<WarnPolicy> selectLike(String str) {


        List<WarnPolicy> policies=warnPolicyMapper.selectPoliciesWithMonitorInstance(str)
//                .stream().filter(policy-> {
//                    return CommonUtil.stringToList(policy.getNoticeGroupIds()).contains(JWTUtil.getCurrentUser().getId());
//                })
//                .collect(Collectors.toList())
                ;
        return policies;

    }

    @Override
    @Transactional
    public void delete(Integer id) {
        warnEntityMapper.delete(new QueryWrapper<WarnEntity>().eq("warn_policy_id",id));
        warnPolicyMapper.deleteById(id);
    }


}
