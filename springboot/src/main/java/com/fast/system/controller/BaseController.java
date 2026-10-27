package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * web层通用数据处理  基类
 * 目的：把常用的方法写在这里 避免重复造轮子
 */
public class BaseController {
    /**
     * 返回成功但是无数据
     */
    public<T> AjaxResult<T> success(){
        return AjaxResult.success();
    }
    /**
     * 返回错误(无数据)
     */
    public <T> AjaxResult <T> error(){
        return AjaxResult.error("操作失败");
    }
    /**
     * 返回成功(带消息)
     */
    public <T> AjaxResult <T> success(String msg){
        return AjaxResult.successMsg(msg);
    }
    /**
     * 返回成功(带数据)
     */
    public <T> AjaxResult <T> success(T data){
        return AjaxResult.success(data);
    }
    /**
     * 返回错误带消息
     */
    public <T> AjaxResult <T> error(String msg){
        return AjaxResult.error(msg);
    }
    /**
     * 根据受影响行数判断是否成功
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult<Void> toAjax(int rows){
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("操作失败");
    }

    /**
     * 开启分页（从请求中读取 pageNum 和 pageSize）
     */
    protected void startPage() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        // 加固：参数为空时给默认值，避免 NumberFormatException
        int pageNum = (pageNumStr == null || pageNumStr.isEmpty()) ? 1 : Integer.parseInt(pageNumStr);
        int pageSize = (pageSizeStr == null || pageSizeStr.isEmpty()) ? 10 : Integer.parseInt(pageSizeStr);

        PageHelper.startPage(pageNum, pageSize).setReasonable(true);
    }
    /**
     * 包装分页结果
     * 返回 AjaxResult<PageResult<T>>，前端统一从 data.rows 和 data.total 取数据
     */
    protected <T> AjaxResult<PageResult<T>> getDataTable(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);

        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRows(pageInfo.getList());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());

        return AjaxResult.success(pageResult);
    }
}
