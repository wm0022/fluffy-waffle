package com.shengwei.tushuguanli.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.shengwei.tushuguanli.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器（用于顾客登录）
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 验证码 Redis 缓存前缀 */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    /** 验证码有效期 5 分钟 */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 生成图形验证码，返回图片 Base64 和唯一标识 key
     */
    @GetMapping("/image")
    public Result<Map<String, Object>> getCaptchaImage() {
        // 定义验证码图片规格：宽130px 高48px，4位字符
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(130, 48, 4, 80);
        String captchaCode = lineCaptcha.getCode();
        String captchaBase64 = "data:image/png;base64," + lineCaptcha.getImageBase64();

        // 生成唯一标识，用于后续登录时校验
        String captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 将验证码文本存入 Redis，有效期5分钟
        redisTemplate.opsForValue()
                .set(CAPTCHA_KEY_PREFIX + captchaKey, captchaCode.toLowerCase(),
                        CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("key", captchaKey);
        result.put("image", captchaBase64);

        return Result.success(result);
    }

    /**
     * 校验验证码是否正确（内部调用 / 登录时自动校验）
     *
     * @return true=正确 false=错误或已过期
     */
    public boolean verifyCaptcha(String captchaKey, String userInput) {
        if (captchaKey == null || userInput == null || userInput.trim().isEmpty()) {
            return false;
        }
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        try {
            Object cachedCode = redisTemplate.opsForValue().get(redisKey);
            if (cachedCode == null) {
                return false; // 已过期或不存在
            }
            // 校验后立即删除（一次性使用），防止重复利用
            redisTemplate.delete(redisKey);
            return cachedCode.toString().equalsIgnoreCase(userInput.trim());
        } catch (Exception e) {
            return false;
        }
    }
}
