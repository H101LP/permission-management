package com.fast.system.service.impl;

import com.fast.system.domain.Order;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.OrderMapper;
import com.fast.system.service.IOrderService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 销售订单台账业务实现
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public List<Order> selectOrderList(Order order) {
        return orderMapper.selectOrderList(order);
    }

    @Override
    public Order selectOrderById(Long orderId) {
        return orderMapper.selectOrderById(orderId);
    }

    @Override
    public int insertOrder(Order order) {
        if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            throw new BusinessException("订单编号不能为空");
        }
        if (order.getOrderName() == null || order.getOrderName().trim().isEmpty()) {
            throw new BusinessException("订单名称不能为空");
        }
        // order_no 有唯一索引，重复时数据库抛 Duplicate entry，由全局异常处理器兜底
        if (order.getDeptId() == null) {
            order.setDeptId(SecurityUtils.getLoginUser().getDeptId());
        }
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        if (order.getAmount() == null) {
            order.setAmount(java.math.BigDecimal.ZERO);
        }
        return orderMapper.insertOrder(order);
    }

    @Override
    public int updateOrder(Order order) {
        if (order.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        // XML 里 order_name 恒定更新
        if (order.getOrderName() == null || order.getOrderName().trim().isEmpty()) {
            throw new BusinessException("订单名称不能为空");
        }
        return orderMapper.updateOrder(order);
    }

    @Override
    public int deleteOrderById(Long orderId) {
        return orderMapper.deleteOrderById(orderId);
    }
}
