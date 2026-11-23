package com.fast.system.mapper;

import com.fast.system.annotation.DataScope;
import com.fast.system.domain.Ticket;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TicketMapper {
    // 查询工单列表：数据权限只贴这一行
    @DataScope(deptAlias = "t")          // 与 XML 的 FROM biz_ticket t 对应
    List<Ticket> selectTicketList(Ticket ticket);

    // 查询工单详情
    Ticket selectTicketById(Long ticketId);

    // 新增工单
    int insertTicket(Ticket ticket);

    // 修改工单
    int updateTicket(Ticket ticket);

    // 删除工单
    int deleteTicketById(Long ticketId);
}
