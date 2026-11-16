import request from "@/utils/requst.js";

// 查询部门列表（树形）
export function selectDeptList(query) {
    return request({
        url: "/system/dept/selectDeptList",
        method: "get",
        params: query
    });
}

// 根据部门ID查询
export function selectDeptById(deptId) {
    return request({
        url: "/system/dept/selectDeptById/" + deptId,
        method: "get"
    });
}

// 查询部门树（上级部门下拉）
export function selectDeptTree() {
    return request({
        url: "/system/dept/selectDeptTree",
        method: "get"
    });
}

// 新增部门
export function insertDept(data) {
    return request({
        url: "/system/dept/insertDept",
        method: "post",
        data: data
    });
}

// 修改部门（后端是 @PutMapping，method 必须是 put）
export function updateDept(data) {
    return request({
        url: "/system/dept/updateDept",
        method: "put",
        data: data
    });
}

// 删除部门
export function deleteDeptById(deptId) {
    return request({
        url: "/system/dept/deleteDeptById/" + deptId,
        method: "delete"
    });
}
