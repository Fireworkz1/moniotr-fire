package group.fire_monitor.Interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import group.fire_monitor.annotation.JWTPass;
import group.fire_monitor.mapper.UserMapper;
import group.fire_monitor.pojo.User;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.util.enums.ResponseEnum;
import group.fire_monitor.util.response.ResponseException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JWTInterceptor implements HandlerInterceptor {
    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            // 之前一堆bug就是因为预检机制
            System.out.println("OPTIONS放行");
            return true;
        }
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/doc.html") ||requestURI.contains("/favicon.ico") || requestURI.contains("/v2/api-docs") || requestURI.contains("/swagger-resources") || requestURI.contains("/webjars")) {
            return true; // 放行Swagger UI的请求
        }

        // 检查handler是否是HandlerMethod的实例
        if (handler instanceof HandlerMethod) {
            // 将handler强制转换为HandlerMethod
            // 这样可以访问请求处理程序的详情信息
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 检验是否有注解
            if (handlerMethod.hasMethodAnnotation(JWTPass.class)) {
                return true;    // 放行
            }
        }

        String token = request.getHeader("token");
        if (Strings.isBlank(token)) {
            throw new ResponseException(ResponseEnum.USER_TOKEN_ERROR.getCode(), ResponseEnum.USER_TOKEN_ERROR.getMsg());
        }

        Integer id;
        try {
            id = Integer.valueOf(JWT.decode(token).getAudience().get(0));
        } catch (JWTDecodeException e) {
            throw new ResponseException(ResponseEnum.USER_TOKEN_ERROR.getCode(), ResponseEnum.USER_TOKEN_ERROR.getMsg());
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ResponseException(ResponseEnum.USER_TOKEN_ERROR.getCode(), ResponseEnum.USER_TOKEN_ERROR.getMsg());
        }

        // 验证签名
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(JWTUtil.secure)).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTDecodeException e) {
            throw new ResponseException(ResponseEnum.USER_TOKEN_ERROR.getCode(), ResponseEnum.USER_TOKEN_ERROR.getMsg());
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
