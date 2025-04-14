package group.fire_monitor.controller;

import group.fire_monitor.mapper.AutoGroupMapper;
import group.fire_monitor.pojo.Auto;
import group.fire_monitor.pojo.AutoGroup;
import group.fire_monitor.pojo.form.AutoGroupForm;
import group.fire_monitor.pojo.res.AutoRes;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "5:自动化")
@RequestMapping("/auto")
public class AutoController {
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private AutoGroupMapper autoGroupMapper;
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("创建自动化策略")
    public UniversalResponse<?> autocreate(@RequestBody AutoRes autoRes) {
        try{
            Auto auto=new Auto();
            BeanUtils.copyProperties(autoRes,auto);
            auto.setTargetIds(CommonUtil.listToString(autoRes.getTargetIds()));

            return new UniversalResponse<>().success(resourceService.automizationCreate(auto));
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }

    @PostMapping("/createGroup")
    @ResponseBody
    @ApiOperation("创建规则组策略")
    public UniversalResponse<?> autocreategroup(@RequestBody AutoGroupForm autoGroupForm) {
        try{
            resourceService.automizationCreateGroup(autoGroupForm);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("删除自动化策略")
    public UniversalResponse<?> autoDelete(@RequestParam Integer autoId) {
        try{
            resourceService.automizationDelete(autoId);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }
    @PostMapping("/deleteGroup")
    @ResponseBody
    @ApiOperation("删除规则组策略")
    public UniversalResponse<?> autogroupDelete(@RequestParam Integer autoGroupId) {
        try{
            resourceService.automizationDeleteGroup(autoGroupId);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }
    @PostMapping("/update")
    @ResponseBody
    @ApiOperation("修改自动化策略")
    public UniversalResponse<?> autoUpdate(@RequestBody AutoRes auto) {
        try{
            Auto auto1=new Auto();
            BeanUtils.copyProperties(auto,auto1);
            auto1.setTargetIds(CommonUtil.listToString(auto.getTargetIds()));
            resourceService.automizationModify(auto1);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }

    @PostMapping("/updateGroup")
    @ResponseBody
    @ApiOperation("修改自动化策略")
    public UniversalResponse<?> autoUpdateGroup(@RequestBody AutoGroupForm autoGroupForm) {
        try{
            AutoGroup autoGroup=new AutoGroup();
            BeanUtils.copyProperties(autoGroupForm,autoGroup);
            autoGroup.setResourceIds(CommonUtil.listToString(autoGroupForm.getResourceIds()));
            autoGroup.setAutoPolicyIds(CommonUtil.listToString(autoGroupForm.getAutoPolicyIds()));
            autoGroupMapper.updateById(autoGroup);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }
    @PostMapping("/select")
    @ResponseBody
    @ApiOperation("查询自动启停")
    public UniversalResponse<?> autoSelect(@RequestParam(required = false) String str) {
        try{
            return new UniversalResponse<>().success(resourceService.automizationSelect(str));
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }
    @PostMapping("/selectGroupPolicy")
    @ResponseBody
    @ApiOperation("查询规则组规则")
    public UniversalResponse<?> autoSelectGroup(@RequestParam Integer groupId) {
        try{
            return new UniversalResponse<>().success(resourceService.automizationSelectGroupPolicy(groupId));
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }
    @PostMapping("/selectGroup")
    @ResponseBody
    @ApiOperation("查询规则组")
    public UniversalResponse<?> autoSelectGroup(@RequestParam(required = false) String str) {
        try{
            return new UniversalResponse<>().success(resourceService.automizationSelectGroup(str));
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }
    @PostMapping("/selectGroupById")
    @ResponseBody
    @ApiOperation("查询规则组")
    public UniversalResponse<?> autoSelectGroupById(@RequestParam Integer id) {
        try{
            AutoGroup group=autoGroupMapper.selectById(id);
            AutoGroupForm form=new AutoGroupForm();
            BeanUtils.copyProperties(group,form);
            form.setAutoPolicyIds(CommonUtil.stringToList(group.getAutoPolicyIds()));
            form.setResourceIds(CommonUtil.stringToList(group.getResourceIds()));
            return new UniversalResponse<>().success(form);
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }

    @PostMapping("/changeStatus")
    @ResponseBody
    @ApiOperation("切换自动化策略状态")
    public UniversalResponse<?> autoChangeStatus(@RequestParam Integer autoId) {
        try{
            resourceService.automizationChangeStatus(autoId);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }
    @PostMapping("/changeStatusGroup")
    @ResponseBody
    @ApiOperation("切换组策略状态")
    public UniversalResponse<?> autoChangeStatusGroup(@RequestParam Integer autoGroupId) {
        try{
            resourceService.automizationChangeGroupStatus(autoGroupId);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }

    }
}
