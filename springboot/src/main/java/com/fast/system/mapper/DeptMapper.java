package com.fast.system.mapper;

import com.fast.system.domain.Dept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 部门 Mapper
 */
@Mapper
public interface DeptMapper {

    /** 查询部门列表（平铺，返回后在 Service 里建树） */
    List<Dept> selectDeptList(Dept dept);

    /** 根据部门ID查询 */
    Dept selectDeptById(Long deptId);

    /** 新增部门 */
    int insertDept(Dept dept);

    /** 修改部门（不更新 parent_id / ancestors） */
    int updateDept(Dept dept);

    /** 删除部门 */
    int deleteDeptById(Long deptId);

    /** 查询子部门数量（删除前校验） */
    int selectChildrenCount(Long deptId);
}
