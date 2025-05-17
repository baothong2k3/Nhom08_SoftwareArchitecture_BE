const { v4: uuidv4 } = require('uuid');
const Order = require('../models/Order');
const OrderItem = require('../models/OrderItem');
const { sequelize } = require('../config/database');
const axios = require('axios');

/**
 * Tạo đơn hàng mới
 * @param {Object} orderData - Thông tin đơn hàng
 * @returns {Promise<Object>} - Đơn hàng đã tạo
 */
exports.createOrder = async (orderData) => {
  const transaction = await sequelize.transaction();
  
  try {
    // Tạo mã đơn hàng nếu chưa có
    if (!orderData.orderCode) {
      orderData.orderCode = `ORD-${Date.now()}-${uuidv4().substring(0, 8)}`;
    }
    
    // Mặc định trạng thái là PENDING nếu không được chỉ định
    if (!orderData.status) {
      orderData.status = 'PENDING';
    }
    
    // Tạo đơn hàng
    const order = await Order.create({
      orderCode: orderData.orderCode,
      userId: orderData.userId,
      status: orderData.status,
      totalAmount: orderData.totalAmount,
      shippingAddress: orderData.shippingAddress,
      paymentMethod: orderData.paymentMethod || 'PAYOS',
      paymentStatus: 'PENDING',
      shippingFee: orderData.shippingFee || 0,
      customerName: orderData.customerName,
      customerEmail: orderData.customerEmail,
      customerPhone: orderData.customerPhone,
      notes: orderData.notes
    }, { transaction });
    
    // Tạo các mục đơn hàng
    if (orderData.items && orderData.items.length > 0) {
      const orderItems = orderData.items.map(item => ({
        orderId: order.id,
        bookId: item.bookId,
        quantity: item.quantity,
        price: item.price,
        discount: item.discount || 0,
        bookTitle: item.bookTitle,
        bookImage: item.bookImage
      }));
      
      await OrderItem.bulkCreate(orderItems, { transaction });
    }
    
    await transaction.commit();
    
    // Lấy đơn hàng đã tạo cùng với các mục
    const createdOrder = await Order.findByPk(order.id, {
      include: [{ model: OrderItem, as: 'items' }]
    });
    
    return {
      success: true,
      order: createdOrder
    };
  } catch (error) {
    await transaction.rollback();
    console.error('Lỗi khi tạo đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Lấy thông tin chi tiết đơn hàng
 * @param {number} orderId - ID đơn hàng
 * @returns {Promise<Object>} - Thông tin đơn hàng
 */
exports.getOrderById = async (orderId) => {
  try {
    const order = await Order.findByPk(orderId, {
      include: [{ model: OrderItem, as: 'items' }]
    });
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    return {
      success: true,
      order
    };
  } catch (error) {
    console.error('Lỗi khi lấy thông tin đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Lấy thông tin đơn hàng theo mã đơn hàng
 * @param {string} orderCode - Mã đơn hàng
 * @returns {Promise<Object>} - Thông tin đơn hàng
 */
exports.getOrderByCode = async (orderCode) => {
  try {
    const order = await Order.findOne({
      where: { orderCode },
      include: [{ model: OrderItem, as: 'items' }]
    });
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    return {
      success: true,
      order
    };
  } catch (error) {
    console.error('Lỗi khi lấy thông tin đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Lấy danh sách đơn hàng
 * @param {Object} filters - Điều kiện lọc
 * @returns {Promise<Object>} - Danh sách đơn hàng
 */
exports.getOrders = async (filters = {}) => {
  try {
    const where = {};
    
    // Lọc theo userId nếu có
    if (filters.userId) {
      where.userId = filters.userId;
    }
    
    // Lọc theo status nếu có
    if (filters.status) {
      where.status = filters.status;
    }
    
    const orders = await Order.findAll({
      where,
      include: [{ model: OrderItem, as: 'items' }],
      order: [['createdAt', 'DESC']]
    });
    
    return {
      success: true,
      orders
    };
  } catch (error) {
    console.error('Lỗi khi lấy danh sách đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Hủy đơn hàng
 * @param {number} orderId - ID đơn hàng
 * @param {string} reason - Lý do hủy
 * @returns {Promise<Object>} - Kết quả hủy đơn hàng
 */
exports.cancelOrder = async (orderId, reason) => {
  try {
    const order = await Order.findByPk(orderId);
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    // Kiểm tra nếu đơn hàng đã thanh toán thì không thể hủy
    if (order.status === 'PAID' || order.status === 'SHIPPED' || order.status === 'DELIVERED') {
      return {
        success: false,
        message: 'Không thể hủy đơn hàng đã thanh toán hoặc đang vận chuyển'
      };
    }
    
    // Cập nhật trạng thái đơn hàng
    order.status = 'CANCELLED';
    order.cancelReason = reason;
    await order.save();
    
    return {
      success: true,
      message: 'Hủy đơn hàng thành công',
      order
    };
  } catch (error) {
    console.error('Lỗi khi hủy đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Cập nhật trạng thái đơn hàng
 * @param {string} orderCode - Mã đơn hàng
 * @param {string} status - Trạng thái mới
 * @returns {Promise<Object>} - Kết quả cập nhật
 */
exports.updateOrderStatus = async (orderCode, status) => {
  try {
    const order = await Order.findOne({ where: { orderCode } });
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    // Cập nhật trạng thái
    order.status = status;
    
    // Nếu trạng thái là PAID (đã thanh toán), cập nhật ngày thanh toán
    if (status === 'PAID') {
      order.paymentStatus = 'PAID';
      order.paymentDate = new Date();
    }
    
    // Nếu trạng thái là SHIPPED (đang vận chuyển), cập nhật ngày gửi hàng
    if (status === 'SHIPPED') {
      order.shippingDate = new Date();
    }
    
    // Nếu trạng thái là DELIVERED (đã giao hàng), cập nhật ngày giao hàng
    if (status === 'DELIVERED') {
      order.deliveryDate = new Date();
    }
    
    await order.save();
    
    return {
      success: true,
      message: 'Cập nhật trạng thái đơn hàng thành công',
      order
    };
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái đơn hàng:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Xử lý thanh toán hoàn tất
 * @param {string} orderCode - Mã đơn hàng
 * @returns {Promise<Object>} - Kết quả xử lý
 */
exports.handlePaymentCompleted = async (orderCode) => {
  try {
    const order = await Order.findOne({ where: { orderCode } });
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    // Cập nhật trạng thái đơn hàng
    order.status = 'PAID';
    order.paymentStatus = 'PAID';
    order.paymentDate = new Date();
    await order.save();
    
    return {
      success: true,
      message: 'Cập nhật trạng thái thanh toán thành công',
      order
    };
  } catch (error) {
    console.error('Lỗi khi xử lý thanh toán hoàn tất:', error);
    return {
      success: false,
      error: error.message
    };
  }
};

/**
 * Xử lý thanh toán thất bại
 * @param {string} orderCode - Mã đơn hàng
 * @returns {Promise<Object>} - Kết quả xử lý
 */
exports.handlePaymentFailed = async (orderCode) => {
  try {
    const order = await Order.findOne({ where: { orderCode } });
    
    if (!order) {
      return {
        success: false,
        message: 'Không tìm thấy đơn hàng'
      };
    }
    
    // Cập nhật trạng thái đơn hàng
    order.paymentStatus = 'FAILED';
    await order.save();
    
    return {
      success: true,
      message: 'Cập nhật trạng thái thanh toán thất bại',
      order
    };
  } catch (error) {
    console.error('Lỗi khi xử lý thanh toán thất bại:', error);
    return {
      success: false,
      error: error.message
    };
  }
};
