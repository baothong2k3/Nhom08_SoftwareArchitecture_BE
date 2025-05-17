const orderService = require('../services/orderService');
const { decodeToken } = require('../middleware/authMiddleware');

/**
 * Tạo đơn hàng mới
 */
exports.createOrder = async (req, res) => {
  try {
    const orderData = req.body;
    
    // Kiểm tra dữ liệu đầu vào
    if (!orderData.items || orderData.items.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Đơn hàng phải có ít nhất một sản phẩm'
      });
    }

    if (!orderData.totalAmount || orderData.totalAmount <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Tổng số tiền không hợp lệ'
      });
    }

    // Nếu có token, trích xuất userId từ token
    if (req.headers.authorization) {
      try {
        const token = req.headers.authorization.split(' ')[1];
        const decoded = decodeToken(token);
        
        if (decoded.success) {
          orderData.userId = decoded.user.userId;
        }
      } catch (error) {
        console.error('Lỗi giải mã token:', error.message);
      }
    }

    // Gọi service để tạo đơn hàng
    const result = await orderService.createOrder(orderData);
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: 'Không thể tạo đơn hàng',
        error: result.error
      });
    }

    return res.status(201).json({
      success: true,
      message: 'Tạo đơn hàng thành công',
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller tạo đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi tạo đơn hàng',
      error: error.message
    });
  }
};

/**
 * Lấy thông tin chi tiết đơn hàng
 */
exports.getOrderById = async (req, res) => {
  try {
    const { id } = req.params;
    
    if (!id) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu ID đơn hàng'
      });
    }
    
    const result = await orderService.getOrderById(id);
    
    if (!result.success) {
      return res.status(404).json({
        success: false,
        message: result.message || 'Không tìm thấy đơn hàng'
      });
    }
    
    // Kiểm tra quyền truy cập (nếu không phải admin, chỉ xem được đơn hàng của mình)
    if (req.user && req.user.role !== 'ADMIN' && result.order.userId !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Bạn không có quyền xem đơn hàng này'
      });
    }
    
    return res.status(200).json({
      success: true,
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller lấy thông tin đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi lấy thông tin đơn hàng',
      error: error.message
    });
  }
};

/**
 * Lấy danh sách đơn hàng
 */
exports.getOrders = async (req, res) => {
  try {
    const filters = {};
    
    // Nếu không phải admin, chỉ lấy đơn hàng của user đó
    if (req.user && req.user.role !== 'ADMIN') {
      filters.userId = req.user.userId;
    } 
    // Nếu là request từ API frontend và có userId trong query
    else if (req.query.userId) {
      filters.userId = req.query.userId;
    }
    
    // Lọc theo trạng thái nếu có
    if (req.query.status) {
      filters.status = req.query.status;
    }
    
    const result = await orderService.getOrders(filters);
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: 'Không thể lấy danh sách đơn hàng',
        error: result.error
      });
    }
    
    return res.status(200).json({
      success: true,
      orders: result.orders
    });
    
  } catch (error) {
    console.error('Lỗi controller lấy danh sách đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi lấy danh sách đơn hàng',
      error: error.message
    });
  }
};

/**
 * Hủy đơn hàng
 */
exports.cancelOrder = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;
    
    if (!id) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu ID đơn hàng'
      });
    }
    
    // Kiểm tra đơn hàng tồn tại không
    const orderCheck = await orderService.getOrderById(id);
    
    if (!orderCheck.success) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy đơn hàng'
      });
    }
    
    // Kiểm tra quyền hủy đơn hàng (nếu không phải admin, chỉ hủy được đơn hàng của mình)
    if (req.user && req.user.role !== 'ADMIN' && orderCheck.order.userId !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'Bạn không có quyền hủy đơn hàng này'
      });
    }
    
    const result = await orderService.cancelOrder(id, reason || 'Hủy bởi người dùng');
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: result.message || 'Không thể hủy đơn hàng',
        error: result.error
      });
    }
    
    return res.status(200).json({
      success: true,
      message: 'Hủy đơn hàng thành công',
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller hủy đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi hủy đơn hàng',
      error: error.message
    });
  }
};

/**
 * Cập nhật trạng thái đơn hàng
 */
exports.updateOrderStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;
    
    if (!id || !status) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu ID đơn hàng hoặc trạng thái mới'
      });
    }
    
    // Kiểm tra trạng thái hợp lệ
    const validStatuses = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
    
    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        message: 'Trạng thái không hợp lệ',
        validStatuses
      });
    }
    
    // Tìm order trước
    const orderCheck = await orderService.getOrderById(id);
    
    if (!orderCheck.success) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy đơn hàng'
      });
    }
    
    // Cập nhật trạng thái
    const result = await orderService.updateOrderStatus(orderCheck.order.orderCode, status);
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: result.message || 'Không thể cập nhật trạng thái đơn hàng',
        error: result.error
      });
    }
    
    return res.status(200).json({
      success: true,
      message: 'Cập nhật trạng thái đơn hàng thành công',
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller cập nhật trạng thái đơn hàng:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi cập nhật trạng thái đơn hàng',
      error: error.message
    });
  }
};

/**
 * Xử lý thanh toán hoàn tất (được gọi từ Payment service)
 */
exports.handlePaymentCompleted = async (req, res) => {
  try {
    const { orderCode } = req.body;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    const result = await orderService.handlePaymentCompleted(orderCode);
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: result.message || 'Không thể cập nhật trạng thái thanh toán',
        error: result.error
      });
    }
    
    return res.status(200).json({
      success: true,
      message: 'Cập nhật trạng thái thanh toán thành công',
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller xử lý thanh toán hoàn tất:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xử lý thanh toán hoàn tất',
      error: error.message
    });
  }
};

/**
 * Xử lý thanh toán thất bại (được gọi từ Payment service)
 */
exports.handlePaymentFailed = async (req, res) => {
  try {
    const { orderCode } = req.body;
    
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }
    
    const result = await orderService.handlePaymentFailed(orderCode);
    
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: result.message || 'Không thể cập nhật trạng thái thanh toán',
        error: result.error
      });
    }
    
    return res.status(200).json({
      success: true,
      message: 'Cập nhật trạng thái thanh toán thất bại thành công',
      order: result.order
    });
    
  } catch (error) {
    console.error('Lỗi controller xử lý thanh toán thất bại:', error);
    return res.status(500).json({
      success: false,
      message: 'Đã xảy ra lỗi khi xử lý thanh toán thất bại',
      error: error.message
    });
  }
};
