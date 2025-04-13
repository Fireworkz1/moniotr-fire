package group.fire_monitor.controller;

import group.fire_monitor.pojo.Auto;
import group.fire_monitor.pojo.form.AutoGroupForm;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "5:自动化")
@RequestMapping("/auto")
public class AutoController {
    @Autowired
    private ResourceService resourceService;
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("创建自动化策略")
    public UniversalResponse<?> autocreate(@RequestBody Auto auto) {
        try{
            resourceService.automizationCreate(auto);
            return new UniversalResponse<>().success();
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
    public UniversalResponse<?> autoUpdate(@RequestBody Auto auto) {
        try{
            resourceService.automizationModify(auto);
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
}
