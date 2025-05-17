const express = require('express');
const router = express.Router();
const paymentController = require('../controllers/paymentController');
const checkoutController = require('../controllers/checkoutController');

/**
 * Tạo yêu cầu thanh toán mới
 * POST /api/payments/create
 */
router.post('/create', paymentController.createPayment);

/**
 * Kiểm tra trạng thái thanh toán
 * GET /api/payments/status/:orderCode
 */
router.get('/status/:orderCode', paymentController.checkPaymentStatus);

/**
 * Webhook để nhận thông báo từ Payos
 * POST /api/payments/payos/webhook
 */
router.post('/payos/webhook', paymentController.handleWebhook);

/**
 * Hủy yêu cầu thanh toán
 * POST /api/payments/cancel/:orderCode
 */
router.post('/cancel/:orderCode', paymentController.cancelPayment);

/**
 * Lấy tất cả giao dịch thanh toán
 * GET /api/payments/transactions
 */
router.get('/transactions', paymentController.getAllTransactions);

/**
 * Lấy giao dịch thanh toán theo userId
 * GET /api/payments/transactions/user/:userId
 */
router.get('/transactions/user/:userId', paymentController.getUserTransactions);

/**
 * API xử lý quá trình thanh toán
 * POST /api/payments/checkout
 */
router.post('/checkout', checkoutController.processCheckout);

/**
 * API xử lý kết quả sau khi thanh toán
 * GET /api/payments/result
 */
router.get('/result', checkoutController.handlePaymentResult);

/**
 * API hủy đơn hàng
 * POST /api/payments/cancel/:orderCode
 */
router.post('/cancel/:orderCode', checkoutController.cancelOrder);

/**
 * API xác nhận đơn hàng đã thanh toán (dành cho admin)
 * POST /api/payments/confirm/:orderCode
 */
router.post('/confirm/:orderCode', checkoutController.confirmOrder);

/**
 * API lấy thông tin chi tiết thanh toán của đơn hàng
 * GET /api/payments/details/:orderCode
 */
router.get('/details/:orderCode', checkoutController.getPaymentDetails);

module.exports = router;
