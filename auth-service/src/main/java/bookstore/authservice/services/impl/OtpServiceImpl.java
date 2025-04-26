package bookstore.authservice.services.impl;

import bookstore.authservice.services.OtpService;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

@Service
public class OtpServiceImpl implements OtpService {
    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.serviceId}") // ID của Twilio Verify Service
    private String serviceId;

    @Value("${spring.profiles.active:}")
    private String activeProfile;


    @PostConstruct
    public void init() {
        if (!"dev".equals(activeProfile)) {
            Twilio.init(accountSid, authToken);
        }
    }

    @Override
    public String generateOtp(String phoneNumber) {
        try {
            String formattedPhoneNumber = normalizePhoneNumber(phoneNumber);
            System.out.println("Sending OTP to: " + formattedPhoneNumber);
            // Nếu đang chạy trong môi trường "dev", trả về OTP mặc định
            if ("dev".equals(activeProfile)) {
                System.out.println("[DEV MODE] OTP for " + formattedPhoneNumber + " is 123456");
                return "OTP sent successfully! (DEV MODE: 123456)";
            }

            // Gửi OTP qua Twilio Verify Service
            Verification verification = Verification.creator(
                    serviceId,
                    formattedPhoneNumber,
                    "sms"
            ).create();

            // Kiểm tra trạng thái của yêu cầu gửi OTP
            if ("pending".equals(verification.getStatus())) {
                return "OTP sent successfully!";
            } else {
                return "Failed to send OTP: " + verification.getStatus();
            }
        } catch (ApiException e) {
            return "Failed to send OTP: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid phone number: " + e.getMessage();
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.trim();
        if (phoneNumber.startsWith("+84")) {
            return phoneNumber;
        }
        if (phoneNumber.startsWith("0")) {
            return "+84" + phoneNumber.substring(1);
        }
        throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        try {
            String formattedPhoneNumber = normalizePhoneNumber(phoneNumber);

            // Nếu đang chạy trong môi trường "dev", OTP mặc định là 123456
            if ("dev".equals(activeProfile)) {
                return "123456".equals(otp);
            }

            // Xác thực OTP qua Twilio Verify Service
            VerificationCheck verificationCheck = VerificationCheck.creator(serviceId)
                    .setTo(formattedPhoneNumber)
                    .setCode(otp)
                    .create();

            return "approved".equals(verificationCheck.getStatus());
        } catch (ApiException e) {
            System.err.println("Failed to verify OTP: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid phone number: " + e.getMessage());
            return false;
        }
    }
}
