package com.fast.system.service.impl;

import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.Menu;
import com.fast.system.domain.TreeSelect;
import com.fast.system.domain.vo.MetaVo;
import com.fast.system.domain.vo.RouterVo;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IMenuService;
import com.sun.net.httpserver.Authenticator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements IMenuService {
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    /**
     * 查询菜单列表
     */
    @Override
    public List<Menu> selectMenuList(Menu menu, Long userId) {
       //根据用户ID查询对应的角色信息
        Long roleId = userRoleMapper.selectRoleIdByUserId(userId);
        //管理员显示所有菜单信息
        if(Objects.equals(roleId, RoleIdConstants.ADMIN_ROLE_ID)){
           return menuMapper.selectMenuListByUserId(menu);
        }else {
            menu.setUserId(userId);
            return menuMapper.selectMenuListByUserId(menu);
        }
    }

    /**
     * 新增菜单
     * @param menu 菜单信息
     * @return
     */
    @Override
    public int insertMenu(Menu menu) {
        return menuMapper.insertMenu(menu);
    }

    /**
     * 根据菜单ID查询菜单信息
     */
    @Override
    public Menu selectMenuByMenuId(Long menuId) {
        return menuMapper.selectMenuByMenuId(menuId);
    }
    /**
     * 修改菜单
     */
    @Override
    public int updateMenu(Menu menu) {
        return menuMapper.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @Override
    public int deleteMenuByMenuId(Long menuId) {
        return menuMapper.deleteMenuByMenuId(menuId);
    }

    /**
     * 根据角色ID查询对应的菜单树
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        return menuMapper.selectMenuListByRoleId(roleId);
    }
    /**
     * 构建前端所需要的下拉树结构
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<Menu> menus) {
        //步骤1 将平铺的菜单列表转换为树形的菜单列表
        List<Menu> menuTrees = buildMenuTree(menus);
        //步骤2 将树形Menu列表转换为TreeSelect列表
        return menuTrees.stream()
                .map(TreeSelect::new)
                .toList();
    }


    /**
     * 构建前端所需要的树结构
     */
    public List<Menu> buildMenuTree(List<Menu> menus) {
        //去除重复的菜单项 以menuId作为判断依据
        LinkedHashMap<Long, Menu> uniqeMenusMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            //如果菜单ID存在 跳过
            if (!uniqeMenusMap.containsKey(menu.getMenuId())) {
                uniqeMenusMap.put(menu.getMenuId(), menu);
            }
        }
        //转换为去重后的列表
        ArrayList<Menu> uniqeMenus = new ArrayList<>(uniqeMenusMap.values());
        //存放最终构建好的树形菜单列表
        ArrayList<Menu> returnList = new ArrayList<>();
        //将菜单列表转换为Map 快速查找
        LinkedHashMap<Long, Menu> menuMap = new LinkedHashMap<>();
        for (Menu menu : uniqeMenus) {
            menuMap.put(menu.getMenuId(), menu);
            //确保每个菜单都有初始化的children列表
            if(menu.getChildren() == null){
                menu.setChildren(new ArrayList<>());
            }else {
                //清空现有的children列表 避免重复添加
                menu.getChildren().clear();
            }
        }
        //查找顶级菜单
        HashSet<Long> addedTopMenuIds = new HashSet<>();
        for (Menu menu : uniqeMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                //这是一个顶级菜单 如果还没有添加过 则加入到结果列表
                if (!addedTopMenuIds.contains(menu.getMenuId())) {
                    returnList.add(menu);
                    addedTopMenuIds.add(menu.getMenuId());
                }
            } else {
                //这是一个子菜单 找到它的父菜单并添加到父菜单的children列表中
                Menu parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                }else {
                    //如果没有找到父菜单 可以暂时作为顶级菜单处理
                    returnList.add(menu);
                    addedTopMenuIds.add(menu.getMenuId());
                }
            }
        }
        return returnList;
    }
    /**
     * 根据用户ID查询菜单树并且构建前端所需要的路由结构
     * @param userId 用户ID
     * @return 路由结构列表
     */
    @Override
    public List<RouterVo> selectMenuTreeRouterByUserId(long userId) {
        //创建菜单查询条件对象
        Menu menu = new Menu();
        List<Menu> menus;
        //1判断用户身份 获取对应平铺的菜单列表数据
        //根据用户ID查询对应的角色信息
        Long roleId = userRoleMapper.selectRoleIdByUserId(userId);
        //管理员显示所有菜单信息
        if(Objects.equals(roleId, RoleIdConstants.ADMIN_ROLE_ID)){
            menus =  menuMapper.selectMenuListByUserId(new Menu());
        }else {
            menu.setUserId(userId);
            menus = menuMapper.selectMenuListByUserId(menu);
        }
        //2 把菜单列表变成Map 方便根据ID快速查找
        HashMap<Long, Menu> menuMap = new HashMap<>();
        for (Menu m : menus) {
            menuMap.put(m.getMenuId(), m); //将菜单ID作为key 菜单对象作为value
        }
        //3 构建属性结构
        List<Menu> rootMenus = new ArrayList<>(); // 顶级菜单列表
        for (Menu m : menus) {
            //清空children列表 避免重复添加
            m.setChildren(new ArrayList<>());
            //如果菜单的父ID为空 或者 父ID为0 则认为是顶级菜单
            if (m.getParentId() == null || m.getParentId() == 0) {
                rootMenus.add(m);
            } else {
                //否则认为是子菜单 找到它的父菜单并添加到父菜单的children列表中
                Menu parent = menuMap.get(m.getParentId());
                if (parent != null) {
                    parent.getChildren().add(m);
                }
            }
        }
        //4 构建路由结构
        return buildMenus(rootMenus);
    }
    /**
     * 构建前端需要的路由格式
     */
    private List<RouterVo> buildMenus(List<Menu> menus) {
        //创建空的LinkedList 用于存储最终结果
        LinkedList<RouterVo> routers = new LinkedList<>();
        //开始处理菜单列表
        for (Menu menu : menus) {
            //1 创建一个新的前端路由对象
            RouterVo router = new RouterVo();
            //2 设置路由的名称 name
            String routeName = menu.getPath(); // 设置路由的名称 name
            if(menu.getParentId() ==0 && menu.getMenuId() != null){
                //对于顶级菜单 确保名称唯一
                routeName = menu.getPath() + "_" + menu.getMenuId();
            }
            router.setName(routeName);
            //3设置路由的访问路径

            //先获取菜单在数据库中的原始字符串路径
            String routerPath = menu.getPath();
            //情况1 如果这个菜单是一级目录(顶)
            if(menu.getParentId().intValue() == 0 && menu.getMenuType().equals("M")){
                //一级目录的路径需要加/
                routerPath = "/" + menu.getPath();
            }
            //情况2 如果这个如果这个菜单是一级菜单(不是目录 具体页面)
            else if(menu.getParentId().intValue() == 0 && menu.getMenuType().equals("C")){
                //一级菜单的路径设置为根路径
                routerPath = "/";
            }
            //情况3 二级菜单或者更低级的菜单
            router.setPath(routerPath);

            //4设置路由的组件 component
//情况1 如果菜单配置了组件 而且不是特殊情况(不是一级C类型的菜单)
            String component = "Layout";
            if(menu.getComponent() != null && !menu.getComponent().isEmpty()
                    && !(menu.getParentId().intValue()==0 && "C".equals(menu.getMenuType())) ) {
                //使用菜单配置的组件路径
                component = menu.getComponent();
            }
//情况2 如果菜单没有配置组件 但是他是二级或者以上的目录
            else if((menu.getComponent() == null || menu.getComponent().isEmpty())
                    && menu.getParentId().intValue() != 0 && "M".equals(menu.getMenuType()) ) {
                component = "ParentView";
            }
            //情况3 其他情况 一级目录 没有组件的一级菜单
            router.setComponent(component);

            //5设置路由的元信息 meta
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
            //6设置路由的子菜单 children 递归处理
            List<Menu> cMenus = menu.getChildren();
            //情况1 如果当前菜单有子菜单 并且它是目录
            if(!cMenus.isEmpty() && "M".equals(menu.getMenuType())){
                router.setAlwaysShow(true); // 始终显示
                router.setChildren(buildMenus(cMenus));
            }
            //情况2 一级菜单C
            else if(menu.getParentId().intValue() == 0 && "C".equals(menu.getMenuType())) {
                //清空当前路由的meta
                router.setMeta(null);
                //创建一个子路由列表
                ArrayList<RouterVo> childrenList = new ArrayList<>();
                //创建一个子路由对象
                RouterVo children = new RouterVo();
                //设置子路由的各个属性
                children.setName(menu.getPath());
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                //创建子路由的meta
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                //将子路由添加到列表中
                childrenList.add(children);
                //将子路由列表添加到子路由对象中
                router.setChildren(childrenList);
            }
            //情况3 普通页面菜单 没有子菜单的C类型菜单
            routers.add(router);
        }
        //返回构建好的路由列表
        System.out.println(routers);
        return routers;
    }

}
