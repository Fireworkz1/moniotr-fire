package group.fire_monitor.util.response;

import lombok.Data;

@Data
public class UniversalResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public UniversalResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public UniversalResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public UniversalResponse() {

    }

    public UniversalResponse<T> success(T data){
        this.code=200;
        this.msg="success";
        this.data=data;
        return this;
    }
    public UniversalResponse<?> success(){
        this.code=200;
        this.msg="success";
        return this;
    }
}
