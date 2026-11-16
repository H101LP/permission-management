package com.fast.system.service;

import com.fast.system.domain.Dept;

import java.util.List;

/**
 * 部门服务接口
 */
public interface IDeptService {

    /** 查询部门列表（平铺） */
    List<Dept> selectDeptList(Dept dept);

    /** 查询部门树（给前端表格 / 下拉用） */
    List<Dept> selectDeptTree(Dept dept);

    /** 根据ID查询 */
    Dept selectDeptById(Long deptId);

    /** 新增 */
    int insertDept(Dept dept);

    /** 修改 */
    int updateDept(Dept dept);

    /** 删除（有子部门时拒绝） */
    int deleteDeptById(Long deptId);
}
