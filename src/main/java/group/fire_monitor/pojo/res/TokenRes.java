package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class TokenRes {

    private String token;
//    private Integer identity;

    public TokenRes(String token) {
        this.token = token;
//        this.identity = identity;
    }
}
