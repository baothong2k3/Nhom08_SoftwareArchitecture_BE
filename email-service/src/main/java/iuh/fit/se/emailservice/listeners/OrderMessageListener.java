package iuh.fit.se.emailservice.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.emailservice.dtos.MessageResponse;
import iuh.fit.se.emailservice.services.events.EmailService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class OrderMessageListener {

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "${app.rabbit.queue-name}")
    public void receiveOrder(Message message) {
        try {
            String body = new String(message.getBody());
            ObjectMapper mapper = new ObjectMapper();
            MessageResponse messageResponse = mapper.readValue(body, MessageResponse.class);

            String toEmail = messageResponse.getEmail();
            String subject = "Xác Nhận Đặt Hàng Thành Công";

            // Format order details
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedTotalPrice = currencyFormatter.format(messageResponse.getTotalPrice());

            // Create professional email content
            String content = String.format(
                    "Kính gửi Quý Khách Hàng,\n\n" +
                            "Cảm ơn Quý Khách đã tin tưởng và đặt hàng tại cửa hàng của chúng tôi! Đơn hàng của Quý Khách đã được tiếp nhận thành công. Dưới đây là thông tin chi tiết về đơn hàng:\n\n" +
                            "--------------------------------------------\n" +
                            "Mã đơn hàng: DH%03d\n" +
                            "Ngày đặt hàng: %s\n" +
                            "Địa chỉ giao hàng: %s\n" +
                            "Số điện thoại: %s\n" +
                            "Phương thức thanh toán: %s\n" +
                            "Tổng giá trị đơn hàng: %s\n" +
                            "--------------------------------------------\n\n" +
                            "Chúng tôi sẽ sớm xử lý và cập nhật trạng thái đơn hàng cho Quý Khách. Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email %s hoặc số điện thoại +84-123-456-789.\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ Cửa Hàng Book Store\n" +
                            "Website: www.bookstore.vn",
                    messageResponse.getId(),
                    messageResponse.getCreatedAt(),
                    messageResponse.getShippingAddress(),
                    messageResponse.getPhoneNumber(),
                    messageResponse.getPaymentMethod(),
                    formattedTotalPrice,
                    "nguyenphan1122k3@gmail.vn"
            );

            emailService.sendEmail(toEmail, subject, content);
        } catch (JsonProcessingException e) {
            System.err.println("Lỗi khi xử lý JSON message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửime xử lý message: " + e.getMessage());
        }
    }
}