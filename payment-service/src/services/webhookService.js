const PaymentTransaction = require('../models/PaymentTransaction');
const axios = require('axios');

/**
 * Xử lý webhook từ Payos khi thanh toán thành công
 * @param {Object} paymentData - Dữ liệu thanh toán từ webhook
 */
exports.handlePaymentSuccess = async (paymentData) => {
  try {
    const { orderCode } = paymentData;
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      console.error(`Không tìm thấy giao dịch với mã: ${orderCode}`);
      return false;
    }
    
    // Cập nhật trạng thái
    transaction.status = 'PAID';
    transaction.paymentDate = new Date();
    transaction.paymentInfo = JSON.stringify({
      ...JSON.parse(transaction.paymentInfo || '{}'),
      webhookData: paymentData
    });
    await transaction.save();
    
    // Thực hiện cập nhật đơn hàng và xóa giỏ hàng
    try {
      // Cập nhật trạng thái đơn hàng
      await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/payment-completed', {
        orderCode: orderCode
      });
      
      // Lấy thông tin sản phẩm và xóa khỏi giỏ hàng
      if (transaction.userId) {
        const paymentInfo = JSON.parse(transaction.paymentInfo);
        const productIds = paymentInfo.products?.map(p => p.id);
        
        if (productIds && productIds.length > 0) {
          await axios.post(process.env.CART_SERVICE_URL + '/api/cart/checkout', {
            userId: transaction.userId,
            productIds: productIds
          });
        }
      }
      
      return true;
    } catch (error) {
      console.error('Lỗi khi xử lý sau thanh toán thành công:', error);
      return false;
    }
  } catch (error) {
    console.error('Lỗi khi xử lý webhook thanh toán thành công:', error);
    return false;
  }
};

/**
 * Xử lý webhook từ Payos khi thanh toán thất bại
 * @param {Object} paymentData - Dữ liệu thanh toán từ webhook
 */
exports.handlePaymentFailure = async (paymentData) => {
  try {
    const { orderCode } = paymentData;
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      console.error(`Không tìm thấy giao dịch với mã: ${orderCode}`);
      return false;
    }
    
    // Cập nhật trạng thái
    transaction.status = 'FAILED';
    transaction.paymentInfo = JSON.stringify({
      ...JSON.parse(transaction.paymentInfo || '{}'),
      webhookData: paymentData
    });
    await transaction.save();
    
    // Cập nhật trạng thái đơn hàng
    try {
      await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/payment-failed', {
        orderCode: orderCode
      });
      return true;
    } catch (error) {
      console.error('Lỗi khi cập nhật trạng thái đơn hàng thất bại:', error);
      return false;
    }
  } catch (error) {
    console.error('Lỗi khi xử lý webhook thanh toán thất bại:', error);
    return false;
  }
};

/**
 * Xử lý webhook từ Payos khi thanh toán bị hủy
 * @param {Object} paymentData - Dữ liệu thanh toán từ webhook
 */
exports.handlePaymentCancelled = async (paymentData) => {
  try {
    const { orderCode } = paymentData;
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      console.error(`Không tìm thấy giao dịch với mã: ${orderCode}`);
      return false;
    }
    
    // Cập nhật trạng thái
    transaction.status = 'CANCELLED';
    transaction.paymentInfo = JSON.stringify({
      ...JSON.parse(transaction.paymentInfo || '{}'),
      webhookData: paymentData
    });
    await transaction.save();
    
    // Cập nhật trạng thái đơn hàng
    try {
      await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/cancel-order', {
        orderCode: orderCode,
        reason: 'Hủy thanh toán qua cổng thanh toán'
      });
      return true;
    } catch (error) {
      console.error('Lỗi khi cập nhật trạng thái đơn hàng đã hủy:', error);
      return false;
    }
  } catch (error) {
    console.error('Lỗi khi xử lý webhook thanh toán bị hủy:', error);
    return false;
  }
};
