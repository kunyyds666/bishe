package com.easypan.utils;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

public class CaptchaUtils {
    private static final Producer captchaProducer;

    static {
        Properties props = new Properties();
        props.put("kaptcha.textproducer.char.length", "1");
        props.put("kaptcha.image.width", "200");
        props.put("kaptcha.image.height", "50");
        Config config = new Config(props);
        captchaProducer = config.getProducerImpl();
    }

    public static void generateCaptcha(HttpServletResponse response, String code) throws IOException {
        BufferedImage image = captchaProducer.createImage(code);
        ImageIO.write(image, "jpg", response.getOutputStream());
    }

    public static String createText() {
        return captchaProducer.createText();
    }
}
