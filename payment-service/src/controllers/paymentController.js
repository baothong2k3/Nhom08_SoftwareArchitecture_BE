const payosService = require('../services/payosService');
const { decodeToken } = require('../middleware/authMiddleware');
const axios = require('axios');
const PaymentTransaction = require('../models/PaymentTransaction');

/**
 * Tạo yêu cầu thanh toán mới
 */
exports.createPayment = async (req, res) => {
  try {
    const { amount, orderCode, description, token, items } = req.body;
    
    if (!amount || amount <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Số tiền thanh toán không hợp lệ'
      });
    }
    
    // Lấy userId từ token nếu có
    let userId = null;
    if (token) {
      const decoded = decodeToken(token);
      if (decoded.success) {
        userId = decoded.user.userId;
      }
    }
    
    // Tạo thông tin đơn hàng
    const transactionCode = orderCode || `ORDER-${Date.now()}`;
    const orderInfo = {
      orderCode: transactionCode,
      amount: Math.round(amount), // Payos yêu cầu số nguyên
      description: description || `Thanh toán đơn hàng ${transactionCode}`,
      customerName: req.body.customerName || 'Khách hàng',
      customerEmail: req.body.customerEmail || '',
      customerPhone: req.body.customerPhone || '',
      items: items || [],
      userId: userId
    };
    
    // Gọi service để tạo yêu cầu thanh toán
    const paymentResult = await payosService.createPaymentRequest(orderInfo);
    
    // Lưu thông tin thanh toán vào database
    const transaction = await PaymentTransaction.create({
      orderCode: transactionCode,
      userId: userId,
      amount: orderInfo.amount,
      paymentMethod: 'PAYOS',
      status: 'PENDING',
      paymentInfo: JSON.stringify(paymentResult),
      paymentUrl: paymentResult.data?.checkoutUrl,
      description: orderInfo.description,
      customerName: orderInfo.customerName,
      customerEmail: orderInfo.customerEmail,
      customerPhone: orderInfo.customerPhone
    });
    
    return res.status(200).json({
      success: true,
      message: 'Tạo yêu cầu thanh toán thành công',
      paymentUrl: paymentResult.data?.checkoutUrl,
      orderCode: transactionCode,
      transactionId: transaction.id
    });
  } catch (error) {
    console.error('Lỗi khi tạo yêu cầu thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi tạo yêu cầu thanh toán',
      error: error.message
    });
  }
};

/**
 * Kiểm tra trạng thái thanh toán
 */
exports.checkPaymentStatus = async (req, res) => {
  try {
    const { orderCode } = req.params;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Kiểm tra trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Gọi API Payos để lấy trạng thái mới nhất
    try {
      const statusResult = await payosService.checkPaymentStatus(orderCode);
      const paymentStatus = statusResult.data.status;
      
      // Cập nhật trạng thái trong database
      if (paymentStatus !== transaction.status) {
        transaction.status = paymentStatus;
        transaction.paymentInfo = JSON.stringify(statusResult.data);
        if (paymentStatus === 'PAID') {
          transaction.paymentDate = new Date();
        }
        await transaction.save();
      }
      
      return res.status(200).json({
        success: true,
        status: paymentStatus,
        transaction: {
          id: transaction.id,
          orderCode: transaction.orderCode,
          amount: transaction.amount,
          status: transaction.status,
          paymentDate: transaction.paymentDate,
          customerName: transaction.customerName,
        }
      });
    } catch (error) {
      // Nếu không thể kết nối đến Payos, trả về trạng thái từ database
      return res.status(200).json({
        success: true,
        status: transaction.status,
        message: 'Trạng thái lấy từ cơ sở dữ liệu, không thể kết nối đến cổng thanh toán',
        transaction: {
          id: transaction.id,
          orderCode: transaction.orderCode,
          amount: transaction.amount,
          status: transaction.status,
          paymentDate: transaction.paymentDate,
          customerName: transaction.customerName,
        }
      });
    }
  } catch (error) {
    console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi kiểm tra trạng thái thanh toán',
      error: error.message
    });
  }
};

/**
 * Xử lý webhook từ Payos
 */
exports.handleWebhook = async (req, res) => {
  try {
    const signature = req.headers['x-signature'];
    const webhookData = req.body;
    
    // Xác minh chữ ký webhook
    const isValid = payosService.verifyWebhook(webhookData, signature);
    
    if (!isValid) {
      console.error('Webhook signature không hợp lệ');
      return res.status(400).json({
        success: false,
        message: 'Chữ ký không hợp lệ'
      });
    }
    
    // Xử lý dữ liệu webhook dựa vào trạng thái thanh toán
    const { status, orderCode } = webhookData.data;
    
    console.log(`Nhận webhook cho đơn hàng ${orderCode} với trạng thái ${status}`);
    
    // Cập nhật trạng thái giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (transaction) {
      transaction.status = status;
      transaction.paymentInfo = JSON.stringify({
        ...JSON.parse(transaction.paymentInfo || '{}'),
        webhookData: webhookData
      });
      
      if (status === 'PAID') {
        transaction.paymentDate = new Date();
      }
      
      await transaction.save();
    }
    
    // Xử lý theo trạng thái
    const webhookService = require('../services/webhookService');
    
    if (status === 'PAID') {
      await webhookService.handlePaymentSuccess(webhookData.data);
    } else if (status === 'FAILED') {
      await webhookService.handlePaymentFailure(webhookData.data);
    } else if (status === 'CANCELLED') {
      await webhookService.handlePaymentCancelled(webhookData.data);
    }
    
    return res.status(200).json({
      success: true,
      message: 'Webhook đã được xử lý'
    });
  } catch (error) {
    console.error('Lỗi khi xử lý webhook:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xử lý webhook',
      error: error.message
    });
  }
};

/**
 * Hủy yêu cầu thanh toán
 */
exports.cancelPayment = async (req, res) => {
  try {
    const { orderCode } = req.params;
    const { cancelReason } = req.body;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Kiểm tra xem giao dịch có tồn tại không
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Gọi API để hủy thanh toán
    const cancelResult = await payosService.cancelPayment(orderCode, cancelReason);
    
    // Cập nhật trạng thái trong database
    transaction.status = 'CANCELLED';
    transaction.paymentInfo = JSON.stringify(cancelResult);
    await transaction.save();
    
    return res.status(200).json({
      success: true,
      message: 'Hủy yêu cầu thanh toán thành công',
      data: {
        orderCode: transaction.orderCode,
        status: 'CANCELLED',
        cancelReason: cancelReason
      }
    });
  } catch (error) {
    console.error('Lỗi khi hủy yêu cầu thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi hủy yêu cầu thanh toán',
      error: error.message
    });
  }
};

/**
 * Lấy tất cả giao dịch thanh toán
 */
exports.getAllTransactions = async (req, res) => {
  try {
    const transactions = await PaymentTransaction.findAll({
      order: [['createdAt', 'DESC']]
    });
    
    return res.status(200).json({
      success: true,
      transactions: transactions
    });
  } catch (error) {
    console.error('Lỗi khi lấy danh sách giao dịch:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi lấy danh sách giao dịch',
      error: error.message
    });
  }
};

/**
 * Lấy giao dịch thanh toán theo userId
 */
exports.getUserTransactions = async (req, res) => {
  try {
    const { userId } = req.params;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu ID người dùng'
      });
    }
    
    const transactions = await PaymentTransaction.findAll({
      where: { userId: userId },
      order: [['createdAt', 'DESC']]
    });
    
    return res.status(200).json({
      success: true,
      transactions: transactions
    });
  } catch (error) {
    console.error('Lỗi khi lấy danh sách giao dịch của người dùng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi lấy danh sách giao dịch của người dùng',
      error: error.message
    });
  }
};
