package com.keyl1me.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyl1me.dto.LoginFormDTO;
import com.keyl1me.dto.Result;
import com.keyl1me.entity.User;
import com.keyl1me.mapper.UserMapper;
import com.keyl1me.service.IUserService;
import com.keyl1me.utils.RegexUtils;
import com.keyl1me.utils.SystemConstants;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 保存验证码到session
        session.setAttribute("code", code);

        log.debug("发送短信验证码成功，验证码：" + code);
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 校验验证码
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            // 不一致 报错
            return Result.fail("验证码错误");
        }

        // 一致 从数据库里查询对应数据 判断用户是否存在
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 若不存在，则插入新数据
            user = createUserWithPhone(phone);
        }

        // 若存在，则将用户信息保存到session
        session.setAttribute("user", user);
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
