package com.fast.system.controller;

import com.fast.system.annotation.OperationLog;
import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.PageResult;
import com.fast.system.domain.Ticket;
import com.fast.system.enums.OperType;
import com.fast.system.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单管理
 */
@Tag(name = "工单管理")
@RestController
@RequestMapping("/business/ticket")
public class TicketController extends BaseController {

    @Resource
    private ITicketService ticketService;

    @Operation(summary = "查询工单列表")
    @GetMapping("/selectTicketList")
    public AjaxResult<PageResult<Ticket>> selectTicketList(Ticket ticket) {
        startPage();
        List<Ticket> list = ticketService.selectTicketList(ticket);
        return getDataTable(list);
    }

    @Operation(summary = "根据ID查询工单详情")
    @GetMapping("/selectTicketById/{ticketId}")
    public AjaxResult selectTicketById(@PathVariable Long ticketId) {
        return success(ticketService.selectTicketById(ticketId));
    }

    @PreAuthorizePerm("business:ticket:add")
    @Operation(summary = "新增工单")
    @OperationLog(module = "工单管理", business = "新增工单", type = OperType.INSERT)
    @PostMapping("/insertTicket")
    public AjaxResult insertTicket(@RequestBody Ticket ticket) {
        return toAjax(ticketService.insertTicket(ticket));
    }

    @PreAuthorizePerm("business:ticket:edit")
    @Operation(summary = "修改工单")
    @OperationLog(module = "工单管理", business = "修改工单", type = OperType.UPDATE)
    @PutMapping("/updateTicket")
    public AjaxResult updateTicket(@RequestBody Ticket ticket) {
        return toAjax(ticketService.updateTicket(ticket));
    }

    @PreAuthorizePerm("business:ticket:remove")
    @Operation(summary = "删除工单")
    @OperationLog(module = "工单管理", business = "删除工单", type = OperType.DELETE)
    @DeleteMapping("/deleteTicketById/{ticketId}")
    public AjaxResult deleteTicketById(@PathVariable Long ticketId) {
        return toAjax(ticketService.deleteTicketById(ticketId));
    }
}
