package com.zhao.easyJmeter.controller.cms;

import com.zhao.easyJmeter.common.LocalUser;
import com.zhao.easyJmeter.common.configuration.LoginCaptchaProperties;
import com.zhao.easyJmeter.dto.user.ChangePasswordDTO;
import com.zhao.easyJmeter.dto.user.LoginDTO;
import com.zhao.easyJmeter.dto.user.RegisterDTO;
import com.zhao.easyJmeter.dto.user.UpdateInfoDTO;
import com.zhao.easyJmeter.model.GroupDO;
import com.zhao.easyJmeter.model.UserDO;
import com.zhao.easyJmeter.service.GroupService;
import com.zhao.easyJmeter.service.UserIdentityService;
import com.zhao.easyJmeter.service.UserService;
import com.zhao.easyJmeter.vo.*;
import com.zhao.easyJmeter.vo.*;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import io.github.talelin.core.annotation.AdminRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionModule;
import io.github.talelin.core.annotation.RefreshRequired;
import io.github.talelin.core.token.DoubleJWT;
import io.github.talelin.core.token.Tokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@RestController
@RequestMapping("/cms/user")
@PermissionModule(value = "用户")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserIdentityService userIdentityService;

    @Autowired
    private DoubleJWT jwt;

    @Autowired
    private LoginCaptchaProperties captchaConfig;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @AdminRequired
    public CreatedVO register(@RequestBody @Validated RegisterDTO validator) {
        userService.createUser(validator);
        return new CreatedVO(11);
    }

    /**
     * 用户登陆
     */
    @PostMapping("/login")
    public Tokens login(@RequestBody @Validated LoginDTO validator, @RequestHeader(value = "Tag", required = false) String tag) {
        if (captchaConfig.getEnabled()) {
            // TODO: 使用spring validation验证。暂时还没想到怎么根据配置文件分组
            if (!StringUtils.hasText(validator.getCaptcha()) || !StringUtils.hasText(tag)) {
                throw new ParameterException("验证码不可为空");
            }
            if (!userService.verifyCaptcha(validator.getCaptcha(), tag)) {
                throw new ParameterException(10260);
            }
        }
        UserDO user = userService.getUserByUsername(validator.getUsername());
        if (user == null) {
            throw new NotFoundException(10021);
        }
        boolean valid = userIdentityService.verifyUsernamePassword(
                user.getId(),
                user.getUsername(),
                validator.getPassword());
        if (!valid) {
            throw new ParameterException(10031);
        }
        return jwt.generateTokens(user.getId());
    }

    @PostMapping("/captcha")
    public LoginCaptchaVO userCaptcha() throws Exception {
        if (captchaConfig.getEnabled()) {
            return userService.generateCaptcha();
        }
        return new LoginCaptchaVO();
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    @LoginRequired
    public UpdatedVO update(@RequestBody @Validated UpdateInfoDTO validator) {
        userService.updateUserInfo(validator);
        return new UpdatedVO(6);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change_password")
    @LoginRequired
    public UpdatedVO updatePassword(@RequestBody @Validated ChangePasswordDTO validator) {
        userService.changeUserPassword(validator);
        return new UpdatedVO(4);
    }

    /**
     * 刷新令牌
     */
    @GetMapping("/refresh")
    @RefreshRequired
    public Tokens getRefreshToken() {
        UserDO user = LocalUser.getLocalUser();
        return jwt.generateTokens(user.getId());
    }

    /**
     * 查询拥有权限
     */
    @GetMapping("/permissions")
    @LoginRequired
    public UserPermissionVO getPermissions() {
        UserDO user = LocalUser.getLocalUser();
        boolean admin = groupService.checkIsRootByUserId(user.getId());
        List<Map<String, List<Map<String, String>>>> permissions = userService.getStructuralUserPermissions(user.getId());
        UserPermissionVO userPermissions = new UserPermissionVO(user, permissions);
        userPermissions.setAdmin(admin);
        return userPermissions;
    }

    /**
     * 查询自己信息
     */
    @LoginRequired
    @GetMapping("/information")
    public UserInfoVO getInformation() {
        UserDO user = LocalUser.getLocalUser();
        List<GroupDO> groups = groupService.getUserGroupsByUserId(user.getId());
        return new UserInfoVO(user, groups);
    }
}
