package group.fire_monitor.util.response;

import group.fire_monitor.util.enums.ResponseEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionResHandler {
    @ExceptionHandler(ResponseException.class)
    public UniversalResponse<?> handlerRes(ResponseException e) {
        return new UniversalResponse<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public UniversalResponse<?> handler(Exception e) {
        e.printStackTrace();
        return new UniversalResponse<>(ResponseEnum.SERVER_BUSY.getCode(), ResponseEnum.SERVER_BUSY.getMsg());
    }
}
