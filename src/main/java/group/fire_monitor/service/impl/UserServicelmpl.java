package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import group.fire_monitor.mapper.UserMapper;
import group.fire_monitor.pojo.User;
import group.fire_monitor.pojo.form.LoginForm;
import group.fire_monitor.pojo.form.RegisterForm;
import group.fire_monitor.pojo.res.TokenRes;
import group.fire_monitor.service.UserService;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.util.enums.PermissionLevelEnum;
import group.fire_monitor.util.enums.ResponseEnum;
import group.fire_monitor.util.response.ResponseException;
import group.fire_monitor.util.response.UniversalResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServicelmpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param loginForm 登录表单
     * @return
     */
    @Override
    public UniversalResponse<TokenRes> login(LoginForm loginForm) {
        QueryWrapper<User> wrapper=new QueryWrapper<>();
        wrapper.eq("account",loginForm.getAccount());
        List<User> users=userMapper.selectList(wrapper);
        User user = users.get(0);
        if (user == null) {
            throw new ResponseException(ResponseEnum.USER_LOGIN_ERROR.getCode(), ResponseEnum.USER_LOGIN_ERROR.getMsg());
        }
        if (!user.getPassword().equals(loginForm.getPassword())) {
            throw new ResponseException(ResponseEnum.USER_LOGIN_ERROR.getCode(), ResponseEnum.USER_LOGIN_ERROR.getMsg());
        }
        // 用户名、密码正确
        // 生成token
        String token;
        token = JWTUtil.createToken(user.getId().toString());
        return new UniversalResponse<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), new TokenRes(token));
    }

    /**
     * 用户注册
     *
     * @param registerForm
     * @return
     */
    @Override
    public UniversalResponse<?> register(RegisterForm registerForm) {
        User newUser = new User();
//        newUser.setId(null);
        newUser.setName(registerForm.getName());
        newUser.setPermissionLevel(registerForm.getPermissionLevel());
        if (!CommonUtil.hasValue(newUser.getPermissionLevel())||newUser.getPermissionLevel()==0){
            newUser.setPermissionLevel(PermissionLevelEnum.NORMAL.getPermissionLevel());
        }
        newUser.setAccount(registerForm.getAccount());
        newUser.setPassword(registerForm.getPassword());
        newUser.setEmail(registerForm.getEmail());
        newUser.setTel(registerForm.getTel());
        if (Strings.isBlank(newUser.getName()) || Strings.isBlank(newUser.getPassword()) || Strings.isBlank(newUser.getAccount())) {
            throw new ResponseException(ResponseEnum.PARAM_IS_INVALID.getCode(), ResponseEnum.PARAM_IS_INVALID.getMsg());
        }
        //判断是否有同账号名称注册过
        QueryWrapper<User> wrapper=new QueryWrapper<>();
        wrapper.eq("account",newUser.getAccount());
        List<User> users=userMapper.selectList(wrapper);
        if (!users.isEmpty()) {
            throw new ResponseException(ResponseEnum.USER_ACCOUNT_EXISTS.getCode(), ResponseEnum.USER_ACCOUNT_EXISTS.getMsg());
        }
        //注册
        userMapper.insert(newUser);
        return new UniversalResponse<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg());
    }

}
