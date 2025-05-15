package bookstore.authservice.services;

public interface OtpService {
    String generateOtp(String phoneNumber);
    boolean verifyOtp(String phoneNumber, String otp);
}

