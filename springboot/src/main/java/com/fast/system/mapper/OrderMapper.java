package com.fast.system.mapper;

import com.fast.system.annotation.DataScope;
import com.fast.system.domain.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    // 查询订单列表
    @DataScope(deptAlias = "o")          // 与 XML 的 FROM biz_order o 对应
    List<Order> selectOrderList(Order order);

    // 查询订单详情
    Order selectOrderById(Long orderId);

    // 新增订单
    int insertOrder(Order order);

    // 修改订单
    int updateOrder(Order order);

    // 删除订单
    int deleteOrderById(Long orderId);
}
