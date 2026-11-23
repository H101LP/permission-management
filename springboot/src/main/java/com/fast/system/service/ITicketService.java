package com.fast.system.service;

import com.fast.system.domain.Ticket;

import java.util.List;

/**
 * 任务工单台账业务接口
 */
public interface ITicketService {

    /** 查询工单列表（受数据权限控制） */
    List<Ticket> selectTicketList(Ticket ticket);

    /** 根据ID查询工单详情 */
    Ticket selectTicketById(Long ticketId);

    /** 新增工单 */
    int insertTicket(Ticket ticket);

    /** 修改工单 */
    int updateTicket(Ticket ticket);

    /** 删除工单 */
    int deleteTicketById(Long ticketId);
}
