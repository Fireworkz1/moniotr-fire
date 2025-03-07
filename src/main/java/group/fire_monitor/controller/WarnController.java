package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.WarnEntityMapper;
import group.fire_monitor.mapper.WarnHistoryMapper;
import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.*;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.AddWarnForm;
import group.fire_monitor.pojo.form.WarnPolicyUpdateForm;
import group.fire_monitor.pojo.res.WarnPolicyRes;
import group.fire_monitor.service.WarnService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    @Autowired
    private WarnEntityMapper warnEntityMapper;
    @Autowired
    private UserController userController;
    @PostMapping("/createPolicy")
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

    @PostMapping("/updatePolicy")
    @ResponseBody
    @ApiOperation("修改告警策略")
    public UniversalResponse<?> update(@RequestBody WarnPolicyUpdateForm form) {
        try{
            WarnPolicy policy= warnPolicyMapper.selectById(form.getId());
            BeanUtils.copyProperties(form,policy);
            policy.setNoticeGroupIds(CommonUtil.listToString(form.getNoticeGroupIds()));
            warnPolicyMapper.updateById(policy);
            return new UniversalResponse<>().success();
        }catch (RuntimeException e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }




    @PostMapping("/deletePolicy")
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

    @PostMapping("/getWarnPolicy")
    @ResponseBody
    @ApiOperation("查询告警策略（告警中优先）")
    public UniversalResponse<?> selectByName(@RequestParam(required = false) String str) {
        try{
            List<WarnPolicy> policies= warnService.selectLike(str);
            List<WarnPolicyRes> resList=new ArrayList<>();
            for(WarnPolicy warnPolicy:policies){
                WarnPolicyRes res=new WarnPolicyRes();
                BeanUtils.copyProperties(warnPolicy,res);
                res.setNoticeGroupIds(CommonUtil.stringToList(warnPolicy.getNoticeGroupIds()));
                resList.add(res);
            }
            return new UniversalResponse<>().success(resList);
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
            if(monitorOn==0){
                policie.setIsActive(0);
            }
            warnPolicyMapper.updateById(policie);
            if(monitorOn==0){
                WarnEntity warnEntity= warnEntityMapper.selectOne(new QueryWrapper<WarnEntity>().eq("warn_policy_id",policie.getId()));
                if(warnEntity!=null){
                    WarnHistory warnHistory=new WarnHistory();
                    BeanUtils.copyProperties(warnEntity,warnHistory);
                    warnEntityMapper.deleteById(warnEntity);
                    warnHistory.setCurrentStatus("因停止监控移入历史告警");
                    warnHistoryMapper.insert(warnHistory);
                }
            }
            return new UniversalResponse<>().success();
        }catch (RuntimeException e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }


    @PostMapping("/warnentity")
    @ResponseBody
    @ApiOperation("查看历史")
    public UniversalResponse<?> getEntity(@RequestParam(required = false) String str,@RequestParam(required = false)Integer onlyUser) {

        QueryWrapper<WarnEntity>wrapper= new QueryWrapper<>();
        if (str!=null&&!str.isEmpty()){
            wrapper .like("warn_name",str)
                    .like("warn_description",str);
        }

        wrapper.orderByDesc("last_warning_time")
                .eq("has_ignored",0);
        List<WarnEntity> entities=warnEntityMapper.selectList(wrapper);
        if(onlyUser!=null&&onlyUser==1){
            UniversalResponse response=userController.group();
            if(response.getCode()==500)return response;
            List<Group> groupList= (List<Group>) response.getData();
            List<Integer> groupIdList=groupList.stream().map(Group::getId).collect(Collectors.toList());
            List<WarnEntity> filteredEntities=new ArrayList<>();
            for(WarnEntity entity:entities){
                List<Integer> entityGroupIdList=CommonUtil.stringToList(entity.getNoticeGroupIds());
                if(CommonUtil.hasIntersection(groupIdList,entityGroupIdList)){
                    filteredEntities.add(entity);
                }
            }
            return new UniversalResponse<>().success(filteredEntities);
        }else{
            return new UniversalResponse<>().success(entities);
        }

    }

    @PostMapping("/warnhistory")
    @ResponseBody
    @ApiOperation("查看历史")
    public UniversalResponse<?> getHistory(@RequestParam(required = false) String str) {

        QueryWrapper<WarnHistory>wrapper= new QueryWrapper<>();
        if (str!=null&&!str.isEmpty()){
            wrapper .like("warn_name",str)
                    .like("warn_description",str);
        }

                wrapper.orderByDesc("last_warning_time");
        List<WarnHistory> histories=warnHistoryMapper.selectList(wrapper);

        return new UniversalResponse<>().success(histories);
    }
}
