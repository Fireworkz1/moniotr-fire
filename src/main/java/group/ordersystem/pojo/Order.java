package group.ordersystem.pojo;

import lombok.Data;

import java.sql.Timestamp;


@Data
public class Order {
    private Integer     order_id;
    private Integer     status;
    private Integer     customer_id;
    private Integer     deliver_id;
    private Timestamp   deliver_time;
    private String      destination;
    private Integer     order_price;
    private String      order_comment;
}
