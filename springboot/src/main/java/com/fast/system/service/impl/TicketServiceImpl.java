package com.fast.system.service.impl;

import com.fast.system.domain.Ticket;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.TicketMapper;
import com.fast.system.service.ITicketService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务工单台账业务实现
 */
@Service
public class TicketServiceImpl implements ITicketService {

    @Resource
    private TicketMapper ticketMapper;

    @Override
    public List<Ticket> selectTicketList(Ticket ticket) {
        return ticketMapper.selectTicketList(ticket);
    }

    @Override
    public Ticket selectTicketById(Long ticketId) {
        return ticketMapper.selectTicketById(ticketId);
    }

    @Override
    public int insertTicket(Ticket ticket) {
        if (ticket.getTicketNo() == null || ticket.getTicketNo().trim().isEmpty()) {
            throw new BusinessException("工单编号不能为空");
        }
        if (ticket.getTicketTitle() == null || ticket.getTicketTitle().trim().isEmpty()) {
            throw new BusinessException("工单标题不能为空");
        }
        if (ticket.getDeptId() == null) {
            ticket.setDeptId(SecurityUtils.getLoginUser().getDeptId());
        }
        if (ticket.getTicketType() == null) {
            ticket.setTicketType(0);
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(0);
        }
        return ticketMapper.insertTicket(ticket);
    }

    @Override
    public int updateTicket(Ticket ticket) {
        if (ticket.getTicketId() == null) {
            throw new BusinessException("工单ID不能为空");
        }
        // XML 里 ticket_title 恒定更新
        if (ticket.getTicketTitle() == null || ticket.getTicketTitle().trim().isEmpty()) {
            throw new BusinessException("工单标题不能为空");
        }
        return ticketMapper.updateTicket(ticket);
    }

    @Override
    public int deleteTicketById(Long ticketId) {
        return ticketMapper.deleteTicketById(ticketId);
    }
}
