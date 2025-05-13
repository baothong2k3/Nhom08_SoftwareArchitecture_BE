package bookstore.authservice.services;

public interface OtpService {
    String generateOtp(String phoneNumber);
    String verifyOtp(String phoneNumber, String otp);
}

