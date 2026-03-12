package com.example.springboot.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.User;
import com.example.springboot.service.IAdminService;
import com.example.springboot.service.IUnitService;
import com.example.springboot.service.IUserService;
import com.example.springboot.service.impl.UserServiceImpl;
import com.example.springboot.utils.TokenUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * <p>
 * 通用Web接口控制器
 * 处理登录、注册、密码修改、用户信息查询、文件上传下载等通用Web请求
 * </p>
 */
@RestController
@RequestMapping("/web") // 该控制器的基础请求路径，所有接口都以/web开头
public class WebController {

    // 定义文件上传的根路径，拼接当前项目根目录 + /files/ 目录
    private static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "/files/";

    // 从配置文件读取ip地址，默认值为localhost
    @Value("${ip:localhost}")
    String ip;

    // 从配置文件读取服务器端口号
    @Value("${server.port}")
    String port;

    // 注入用户业务层服务对象，处理用户相关业务逻辑
    @Resource
    private IUserService userService;

    // 注入管理员业务层服务对象，处理管理员相关业务逻辑
    @Resource
    private IAdminService adminService;

    // 注入单位业务层服务对象，处理单位相关业务逻辑
    @Resource
    private IUnitService unitService;

    //RequestBody会帮助我们解析前端发送来的数据对象，然后转换为java对象
    /**
     * 登录接口
     * @param account 前端传递的登录账号信息，包含用户名、密码、角色类型
     * @return Result 统一返回结果对象，包含登录成功后的账号信息或错误提示
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        // 参数判空校验：用户名、密码、角色任一为空则返回参数错误
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getRole())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }

        // 根据角色类型调用对应服务的登录方法
        if (StrUtil.equals(account.getRole(),"ROLE_USER")) { // 普通用户角色
            account = userService.login(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) { // 管理员角色
            account = adminService.login(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) { // 单位角色
            account = unitService.login(account);
        }

        // 返回登录成功的结果，包含账号信息
        return Result.success(account);
    }

    /**
     * 注册接口
     * @param account 前端传递的注册账号信息，包含用户名、密码、角色类型
     * @return Result 统一返回结果对象，注册成功则返回成功标识，失败则返回错误提示
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        // 参数判空校验：用户名、密码、角色任一为空则返回参数错误
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getRole())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }

        // 根据角色类型调用对应服务的注册方法
        if (StrUtil.equals(account.getRole(),"ROLE_USER")) { // 普通用户角色
            userService.register(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) { // 管理员角色
            adminService.register(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) { // 单位角色
            unitService.register(account);
        }

        // 返回注册成功的结果
        return Result.success();
    }

    /**
     * 修改密码接口
     * @param account 前端传递的密码修改信息，包含原密码、新密码
     * @return Result 统一返回结果对象，修改成功则返回成功标识，失败则返回错误提示
     */
    @PostMapping("/password")
    public Result updatePassword(@RequestBody Account account) {
        // 参数判空校验：原密码、新密码任一为空则返回参数错误
        if (StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getNewPassword())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        // 获取当前登录的用户信息
        Account one = TokenUtils.getCurrentUser();
        // 设置要修改密码的用户名（从当前登录用户中获取，避免前端传递错误）
        account.setUsername(one.getUsername());

        // 根据当前用户角色调用对应服务的密码修改方法
        if (StrUtil.equals(one.getRole(),"ROLE_USER")) { // 普通用户角色
            userService.updatePassword(account);
        }
        if (StrUtil.equals(one.getRole(),"ROLE_ADMIN")) { // 管理员角色
            adminService.updatePassword(account);
        }
        if (StrUtil.equals(one.getRole(),"ROLE_UNIT")) { // 单位角色
            unitService.updatePassword(account);
        }

        // 返回密码修改成功的结果
        return Result.success();
    }

    /**
     * 获取当前登录用户信息接口
     * @return Result 统一返回结果对象，包含当前用户的详细信息或获取失败的错误提示
     */
    @GetMapping("/userInfo")
    public Result userInfo() {
        // 获取当前登录的用户信息
        Account account = TokenUtils.getCurrentUser();

        // 根据用户角色调用对应服务的查询方法，返回用户详细信息
        if (StrUtil.equals(account.getRole(),"ROLE_USER")) { // 普通用户角色
            return Result.success(userService.getById(account.getId()));
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) { // 管理员角色
            return Result.success(adminService.getById(account.getId()));
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) { // 单位角色
            return Result.success(unitService.getById(account.getId()));
        }

        // 若角色不匹配，返回获取用户信息失败的错误提示
        return Result.error(Constants.CODE_605,"获取用户信息失败");
    }

    /**
     * 文件上传接口
     * @param file 前端传递过来的待上传文件
     * @return String 上传成功后的文件访问地址
     * @throws IOException 文件操作异常（如读写失败、目录创建失败等）
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        // 获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        // 获取文件扩展名（如jpg、png等）
        String type = FileUtil.extName(originalFilename);

        // 生成文件唯一标识码（UUID），拼接扩展名，避免文件重名
        String fileUUID = IdUtil.fastSimpleUUID() + StrUtil.DOT + type;

        // 构建文件保存的完整路径
        File uploadFile = new File(FILE_UPLOAD_PATH + fileUUID);
        // 获取文件所在目录
        File parentFile = uploadFile.getParentFile();
        // 若目录不存在则创建目录
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        // 将上传的文件保存到指定路径
        file.transferTo(uploadFile);
        // 拼接文件访问的URL地址（供前端下载使用）
        String url = "http://"+ip+":"+port+"/web/download/" + fileUUID;
        // 返回文件访问地址
        return url;
    }

    /**
     * 文件下载接口
     * @param fileUUID 文件的唯一标识码，用于定位要下载的文件
     * @param response HTTP响应对象，用于设置下载响应头和输出文件流
     * @throws IOException 文件操作异常（如文件不存在、流写入失败等）
     */
    @GetMapping("/download/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件唯一标识码获取要下载的文件
        File uploadFile = new File(FILE_UPLOAD_PATH + fileUUID);
        // 获取响应的输出流
        ServletOutputStream os = response.getOutputStream();
        // 设置响应头：指定文件下载的文件名编码，避免中文乱码
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        // 设置响应内容类型为二进制流，支持所有文件类型下载
        response.setContentType("application/octet-stream");

        // 读取文件字节流并写入响应输出流
        try {
            os.write(FileUtil.readBytes(uploadFile));
        } catch (Exception e) {
            // 捕获文件下载异常，打印错误信息
            System.err.println("文件下载失败，文件不存在");
        }
        // 刷新输出流
        os.flush();
        // 关闭输出流
        os.close();
    }

}