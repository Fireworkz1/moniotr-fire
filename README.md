订餐系统后端代码<br><br>

OrderStatusEnum:订单状态枚举类。<br>
    CREATED：订单已创建状态（师傅正在炒菜）          code=1<br>
    COOKED：菜品已经出餐（正在等待配送）             code=2<br>
    ACCEPTED：订单正在配送（外卖小哥取到餐了）        code=3<br>
    FINISHED：订单已经送达（顾客还没有评论           code=4<br>
    COMMENTED：已经被顾客评论了                    code=5<br>
                