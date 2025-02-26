package group.fire_monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.annotation.JWTPass;
import group.fire_monitor.mapper.GroupMapper;
import group.fire_monitor.mapper.RelationGroupUserMapper;
import group.fire_monitor.mapper.UserMapper;
import group.fire_monitor.pojo.Group;
import group.fire_monitor.pojo.RelationGroupUser;
import group.fire_monitor.pojo.User;
import group.fire_monitor.pojo.form.*;
import group.fire_monitor.pojo.res.GroupInfoRes;
import group.fire_monitor.pojo.res.GroupUserRelation;
import group.fire_monitor.service.UserService;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.util.enums.PermissionLevelEnum;
import group.fire_monitor.util.enums.ResponseEnum;
import group.fire_monitor.util.response.UniversalResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Api(tags = "1:注册登录和账号管理")
@RequestMapping("/account")
public class UserController {
    @Resource
    private UserService userService;

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RelationGroupUserMapper groupUserMapper;

    @GetMapping
    @ResponseBody
//    @JWTPass
    public UniversalResponse<Object> testToken() {
        return new UniversalResponse<>(1, "OK");
    }

    /**
     * 将登录，注册操作封装到CommonService（lmpl）接口之中
     * 在登录后，将token和identity传给前端，用以登录时区分跳转到不同用户界面
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    @ResponseBody
    @JWTPass
    public UniversalResponse<?> login(@RequestBody LoginForm loginForm) {
        try{
            return userService.login(loginForm);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    /*
    * 注册
    * */

    @PostMapping("/register")
    @ApiOperation("注册")
    @ResponseBody
    @JWTPass
    public UniversalResponse<?> register(@RequestBody RegisterForm registerForm) {
        return userService.register(registerForm);
    }

    /*
    * 更改人员权限（仅admin）
    * */
    @PostMapping("changePermissionLevel")
    @ApiOperation("更改人员权限(暂时不使用)")
    @ResponseBody
    public  UniversalResponse<?> changePermission(@RequestBody ChangePermissionForm changePermissionForm){
        return null;
    }

    /*
    * 创建分组
    * */
    @PostMapping("createGroup")
    @ApiOperation("创建分组")
    @Transactional
    @ResponseBody
    public  UniversalResponse<?> createGroup(@RequestBody CreateGroupForm createGroupForm){
        if(!Objects.equals(JWTUtil.getCurrentUser().getPermissionLevel(), PermissionLevelEnum.ADMIN.getPermissionLevel())){
            return new UniversalResponse<>(500,"权限不足");
        }
        try{
            QueryWrapper<Group> wrapper=new QueryWrapper<>();
            wrapper.eq("name",createGroupForm.getGroupName());
            if(!groupMapper.selectList(wrapper).isEmpty()){
                return new UniversalResponse<>(500,"名字重复");
            }
            Group group=new Group();
            group.setGroupLeaderId(createGroupForm.getGroupLeaderId());
            group.setName(createGroupForm.getGroupName());
            group.setDefaultPermissionLevel(createGroupForm.getPermissionLevel());

            groupMapper.insert(group);
            Integer groupId=group.getId();
            RelationGroupUser groupUser=new RelationGroupUser();
            groupUser.setGroup_id(groupId);
            for(Integer userId:createGroupForm.getUserIds()){
                groupUser.setId(null);
                groupUser.setUser_id(userId);
                groupUserMapper.insert(groupUser);
            }
            return new UniversalResponse<>(200,"success");
        }catch (Exception e){
            e.printStackTrace();
            return new UniversalResponse<>(500,e.getMessage());
        }
    }
    /*
    * 设置人员分组
    * */
    @PostMapping("changeGroupMember")
    @ApiOperation("更改分组中人员")
    @ResponseBody
    public  UniversalResponse<?> changeGroup(@RequestBody ChangeGroupMemberForm changeGroupMemberForm){
        User currentUser=JWTUtil.getCurrentUser();
        if (groupMapper.selectById(changeGroupMemberForm.getGroupId())==null)
            return new UniversalResponse<>(500,"不存在分组");
        Group group=groupMapper.selectById(changeGroupMemberForm.getGroupId());
        if(!(Objects.equals(currentUser.getPermissionLevel(), PermissionLevelEnum.ADMIN.getPermissionLevel()))&&
            group.getGroupLeaderId()!=currentUser.getId()){
            return new UniversalResponse<>(500,"您没有权限修改分组");
        }
        try{
            QueryWrapper<RelationGroupUser> wrapper=new QueryWrapper<>();
            wrapper.eq("group_id",changeGroupMemberForm.getGroupId());
            groupUserMapper.delete(wrapper);
            for(Integer userId:changeGroupMemberForm.getUserIdList()){
                RelationGroupUser relationGroupUser=new RelationGroupUser();
                relationGroupUser.setGroup_id(changeGroupMemberForm.getGroupId());
                relationGroupUser.setUser_id(userId);
                groupUserMapper.insert(relationGroupUser);
            }
            return new UniversalResponse<>().success();
        }catch (Exception e){
            e.printStackTrace();
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    /*
    * 查询人员分组
    * */
    @GetMapping("selectGroupInfo")
    @ApiOperation("查询全部分组信息")
    @ResponseBody
    public  UniversalResponse<?> groupInfo(){
        try{
            List<Group>groupList=groupMapper.selectList(null);
            GroupInfoRes res=new GroupInfoRes();
            List<GroupUserRelation> relationList = new ArrayList<>();
            for(Group group:groupList){
                List<User> users=groupUserMapper.selectUsersByGroupId(group.getId());
                GroupUserRelation relation=new GroupUserRelation();
                relation.setGroup(group);
                relation.setUsers(users);
                relationList.add(relation);
            }
            return new UniversalResponse<>().success(relationList);
        }catch (Exception e){
            e.printStackTrace();
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    /*
     * 查询全部人员
     * */
    @GetMapping("selectUser")
    @ApiOperation("查询全部人员")
    @ResponseBody
    public  UniversalResponse<?> user(){
        try{
            List<User> userList=userMapper.selectList(null);
            return new UniversalResponse<>(200,"success",userList);
        }catch (Exception e){
            e.printStackTrace();
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @GetMapping("selectGroup")
    @ApiOperation("查询当前用户属于分组")
    @ResponseBody
    public  UniversalResponse<?> group(){
        try{
            return new UniversalResponse<>().success(groupMapper.selectList(new QueryWrapper<Group>().eq("user_id",JWTUtil.getCurrentUser().getId()))) ;
        }catch (Exception e){
            e.printStackTrace();
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @GetMapping("current")
    @ApiOperation("返回当前用户信息")
    @ResponseBody
    public UniversalResponse<?> user_(){
        try {
            return new UniversalResponse<>().success(userMapper.selectById(JWTUtil.getCurrentUser().getId()));
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @GetMapping("changeUserInfo")
    @ApiOperation("修改当前用户信息")
    @ResponseBody
    public UniversalResponse<?> changeuser_(User user){
        try {
            return new UniversalResponse<>().success(userMapper.updateById(user));
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }
}
