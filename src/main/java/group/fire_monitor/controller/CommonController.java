package group.fire_monitor.controller;

import group.fire_monitor.annotation.JWTPass;
import group.fire_monitor.service.UserService;
import group.fire_monitor.util.response.UniversalResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
public class CommonController {
    @Resource
    private UserService userService;

    @GetMapping
    @ResponseBody
    @JWTPass
    public UniversalResponse<Object> index() {
        return new UniversalResponse<>(1, "OK");
    }

    /**
     * 将登录，注册操作封装到CommonService（lmpl）接口之中
     * 在登录后，将token和identity传给前端，用以登录时区分跳转到不同用户界面
     * @param loginForm 登录表单
     * @return
     * Author ruo371
     */
    @PostMapping("/login")
    @ResponseBody
    @JWTPass
    public UniversalResponse<?> login(@RequestBody LoginForm loginForm) {
        return userService.login(loginForm);
    }

    @PostMapping("/register")
    @ResponseBody
    @JWTPass
    public UniversalResponse<?> register(@RequestBody RegisterForm registerForm) {
        return userService.register(registerForm);
    }
}
