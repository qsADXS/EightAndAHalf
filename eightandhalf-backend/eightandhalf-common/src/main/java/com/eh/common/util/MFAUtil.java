package com.eh.common.util;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class MFAUtil {

    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String generateQRCode(String otpUri, String filePath) throws IOException, WriterException {
        // Create a QR code from the otpUri
        var writer = new QRCodeWriter();
        var bitMatrix = writer.encode(otpUri, com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        ImageIO.write(image, "png", new File(filePath));
        // 将 BufferedImage 转换为 Base64 编码
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return "data:image/png;base64," + base64Image; // 返回完整的 Base64 字符串
    }

    public static boolean verifyCode(String secret, String code) throws InvalidKeyException {
        // 将 Base32 编码的密钥转换为 byte[]
        Base32 base32 = new Base32();
        byte[] decodedKey = base32.decode(secret);

        // 使用 SecretKeySpec 创建 Key 对象
        Key key = new SecretKeySpec(decodedKey, "HmacSHA1");
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        Instant now = Instant.now();

        // 生成当前时间的验证码
        String generatedCode = String.format("%06d", totp.generateOneTimePassword(key, now));

        return generatedCode.equals(code);
    }
}
