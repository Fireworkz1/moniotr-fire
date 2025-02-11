package group.fire_monitor.controller;

import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.ResourceCreateForm;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "2:资源管理")
@RequestMapping("/resource")
public class ResourceController {
    @Autowired
    ResourceService resourceService;
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
    @ApiOperation("获取服务器资源")
    public UniversalResponse<?> selectServer(@RequestParam(required = false) String str) {

        return resourceService.selectServer(str);
    }
    /*
    * 服务器从资源列表删除
    * */
    @PostMapping("/deleteServer")
    @ResponseBody
    @ApiOperation("删除服务器资源")
    public UniversalResponse<?> deleteServer(@RequestParam Integer id) {

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

        return resourceService.deleteSoftware(id);
    }

    /*
     * 将软件资源从资源列表删除，并删除prometheus监测
     * */
    @PostMapping("/selectSoftware")
    @ResponseBody
    @ApiOperation("查询软件资源")
    public UniversalResponse<?> selectSoftware(@RequestParam(required = false) String str) {
        return resourceService.selectSoftware(str);
    }


}
