package com.fast.system.controller;

import com.fast.system.config.FastConfig;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


/**
 * 个人信息
 */
@RestController
@RequestMapping("/system/user/profile")
public class ProfileController extends BaseController {
    @Resource
    private FastConfig fastConfig;
    @Resource
    private IUserService userService;
    /**
     * 头像上传接口
     */
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam MultipartFile file) throws IOException {
        //第一步 检查文件是否为空
        if (!file.isEmpty()) {
            //获取当前登录用户的信息
            LoginUser loginUser = SecurityUtils.getLoginUser();
            //准备存放头像的文件夹
            String uploadDir = fastConfig.getProfile() + "/avatar";
            //创建一个file对象 代表这个目录路径
            File dir = new File(uploadDir);
            //看看文件夹是否存在
            if (!dir.exists()) {
                //如果不存在就创建  会创建所有不存在的父目录
                dir.mkdirs();
            }
            //第四步 为文件名生成一个唯一的值
            //拿到原始文件名
            String originalFilename = file.getOriginalFilename();
            //获取文件的扩展名
            String extension = "";
            if(originalFilename!= null && originalFilename.contains(".")){
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }else {
                //如果文件名没有拓展名
                extension = "";
            }
            //生成一个唯一的文件名  防止文件名冲突 用uuid
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //拿到最终文件名
            String newFilename = uuid + extension;
            //第五步 保存文件
            //构建完整的文件保存路径
            Path filePath = Paths.get(uploadDir, newFilename);

            //保存文件  将文件写入到指定的路径 file.getBytes()是将文件转换成字节数组
            Files.write(filePath, file.getBytes());
            //构建头像的访问路径  给前端的路径
            String avatar = "/profile/avatar/" + newFilename;
            //更新数据库中用户的头像信息
            if (userService.updateUserAvatar(loginUser.getUserId(), avatar) > 0) {
                //返回成功
                AjaxResult ajax = AjaxResult.success();
                //在响应数据中 添加头像地址
                ajax.put("imgUrl", avatar);
                //更新缓存中的用户头像
                loginUser.getUser().setAvatar(avatar);
                //返回成功响应给前端
                return ajax;
            }


        }

        //如果上传失败 返回错误信息
        return error("上传头像失败");
    }

    /**
     * 修改个人信息
     */
    @PutMapping
    public AjaxResult updateProfile(@RequestBody User user) {
        //获取当前用户数据
        LoginUser loginUser = SecurityUtils.getLoginUser();
        //提取用户数据
        User currentUser = loginUser.getUser();
        //设置要更新的用户信息
        currentUser.setUserName(user.getUserName());
        currentUser.setSex(user.getSex());
        //调用服务层更新用户信息
        return toAjax(userService.updateUser(currentUser));

    }
}
