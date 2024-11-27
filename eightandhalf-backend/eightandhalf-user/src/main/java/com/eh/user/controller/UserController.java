package com.eh.user.controller;

import cn.hutool.core.util.StrUtil;
import com.eh.common.util.*;
import com.eh.common.vo.MFAResponse;
import com.eh.common.vo.Result;

import com.eh.common.vo.Token;
import com.eh.user.model.dto.UserDTO;
import com.eh.user.model.entity.User;
import com.eh.user.model.request.BindCodeRequest;
import com.eh.user.model.request.EmailRequest;
import com.eh.user.model.request.LoginRequest;
import com.eh.user.model.request.RegisterRequest;
import com.eh.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.SocketException;
import java.security.InvalidKeyException;
import java.util.*;


@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@ModelAttribute @Validated RegisterRequest registerRequest) throws SocketException {

        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();

        User userName = userService.findByUserName(username);
        if(userName!=null){
            return Result.error("用户名已被占用");
        }
        User userEmail = userService.findByEmail(email);
        if(userEmail!=null){
            return Result.error("邮箱已注册");
        }
        userService.register(registerRequest);
        return Result.success();
    }

    /**
     * 登录 使用JwtUtil获取token
     */

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<Token> login(@ModelAttribute @Validated LoginRequest loginRequest){

        String username = loginRequest.getUsername();
        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断该用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", loginUser.getUserId());
        String accessToken = JwtUtil.genToken(claims,6);
        String refreshToken = JwtUtil.genToken(claims,24);

        try {
            //code正确则直接登录
            if(loginRequest.getCode() != null&&loginUser.getSecret()!=null){
                boolean b = MFAUtil.verifyCode(loginUser.getSecret(), String.valueOf(loginRequest.getCode()));
                if(b){
                    return Result.success(new Token(accessToken,refreshToken));
                }
            }
        } catch (InvalidKeyException e) {
           log.info("啥都不干..");
        }

        String password = loginRequest.getPassword();
        //判断密码是否正确  loginUser对象中的password是密文
        if (!Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            return Result.error("密码错误");
        }

        return Result.success(new Token(accessToken,refreshToken));

    }

    @GetMapping("/refresh")
    @ApiOperation("刷新token")
    public Result refresh(@RequestParam String refreshToken) {
        if(StrUtil.isEmpty(refreshToken)){
            throw new RuntimeException("token为空");
        }
        Map<String, Object> claims = new HashMap<>();
        try {
            claims = JwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            return Result.error("token过期");
        }
        String accessToken = JwtUtil.genToken(claims,6);
        String newRefreshToken = JwtUtil.genToken(claims,24);
        return Result.success(new Token(accessToken,newRefreshToken));
    }


    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<UserDTO> getUserInfo() {
        Long id = ThreadLocalUtil.get();
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }


    @PutMapping ("/avatar")
    @ApiOperation("上传头像")
    public Result<UserDTO> uploadAvatar(MultipartFile avatar) {
        String url = TencentYunCOSUtil.upload(avatar);
        Long id = ThreadLocalUtil.get();
        userService.updateAvatarUrl(id,url);
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/mfa/qrcode")
    @ApiOperation("获取qrcode")
    public Result<MFAResponse> getQRCode() throws Exception {
        String secret = MFAUtil.generateSecret();
        String otpUri = "otpauth://totp/MyService:username?secret=" + secret + "&issuer=MyService";
        String qrCode = MFAUtil.generateQRCode(otpUri, "qrcode.png");
        return Result.success(new MFAResponse(secret,qrCode));
    }

    @PostMapping("/mfa/bind")
    @ApiOperation("绑定MFA认证")
    public Result bindCode(@ModelAttribute BindCodeRequest request) throws InvalidKeyException {
        boolean b = MFAUtil.verifyCode(request.getSecret(), String.valueOf(request.getCode()));
        if(!b){
            return Result.error("绑定失败");
        }
        userService.bindCode(request);
        return Result.success();
    }

    @PostMapping("/login/email")
    @ApiOperation("邮箱登录")
    public Result loginEmail(@ModelAttribute @Validated EmailRequest request){
        String email = request.getEmail();
        User user = userService.findByEmail(email);
        if (user == null) {
            return Result.error("邮箱未注册");
        }
        if(!userService.proveEmailCode(request)){
            return Result.error("验证失败");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        String accessToken = JwtUtil.genToken(claims,6);
        String refreshToken = JwtUtil.genToken(claims,24);

        return Result.success(new Token(accessToken,refreshToken));

    }

    @PostMapping("/login/email/code")
    @ApiOperation("获取邮箱验证码")
    public Result sendEmailCode(@ModelAttribute @Validated EmailRequest request){
        String email = request.getEmail();
        User user = userService.findByEmail(email);
        if (user == null) {
            return Result.error("邮箱未注册");
        }
        request.setUserId(user.getUserId());
        if(userService.sendEmailCode(request)){
            return Result.success();
        }else {
            return Result.error("无法发送或者仍在有效期内");
        }
    }


    @GetMapping("/listAll")
    @ApiOperation("获取用户列表")
    public List<UserDTO> getUsers(@RequestParam List<Long> ids) {
        return userService.getUsers(ids);
    }

    @GetMapping("/{username}")
    public User getUser(@PathVariable("username") String username) {
        return userService.findByUserName(username);
    }
}
