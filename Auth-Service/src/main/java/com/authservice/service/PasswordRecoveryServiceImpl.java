package com.authservice.service;

import com.authservice.client.UserClient;
import com.authservice.dto.ChangePasswordRequest;
import com.authservice.dto.PasswordUpdateRequest;
import com.authservice.dto.UserDTO;
import com.authservice.exception.InvalidOTPException;
import com.authservice.exception.UserNotFoundException;
import com.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final RedisTemplate<String, String> redisTemplate;

    private final JavaMailSender javaMailSender;
    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private static final long OTP_EXPIRATION_MINUTES = 2;

    public PasswordRecoveryServiceImpl(RedisTemplate<String, String> redisTemplate, JavaMailSender javaMailSender, UserClient userClient, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.javaMailSender = javaMailSender;
        this.userClient = userClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Override
    public String sendOtpToEmail(String email) {
        UserDTO user = userClient.getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found for email: " + email);
        }

        String otp = generateOtp();
        redisTemplate.opsForValue().set("OTP:" + email, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(email);
        mail.setSubject("OTP for Password Reset");
        mail.setText("Your OTP is: " + otp + ". It expires in 2 minutes.");
        javaMailSender.send(mail);

        return "OTP sent to email";
    }

    @Override
    public String verifyOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP:" + email);
        if (storedOtp == null || !storedOtp.equals(otp)) throw new InvalidOTPException("Invalid or expired OTP");

        redisTemplate.delete("OTP:" + email);
        return jwtUtil.generateToken(userClient.getUserByEmail(email));
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request) {

        userClient.updatePassword(request);

        return true;
    }

    private String generateOtp() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            otp.append(chars.charAt(random.nextInt(chars.length())));
        }
        return otp.toString();
    }
}
