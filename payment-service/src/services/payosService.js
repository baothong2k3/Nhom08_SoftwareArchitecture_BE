const axios = require('axios');
const crypto = require('crypto');
const { v4: uuidv4 } = require('uuid');

// Lấy thông tin cấu hình từ biến môi trường
const CLIENT_ID = process.env.PAYOS_CLIENT_ID;
const API_KEY = process.env.PAYOS_API_KEY;
const CHECKSUM_KEY = process.env.PAYOS_CHECKSUM_KEY;
const API_URL = process.env.PAYOS_API_URL || 'https://api-merchant.payos.vn';
const RETURN_URL = process.env.PAYOS_RETURN_URL || 'http://localhost:3333/payment-result';
const WEBHOOK_URL = process.env.PAYOS_WEBHOOK_URL || 'http://localhost:8989/api/payments/payos/webhook';

/**
 * Tạo mã checksum cho request
 * @param {Object} data - Dữ liệu request
 * @param {String} checksumKey - Checksum key từ Payos
 * @returns {String} - Checksum hash
 */
const createChecksum = (data, checksumKey = CHECKSUM_KEY) => {
  // Sắp xếp các khóa theo thứ tự ABC
  const keys = Object.keys(data).sort();
  const checkedData = {};

  // Tạo đối tượng mới với các khóa đã sắp xếp
  keys.forEach(key => {
    checkedData[key] = data[key];
  });

  // Chuyển đối tượng thành chuỗi JSON
  const stringifiedData = JSON.stringify(checkedData);

  // Tạo HMAC với SHA256
  return crypto
    .createHmac('sha256', checksumKey)
    .update(stringifiedData)
    .digest('hex');
};

/**
 * Xác minh webhook từ Payos
 * @param {Object} body - Body của webhook request
 * @param {String} signatureHeader - Chữ ký trong header webhook
 * @returns {Boolean} - Kết quả xác minh
 */
const verifyWebhook = (body, signatureHeader) => {
  const signature = createChecksum(body);
  return signature === signatureHeader;
};

/**
 * Tạo yêu cầu thanh toán mới
 * @param {Object} orderInfo - Thông tin đơn hàng
 * @returns {Promise<Object>} - Kết quả từ Payos API
 */
const createPaymentRequest = async (orderInfo) => {
  try {
    console.log("Thông tin nhận được:", JSON.stringify(orderInfo, null, 2));

    // Kiểm tra customerInfo
    if (!orderInfo.customerInfo) {
      throw new Error("Thiếu customerInfo trong orderInfo");
    }

    // Xử lý orderCode thành số nguyên (nếu có tiền tố ORDER-)
    let numericOrderCode;
    if (typeof orderInfo.orderCode === 'string' && orderInfo.orderCode.startsWith('ORDER-')) {
      numericOrderCode = orderInfo.orderCode.replace(/\D/g, ''); // Chỉ giữ lại số
    } else {
      numericOrderCode = orderInfo.orderCode;
    }

    const paymentData = {
      orderCode: numericOrderCode,
      amount: parseInt(orderInfo.amount),
      description: orderInfo.description || `Thanh toán đơn hàng ${orderInfo.orderCode}`,
      cancelUrl: orderInfo.cancelUrl || RETURN_URL,
      returnUrl: orderInfo.returnUrl || RETURN_URL,
      buyerName: orderInfo.customerInfo.fullName,
      buyerEmail: orderInfo.customerInfo.email,
      buyerPhone: orderInfo.customerInfo.phone,
      expiredAt: Math.floor((Date.now() + 24 * 60 * 60 * 1000) / 1000),
      webhookUrl: WEBHOOK_URL
    };

    // Thêm items nếu có
    if (orderInfo.items && orderInfo.items.length > 0) {
      paymentData.items = orderInfo.items.map(item => ({
        name: item.bookTitle || item.name,
        quantity: item.quantity,
        price: parseInt(item.price)
      }));
    }

    // Loại bỏ các trường undefined
    Object.keys(paymentData).forEach(key => {
      if (paymentData[key] === undefined) {
        delete paymentData[key];
      }
    });

    console.log('Payment request data:', JSON.stringify(paymentData, null, 2));
    console.log('API URL:', `${API_URL}/v2/payment-requests`);
    console.log('Client ID:', CLIENT_ID);
    console.log('API KEY:', API_KEY ? '[SET]' : '[MISSING]');
    console.log('CHECKSUM KEY:', CHECKSUM_KEY ? '[SET]' : '[MISSING]');

    const checksum = createChecksum(paymentData);
    console.log('Generated checksum:', checksum);

    // Gửi request đến Payos API
    const response = await axios.post(`${API_URL}/v2/payment-requests`, paymentData, {
      headers: {
        'x-client-id': CLIENT_ID,
        'x-api-key': API_KEY,
        'x-checksum': checksum,
        'Content-Type': 'application/json'
      }
    });

    console.log('Payos response:', JSON.stringify(response.data, null, 2));
    return response.data;

  } catch (error) {
    console.error('Lỗi khi tạo yêu cầu thanh toán:', error);
    if (error.response) {
      console.error('Response data:', JSON.stringify(error.response.data, null, 2));
      console.error('Response status:', error.response.status);
      console.error('Response headers:', error.response.headers);
      return error.response.data;
    }
    throw error;
  }
};

/**
 * Kiểm tra trạng thái thanh toán
 * @param {String} orderCode - Mã đơn hàng
 * @returns {Promise<Object>} - Thông tin trạng thái
 */
const checkPaymentStatus = async (orderCode) => {
  try {
    const response = await axios.get(`${API_URL}/v2/payment-requests/${orderCode}`, {
      headers: {
        'x-client-id': CLIENT_ID,
        'x-api-key': API_KEY
      }
    });

    return response.data;
  } catch (error) {
    console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error.response ? error.response.data : error.message);
    throw error;
  }
};

/**
 * Xác nhận thanh toán thành công
 * @param {String} orderCode - Mã đơn hàng
 * @returns {Promise<Object>} - Kết quả xác nhận
 */
const confirmPayment = async (orderCode) => {
  try {
    const data = { orderCode };
    const checksum = createChecksum(data);

    const response = await axios.post(`${API_URL}/v2/confirm-payment`, data, {
      headers: {
        'x-client-id': CLIENT_ID,
        'x-api-key': API_KEY,
        'x-checksum': checksum
      }
    });

    return response.data;
  } catch (error) {
    console.error('Lỗi khi xác nhận thanh toán:', error.response ? error.response.data : error.message);
    throw error;
  }
};

/**
 * Hủy yêu cầu thanh toán
 * @param {String} orderCode - Mã đơn hàng
 * @param {String} cancelReason - Lý do hủy (tối đa 255 ký tự)
 * @returns {Promise<Object>} - Kết quả hủy
 */
const cancelPayment = async (orderCode, cancelReason = 'Hủy bởi người dùng') => {
  try {
    const data = { orderCode, cancelReason };
    const checksum = createChecksum(data);

    const response = await axios.post(`${API_URL}/v2/cancel-payment-request`, data, {
      headers: {
        'x-client-id': CLIENT_ID,
        'x-api-key': API_KEY,
        'x-checksum': checksum
      }
    });

    return response.data;
  } catch (error) {
    console.error('Lỗi khi hủy thanh toán:', error.response ? error.response.data : error.message);
    throw error;
  }
};

module.exports = {
  createPaymentRequest,
  checkPaymentStatus,
  confirmPayment,
  cancelPayment,
  verifyWebhook,
  createChecksum
};
