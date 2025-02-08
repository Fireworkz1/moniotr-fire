package group.ordersystem.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class PostOrderForm {
    //送餐地点
    private String          destination;
    //选菜的菜主码
    private List<Integer>   meals;
}
