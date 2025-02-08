package group.fire_monitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MapperTests {

    @Resource
    MenuMapper menuMapper;

    @Resource
    OrderMapper orderMapper;
    @Resource
    CustomerService customerService;
    @Test
    void OrderMapper(){
        System.out.println(menuMapper.selectMenu());

    }

    @Test
    void InsertOrder() {
        Orders order = new Orders(1, 1, "海棠10", 3);
        orderMapper.insertOrderDB(order);
        System.out.println(order.getOrder_id());
    }
}
