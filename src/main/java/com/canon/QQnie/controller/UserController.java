package com.canon.QQnie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.canon.QQnie.common.R;
import com.canon.QQnie.entity.User;
import com.canon.QQnie.service.UserService;
import com.canon.QQnie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        //若手机号不为空
        if (StringUtils.isNotEmpty(phone)){
            //生产随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}",code);

            //调用阿里云提供的短信服务API完成短信发送
           // SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //需要将生成的验证码保存到session
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到Redis中，并设置有效时间为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码发送成功！");
        }

        return R.error("短信发送失败！");
    }


    /**
     * 用户登陆
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取手机号、验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码）
        if (codeInSession != null && codeInSession.equals(code)){
            //如果能比对成功，说明登陆成功
            //查询是否为新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);

            if (user == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //保存user的id到session中，以便在过滤器中进行登陆校验
            session.setAttribute("user",user.getId());

            //如果用户登陆成功，删除redis中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登陆失败！");
    }

    /**
     * 退出登陆
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){
        //清理session中保存的当前登陆用户的id
        session.removeAttribute("user");
        return R.success("退出登陆成功！");
    }
}
