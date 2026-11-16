package com.fast.system.service.impl;

import com.fast.system.domain.Dept;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.DeptMapper;
import com.fast.system.service.IDeptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 部门服务实现
 */
@Service
public class DeptServiceImpl implements IDeptService {

    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<Dept> selectDeptList(Dept dept) {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public List<Dept> selectDeptTree(Dept dept) {
        return buildDeptTree(deptMapper.selectDeptList(dept));
    }

    @Override
    public Dept selectDeptById(Long deptId) {
        return deptMapper.selectDeptById(deptId);
    }

    @Override
    public int insertDept(Dept dept) {
        Long parentId = dept.getParentId();

        // 核心：ancestors = 父的ancestors + "," + 父的deptId
        if (parentId == null || parentId == 0L) {
            dept.setParentId(0L);
            dept.setAncestors("0");
        } else {
            Dept parent = deptMapper.selectDeptById(parentId);
            if (parent == null) {
                throw new BusinessException("上级部门不存在");
            }
            dept.setAncestors(parent.getAncestors() + "," + parent.getDeptId());
        }

        // 兜底默认值，避免把数据库默认值覆盖成 null
        if (dept.getStatus() == null || dept.getStatus().isEmpty()) {
            dept.setStatus("0");
        }
        if (dept.getOrderNum() == null) {
            dept.setOrderNum(0);
        }

        return deptMapper.insertDept(dept);
    }

    @Override
    public int updateDept(Dept dept) {
        // 本期不支持改父级，parent_id / ancestors 在 XML 里就不更新
        return deptMapper.updateDept(dept);
    }

    @Override
    public int deleteDeptById(Long deptId) {
        int childrenCount = deptMapper.selectChildrenCount(deptId);
        if (childrenCount > 0) {
            throw new BusinessException("存在子部门，不允许删除");
        }
        return deptMapper.deleteDeptById(deptId);
    }

    /**
     * 平铺列表 → 树
     * 与 MenuServiceImpl.buildMenuTree 完全同一思路：LinkedHashMap 定位父节点，父挂子
     *
     */
    private List<Dept> buildDeptTree(List<Dept> depts) {
      LinkedHashMap<Long,Dept> deptMap = new LinkedHashMap<>();
      for (Dept dept : depts) {
          dept.getChildren().clear();
          deptMap.put(dept.getDeptId(), dept);
      }
      List<Dept> returnList = new ArrayList<>();
      Set<Long> addedIds = new HashSet<>();
      for(Dept dept :depts){
          Long parentId = dept.getParentId();
          if(parentId == null || parentId == 0L){
              if(!addedIds.contains(dept.getDeptId())){
                  returnList.add(dept);
                  addedIds.add(dept.getDeptId());
              }
          }else {
              Dept parent = deptMap.get(parentId);
              if(parent != null) {
                  parent.getChildren().add(dept);
              }else {
                  returnList.add(dept);
                  addedIds.add(dept.getDeptId());
              }
          }
      }
      return returnList;
    }
}
