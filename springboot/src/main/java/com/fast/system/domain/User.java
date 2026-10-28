package com.fast.system.domain;

import com.fast.system.domain.group.AddGroup;
import com.fast.system.domain.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @NotNull(groups = {UpdateGroup.class}, message = "修改时用户ID不能为空")
    private Long userId;
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间", groups = {AddGroup.class, UpdateGroup.class})
    private String userName;

    private Integer sex;

    private String avatar;
    @NotBlank(groups = {AddGroup.class}, message = "添加时密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 后端返回前端时隐藏，前端传后端时保留
    private String password;

    //创建时间
    //可以控制时间格式 GMT+8  表示东八区时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //角色ID（用于关联角色信息）
    private Long roleId;
    //角色名称（用于显示角色信息）
    private String roleName;

}
