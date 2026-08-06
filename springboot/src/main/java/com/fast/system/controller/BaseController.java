package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;

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

}
