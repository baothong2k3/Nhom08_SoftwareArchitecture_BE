const payosService = require('../services/payosService');
const axios = require('axios');
const PaymentTransaction = require('../models/PaymentTransaction');
const { decodeToken } = require('../middleware/authMiddleware');

/**
 * Xử lý yêu cầu thanh toán đơn hàng
 */
exports.processCheckout = async (req, res) => {
  try {
    const { orderCode, amount, shippingAddress, note, customerInfo, items, paymentMethod, token } = req.body;
    
    if (!amount || !customerInfo || !items || items.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu thông tin đơn hàng cần thiết'
      });
    }
    
    // Kiểm tra phương thức thanh toán hợp lệ (chỉ chấp nhận COD và BANK)
    if (paymentMethod !== 'COD' && paymentMethod !== 'BANK') {
      return res.status(400).json({
        success: false,
        message: 'Phương thức thanh toán không tồn tại. Chỉ hỗ trợ COD hoặc BANK'
      });
    }

    console.log('Checkout request:', { orderCode, amount, customerInfo, items: items.length, paymentMethod });
    
    // Xác thực và lấy thông tin người dùng từ token
    let userId = null;
    if (token) {
      const decoded = decodeToken(token);
      if (decoded.success) {
        userId = decoded.user.userId;
      }
    }

    // Tạo mã đơn hàng nếu chưa có
    const finalOrderCode = orderCode || `ORDER-${Date.now()}`;
    
    // Nếu là thanh toán COD, tạo đơn hàng và trả về ngay
    if (paymentMethod === 'COD') {
      try {
        // Tạo giao dịch trong database với trạng thái chờ thanh toán khi nhận hàng
        const transaction = await PaymentTransaction.create({
          orderCode: finalOrderCode,
          userId: userId,
          amount: amount,
          paymentMethod: 'COD',
          status: 'PENDING',
          paymentInfo: JSON.stringify({
            products: items,
            customerInfo: customerInfo,
            shippingAddress: shippingAddress,
            note: note
          }),
          description: `Thanh toán khi nhận hàng cho đơn #${finalOrderCode}`,
          customerName: customerInfo.fullName || 'Khách hàng',
          customerEmail: customerInfo.email || '',
          customerPhone: customerInfo.phone || ''
        });
        
        // Tạo đơn hàng bên Order Service
        try {
          await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders', {
            orderCode: finalOrderCode,
            userId: userId,
            totalAmount: amount,
            shippingAddress: shippingAddress,
            paymentMethod: 'COD',
            paymentStatus: 'PENDING',
            customerName: customerInfo.fullName,
            customerEmail: customerInfo.email,
            customerPhone: customerInfo.phone,
            notes: note,
            items: items
          });
        } catch (err) {
          console.error('Lỗi khi tạo đơn hàng ở Order Service:', err.message);
        }
        
        return res.status(200).json({
          success: true,
          message: 'Đặt hàng thành công với phương thức thanh toán khi nhận hàng',
          data: {
            orderCode: finalOrderCode,
            transactionId: transaction.id,
            paymentMethod: 'COD',
            amount: amount,
            redirectToOrderDetail: true
          }
        });
      } catch (error) {
        console.error('Lỗi khi xử lý đơn hàng COD:', error);
        return res.status(500).json({
          success: false,
          message: 'Đã xảy ra lỗi khi xử lý đơn hàng thanh toán khi nhận hàng',
          error: error.message
        });
      }
    } 
    
    // Nếu là thanh toán qua ngân hàng (BANK)
    else if (paymentMethod === 'BANK') {
      try {
        // Tạo thông tin thanh toán
        const paymentData = {
          orderCode: finalOrderCode,
          amount: parseInt(amount), // PayOS yêu cầu số nguyên
          description: `Thanh toán đơn hàng #${finalOrderCode}`,
          customerInfo: customerInfo, // Truyền trực tiếp customerInfo
          returnUrl: process.env.PAYOS_RETURN_URL,
          cancelUrl: process.env.PAYOS_RETURN_URL,
          // Truyền thông tin sản phẩm để hiển thị bên PayOS
          items: items
        };

        console.log('Gửi yêu cầu thanh toán với dữ liệu:', JSON.stringify(paymentData, null, 2));

        // Gọi service tạo yêu cầu thanh toán Payos
        const paymentResult = await payosService.createPaymentRequest(paymentData);
        
        console.log('Kết quả trả về từ Payos:', JSON.stringify(paymentResult, null, 2));
        
        // Check if there was an error
        if (paymentResult.code && paymentResult.code !== '00') {
          console.error('Yêu cầu thanh toán Payos thất bại:', paymentResult);
          return res.status(400).json({
            success: false,
            message: `Không thể tạo yêu cầu thanh toán: ${paymentResult.desc || 'Lỗi không xác định'}`,
            error: paymentResult
          });
        }
        
        if (!paymentResult.data || !paymentResult.data.checkoutUrl) {
          return res.status(500).json({
            success: false,
            message: 'Không nhận được URL thanh toán từ cổng thanh toán',
            error: paymentResult
          });
        }

        // Lưu thông tin giao dịch vào database
        const transaction = await PaymentTransaction.create({
          orderCode: finalOrderCode,
          userId: userId,
          amount: parseInt(amount),
          paymentMethod: 'BANK',
          status: 'PENDING',
          paymentInfo: JSON.stringify({
            products: items,
            customerInfo: customerInfo,
            shippingAddress: shippingAddress,
            note: note,
            payosResponse: paymentResult
          }),
          paymentUrl: paymentResult.data.checkoutUrl,
          description: `Thanh toán đơn hàng #${finalOrderCode}`,
          customerName: customerInfo.fullName,
          customerEmail: customerInfo.email,
          customerPhone: customerInfo.phone
        });

        // Tạo đơn hàng bên Order Service
        try {
          await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders', {
            orderCode: finalOrderCode,
            userId: userId,
            totalAmount: amount,
            shippingAddress: shippingAddress,
            paymentMethod: 'BANK',
            paymentStatus: 'PENDING',
            customerName: customerInfo.fullName,
            customerEmail: customerInfo.email,
            customerPhone: customerInfo.phone,
            notes: note,
            items: items
          });
        } catch (err) {
          console.error('Lỗi khi tạo đơn hàng ở Order Service:', err.message);
        }

        // Phản hồi cho client
        return res.status(200).json({
          success: true,
          message: 'Khởi tạo thanh toán thành công',
          data: {
            orderCode: finalOrderCode,
            transactionId: transaction.id,
            paymentUrl: paymentResult.data.checkoutUrl,
            paymentMethod: 'BANK',
            amount: parseInt(amount)
          }
        });
      } catch (error) {
        console.error('Lỗi khi xử lý thanh toán BANK:', error);
        return res.status(500).json({
          success: false,
          message: 'Đã xảy ra lỗi khi xử lý yêu cầu thanh toán ngân hàng',
          error: error.message
        });
      }
    }
  } catch (error) {
    console.error('Lỗi khi xử lý checkout:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xử lý yêu cầu thanh toán',
      error: error.message
    });
  }
};

/**
 * Xử lý kết quả trả về sau khi thanh toán
 */
exports.handlePaymentResult = async (req, res) => {
  try {
    const { orderCode, resultCode } = req.query;

    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Kiểm tra trạng thái thanh toán từ Payos
    const statusResult = await payosService.checkPaymentStatus(orderCode);
    const paymentStatus = statusResult.data?.status || 'UNKNOWN';
    
    // Cập nhật trạng thái trong database
    transaction.status = paymentStatus;
    transaction.paymentInfo = JSON.stringify({
      ...JSON.parse(transaction.paymentInfo || '{}'),
      paymentResult: statusResult.data
    });
    
    if (paymentStatus === 'PAID') {
      transaction.paymentDate = new Date();
      
      // Thực hiện cập nhật đơn hàng
      try {
        await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/payment-completed', {
          orderCode: orderCode
        });
        
        // Xóa các mục trong giỏ hàng
        const paymentInfo = JSON.parse(transaction.paymentInfo);
        const productIds = paymentInfo.products.map(p => p.id);
        
        if (transaction.userId) {
          await axios.post(process.env.CART_SERVICE_URL + '/api/cart/checkout', {
            userId: transaction.userId,
            productIds: productIds
          });
        }
      } catch (err) {
        console.error('Lỗi khi cập nhật đơn hàng hoặc giỏ hàng:', err.message);
      }
    }
    
    await transaction.save();
    
    // Trả về kết quả
    return res.status(200).json({
      success: true,
      orderCode: orderCode,
      status: paymentStatus,
      transaction: {
        id: transaction.id,
        amount: transaction.amount,
        paymentDate: transaction.paymentDate,
        customerName: transaction.customerName
      }
    });
    
  } catch (error) {
    console.error('Lỗi khi xử lý kết quả thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xử lý kết quả thanh toán',
      error: error.message
    });
  }
};

/**
 * Xử lý hủy đơn hàng
 */
exports.cancelOrder = async (req, res) => {
  try {
    const { orderCode } = req.params;
    const { reason } = req.body;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Kiểm tra giao dịch tồn tại hay không
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Hủy giao dịch ở Payos
    try {
      const cancelReason = reason || 'Hủy bởi người dùng';
      await payosService.cancelPayment(orderCode, cancelReason);
    } catch (err) {
      console.error('Lỗi khi hủy thanh toán ở Payos:', err.message);
      // Tiếp tục xử lý ngay cả khi API Payos lỗi
    }
    
    // Cập nhật trạng thái giao dịch
    transaction.status = 'CANCELLED';
    await transaction.save();
    
    // Cập nhật trạng thái đơn hàng
    try {
      await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/cancel-order', {
        orderCode: orderCode,
        reason: cancelReason
      });
    } catch (err) {
      console.error('Lỗi khi cập nhật trạng thái đơn hàng:', err.message);
    }
    
    return res.status(200).json({
      success: true,
      message: 'Đã hủy đơn hàng thành công',
      orderCode: orderCode
    });
    
  } catch (error) {
    console.error('Lỗi khi hủy đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi hủy đơn hàng',
      error: error.message
    });
  }
};

/**
 * Xác nhận đơn hàng đã hoàn thành (dùng cho quản trị viên)
 */
exports.confirmOrder = async (req, res) => {
  try {
    const { orderCode } = req.params;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Xác nhận thanh toán
    try {
      await payosService.confirmPayment(orderCode);
    } catch (err) {
      console.error('Lỗi khi xác nhận thanh toán ở Payos:', err.message);
      // Tiếp tục xử lý nếu API lỗi
    }
    
    // Cập nhật trạng thái
    transaction.status = 'PAID';
    transaction.paymentDate = new Date();
    await transaction.save();
    
    // Cập nhật trạng thái đơn hàng
    try {
      await axios.post(process.env.ORDER_SERVICE_URL + '/api/orders/payment-completed', {
        orderCode: orderCode
      });
    } catch (err) {
      console.error('Lỗi khi cập nhật trạng thái đơn hàng:', err.message);
    }
    
    return res.status(200).json({
      success: true,
      message: 'Đã xác nhận thanh toán thành công',
      orderCode: orderCode
    });
    
  } catch (error) {
    console.error('Lỗi khi xác nhận thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xác nhận thanh toán',
      error: error.message
    });
  }
};

/**
 * Lấy thông tin thanh toán của đơn hàng
 */
exports.getPaymentDetails = async (req, res) => {
  try {
    const { orderCode } = req.params;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    // Tìm giao dịch trong database
    const transaction = await PaymentTransaction.findOne({
      where: { orderCode: orderCode }
    });
    
    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy giao dịch'
      });
    }
    
    // Định dạng thông tin trả về
    const paymentInfo = transaction.paymentInfo ? JSON.parse(transaction.paymentInfo) : {};
    
    return res.status(200).json({
      success: true,
      payment: {
        id: transaction.id,
        orderCode: transaction.orderCode,
        userId: transaction.userId,
        status: transaction.status,
        amount: transaction.amount,
        paymentMethod: transaction.paymentMethod,
        paymentDate: transaction.paymentDate,
        customerInfo: {
          name: transaction.customerName,
          email: transaction.customerEmail,
          phone: transaction.customerPhone
        },
        details: paymentInfo,
        createdAt: transaction.createdAt,
        updatedAt: transaction.updatedAt
      }
    });
    
  } catch (error) {
    console.error('Lỗi khi lấy thông tin thanh toán:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi lấy thông tin thanh toán',
      error: error.message
    });
  }
};
