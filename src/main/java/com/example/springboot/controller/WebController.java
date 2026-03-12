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


@RestController
@RequestMapping("/web")
public class WebController {

    private static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "/files/";

    @Value("${ip:localhost}")
    String ip;

    @Value("${server.port}")
    String port;

    @Resource
    private IUserService userService;

    @Resource
    private IAdminService adminService;

    @Resource
    private IUnitService unitService;

    //RequestBody会帮助我们节气前端发送来的数据对象，然后帮我们解析成java语言内的对象
    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        //要进行一个判空，若果前端有少发的内容，那么告诉他一下，咱们就不执行了
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getRole())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }

        if (StrUtil.equals(account.getRole(),"ROLE_USER")) {
            account = userService.login(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) {
            account = adminService.login(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) {
            account = unitService.login(account);
        }

        return Result.success(account);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        //要进行一个判空，若果前端有少发的内容，那么告诉他一下，咱们就不执行了
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getRole())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }

        if (StrUtil.equals(account.getRole(),"ROLE_USER")) {
            userService.register(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) {
            adminService.register(account);
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) {
            unitService.register(account);
        }

        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result updatePassword(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getNewPassword())) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        Account one = TokenUtils.getCurrentUser();
        account.setUsername(one.getUsername());

        if (StrUtil.equals(one.getRole(),"ROLE_USER")) {
            userService.updatePassword(account);
        }
        if (StrUtil.equals(one.getRole(),"ROLE_ADMIN")) {
            adminService.updatePassword(account);
        }
        if (StrUtil.equals(one.getRole(),"ROLE_UNIT")) {
            unitService.updatePassword(account);
        }

        return Result.success();
    }
    /**
     * 获取用户信息
     */
    @GetMapping("/userInfo")
    public Result userInfo() {
        Account account = TokenUtils.getCurrentUser();

        if (StrUtil.equals(account.getRole(),"ROLE_USER")) {
            return Result.success(userService.getById(account.getId()));
        }
        if (StrUtil.equals(account.getRole(),"ROLE_ADMIN")) {
            return Result.success(adminService.getById(account.getId()));
        }
        if (StrUtil.equals(account.getRole(),"ROLE_UNIT")) {
            return Result.success(unitService.getById(account.getId()));
        }

        return Result.error(Constants.CODE_605,"获取用户信息失败");
    }


    /**
     * 文件上传接口
     *
     * @param file 前端传递过来的文件
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);

        // 定义一个文件唯一的标识码
        String fileUUID = IdUtil.fastSimpleUUID() + StrUtil.DOT + type;

        File uploadFile = new File(FILE_UPLOAD_PATH + fileUUID);
        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        File parentFile = uploadFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        // 文件保存到磁盘
        file.transferTo(uploadFile);
        // 拼接文件地址
        String url = "http://"+ip+":"+port+"/web/download/" + fileUUID;
        // 返回文件地址
        return url;
    }

    /**
     * 文件下载接口
     *  @param fileUUID 根据文件UUID找到特定文件
     */
    @GetMapping("/download/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(FILE_UPLOAD_PATH + fileUUID);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        try {
            os.write(FileUtil.readBytes(uploadFile));
        } catch (Exception e) {
            System.err.println("文件下载失败，文件不存在");
        }
        os.flush();
        os.close();
    }

}
