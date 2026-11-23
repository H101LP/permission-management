package com.fast.system.service;

import com.fast.system.domain.Order;

import java.util.List;

/**
 * 销售订单台账业务接口
 */
public interface IOrderService {

    /** 查询订单列表（受数据权限控制） */
    List<Order> selectOrderList(Order order);

    /** 根据ID查询订单详情 */
    Order selectOrderById(Long orderId);

    /** 新增订单 */
    int insertOrder(Order order);

    /** 修改订单 */
    int updateOrder(Order order);

    /** 删除订单 */
    int deleteOrderById(Long orderId);
}
