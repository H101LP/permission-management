package com.fast.system.controller;

import com.fast.system.annotation.OperationLog;
import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Order;
import com.fast.system.domain.PageResult;
import com.fast.system.enums.OperType;
import com.fast.system.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/business/order")
public class OrderController extends BaseController {

    @Resource
    private IOrderService orderService;

    /**
     * 查询订单列表
     * 同样不加接口级权限注解，避免部门管理员 403
     */
    @Operation(summary = "查询订单列表")
    @GetMapping("/selectOrderList")
    public AjaxResult<PageResult<Order>> selectOrderList(Order order) {
        startPage();
        List<Order> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    @Operation(summary = "根据ID查询订单详情")
    @GetMapping("/selectOrderById/{orderId}")
    public AjaxResult selectOrderById(@PathVariable Long orderId) {
        return success(orderService.selectOrderById(orderId));
    }

    @PreAuthorizePerm("business:order:add")
    @Operation(summary = "新增订单")
    @OperationLog(module = "订单管理", business = "新增订单", type = OperType.INSERT)
    @PostMapping("/insertOrder")
    public AjaxResult insertOrder(@RequestBody Order order) {
        return toAjax(orderService.insertOrder(order));
    }

    @PreAuthorizePerm("business:order:edit")
    @Operation(summary = "修改订单")
    @OperationLog(module = "订单管理", business = "修改订单", type = OperType.UPDATE)
    @PutMapping("/updateOrder")
    public AjaxResult updateOrder(@RequestBody Order order) {
        return toAjax(orderService.updateOrder(order));
    }

    @PreAuthorizePerm("business:order:remove")
    @Operation(summary = "删除订单")
    @OperationLog(module = "订单管理", business = "删除订单", type = OperType.DELETE)
    @DeleteMapping("/deleteOrderById/{orderId}")
    public AjaxResult deleteOrderById(@PathVariable Long orderId) {
        return toAjax(orderService.deleteOrderById(orderId));
    }
}
