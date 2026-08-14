package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
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
    public AjaxResult success(){
        return AjaxResult.success();
    }
    /**
     * 返回错误(无数据)
     */
    public AjaxResult error(){
        return AjaxResult.error();
    }
    /**
     * 返回成功(带消息)
     */
    public AjaxResult success(String msg){
        return AjaxResult.success(msg);
    }
    /**
     * 返回成功(带数据)
     */
    public AjaxResult success(Object data){
        return AjaxResult.success(data);
    }
    /**
     * 返回错误带消息
     */
    public AjaxResult error(String msg){
        return AjaxResult.error(msg);
    }
    /**
     * 根据受影响行数判断是否成功
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows){
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 设置请求分页数据
     */
    protected void  startPage(){
        //获取当前HTTP请求
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //获取并且转换分页参数
        int pageNum = Integer.parseInt(req.getParameter("pageNum"));
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        //PageHelper.startPage 是PageHelper分页插件的核心方法
        //.setReasonable(true) 是设置分页合理化
        // 解释：true时，pageNum<=0时会查询第一页， pageNum>maxPageNum时会查询最后一页
        PageHelper.startPage(pageNum, pageSize).setReasonable(true);
    }
    /**
     * 插入分页数据
     */
    protected <T>TableDataInfo getDataTable(List<T> list){
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(list);
        dataInfo.setTotal(new PageInfo<>(list).getTotal());
        return dataInfo;
    }
}
