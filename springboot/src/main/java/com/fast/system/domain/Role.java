package com.fast.system.domain;

import com.fast.system.domain.group.AddGroup;
import com.fast.system.domain.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色对象 对应role表
 */
@Data
public class Role {
    // 角色ID
    @NotNull(groups = UpdateGroup.class, message = "修改时角色ID不能为空")
    private Long roleId;
    // 角色名称
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "角色名称不能为空")
    @Size(min = 2, max = 30, message = "角色名称长度必须在2-30位之间", groups = {AddGroup.class, UpdateGroup.class})
    private String roleName;
    //显示顺序
    private Integer roleSort;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
    //菜单组
    private Long[] menuIds;
}
