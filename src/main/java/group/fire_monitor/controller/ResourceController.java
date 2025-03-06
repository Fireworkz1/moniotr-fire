package group.fire_monitor.controller;

import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.ResourceCreateForm;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "2:资源管理")
@RequestMapping("/resource")
public class ResourceController {
    @Autowired
    ResourceService resourceService;
    @Autowired
    ResourceMapper resourceMapper;
    @Autowired
    MonitorMapper monitorMapper;
    /*
    * 测试服务器能否联通
    * */
    @PostMapping("/ping")
    @ResponseBody
    @ApiOperation("测试服务器连通性")
    public UniversalResponse<?> testPing(@RequestBody Resource resource) {
        return resourceService.testPing(resource);
    }

    /*
     * 服务器添加到资源列表
     * */
    @PostMapping("/addServer")
    @ResponseBody
    @ApiOperation("添加服务器资源")
    public UniversalResponse<?> addServer(@RequestBody ResourceCreateForm resourceCreateForm) {
        //提供name，ip，描述，账号，密码
        UniversalResponse<?> response= resourceService.testPing(resourceCreateForm.getResource());
        if(response.getCode()==500)return response;
        return resourceService.addServer(resourceCreateForm);
    }
    /*
     * 服务器添加到资源列表
     * */
    @PostMapping("/selectServer")
    @ResponseBody
    @ApiOperation("查询服务器资源")
    public UniversalResponse<?> selectServer(@RequestParam(required = false) String str) {

        return resourceService.selectServer(str);
    }
    @PostMapping("/selectServerById")
    @ResponseBody
    @ApiOperation("查询服务器资源Byid")
    public UniversalResponse<?> selectServerbyid(@RequestParam(required = false) Integer id) {
        Resource resource= resourceMapper.selectById(id);
        if(resource!=null){
            return new UniversalResponse<>().success(resource) ;
        }
        return new UniversalResponse<>(500,"找不到资源");
    }
    /*
    * 服务器从资源列表删除
    * */
    @PostMapping("/deleteServer")
    @ResponseBody
    @ApiOperation("删除服务器资源")
    public UniversalResponse<?> deleteServer(@RequestParam Integer id) {
        List<Monitor> monitorList= monitorMapper.selectList(null);
        for(Monitor monitor:monitorList){
            if(CommonUtil.stringToList(monitor.getMonitorResourceIds()).contains(id)){
                return new UniversalResponse<>(500,"服务器资源仍在被监控，请先调整监控");
            }
        }
        return resourceService.deleteServer(id);
    }
    /*
    * 测试软件资源是否可联通（是否可以通过prometheus检测到）
    * */
    @PostMapping("/testSoftware")
    @ResponseBody
    @ApiOperation("测试软件资源")
    public UniversalResponse<?> testdSoftware(@RequestBody Resource resource) {
        return resourceService.testSoftware(resource);
    }

    /*
    * 将软件资源添加到资源列表，并纳入prometheus监测
    * */
    @PostMapping("/addSoftware")
    @ResponseBody
    @ApiOperation("添加软件资源")
    public UniversalResponse<?> addSoftware(@RequestBody ResourceCreateForm resourceCreateForm) {
        //提供第二资源类型，端口，描述，名字.ip
        UniversalResponse<?> response=testdSoftware(resourceCreateForm.getResource());
        if(response.getCode()==500)return response;
        return resourceService.addSoftware(resourceCreateForm);
    }
    /*
    * 将软件资源从资源列表删除，并删除prometheus监测
    * */
    @PostMapping("/deleteSoftware")
    @ResponseBody
    @ApiOperation("删除软件资源")
    public UniversalResponse<?> deleteSoftware(@RequestParam Integer id) {
        List<Monitor> monitorList= monitorMapper.selectList(null);
        for(Monitor monitor:monitorList){
            if(CommonUtil.stringToList(monitor.getMonitorResourceIds()).contains(id)){
                return new UniversalResponse<>(500,"软件资源仍在被监控，请先调整监控");
            }
        }
        return new UniversalResponse<>().success(resourceService.deleteSoftware(id));
    }

    /*
     * 查询软件资源
     * */
    @PostMapping("/selectSoftware")
    @ResponseBody
    @ApiOperation("查询软件资源")
    public UniversalResponse<?> selectSoftware(@RequestParam(required = false) String str,@RequestParam(required = false) String type) {
        return resourceService.selectSoftware(str,type);
    }
    @PostMapping("/selectSoftwareById")
    @ResponseBody
    @ApiOperation("查询软件资源Byid")
    public UniversalResponse<?> selectSoftwarebyid(@RequestParam(required = false) Integer id) {
        Resource resource= resourceMapper.selectById(id);
        if(resource!=null){
            return new UniversalResponse<>().success(resource) ;
        }
        return new UniversalResponse<>(500,"找不到资源");
    }

    /*
     * 查询服务器资源
     * */
    @PostMapping("/selectSoftwareDetail")
    @ResponseBody
    @ApiOperation("查询具体某一软件")
    public UniversalResponse<?> selectSoftwareDetail(@RequestParam Integer id) {
        return resourceService.selectSoftwareDetail(id);
    }

    /*
     * 查询资源
     * */
    @PostMapping("/selectServerDetail")
    @ResponseBody
    @ApiOperation("查询具体某一服务器")
    public UniversalResponse<?> selectServerDetail(@RequestParam Integer id) {
        return resourceService.selectServerDetail(id);
    }

    @PostMapping("/selectBatch")
    @ResponseBody
    @ApiOperation("查询资源Byids")
    public UniversalResponse<?> selectResourceBatch(@RequestBody List<Integer> ids) {
        List<Resource> resources= resourceMapper.selectBatchIds(ids);
        if(resources!=null){
            return new UniversalResponse<>().success(resources) ;
        }
        return new UniversalResponse<>(500,"找不到资源");
    }
    /*
     * 修改资源
     * */
    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation("修改资源")
    public UniversalResponse<?> editResource(@RequestBody Resource resource) {
        try{
            resourceMapper.updateById(resource);
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }
    }
    /*
     * 停止docker程序
     * */
    @PostMapping("/software/docker/stop")
    @ResponseBody
    @ApiOperation("根据资源id停止docker容器")
    public UniversalResponse<?> stopContainer(@RequestParam Integer id) {
        try {
            return resourceService.stopContainer(id);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    /*
     * 重启docker程序
     * */
    @PostMapping("/software/docker/restart")
    @ResponseBody
    @ApiOperation("根据资源id重启docker容器")
    public UniversalResponse<?> restartContainer(@RequestParam Integer id) {
        return resourceService.restartDocker(id);
    }

    /*
     * 打开docker程序
     * */
    @PostMapping("/software/docker/start")
    @ResponseBody
    @ApiOperation("根据资源id启动docker容器")
    public UniversalResponse<?> startContainer(@RequestParam Integer id) {
        return resourceService.startDocker(id);
    }

    @PostMapping("/software/docker/details")
    @ResponseBody
    @ApiOperation("根据资源id查询docker容器信息")
    public UniversalResponse<?> containerDetails(@RequestParam Integer id) {
        return resourceService.dockerDetails(id);
    }


}
