package group.fire_monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import group.fire_monitor.pojo.User;
import group.fire_monitor.pojo.form.LoginForm;
import group.fire_monitor.pojo.form.RegisterForm;
import group.fire_monitor.pojo.res.TokenRes;
import group.fire_monitor.util.response.UniversalResponse;
public interface UserService extends IService<User> {
    UniversalResponse<TokenRes> login(LoginForm loginForm);

    UniversalResponse<?> register(RegisterForm registerForm);


}
