const express = require('express');
const router = express.Router();
const orderController = require('../controllers/orderController');
const { authenticateToken } = require('../middleware/authMiddleware');

/**
 * POST /api/orders
 * Tạo đơn hàng mới
 */
router.post('/', orderController.createOrder);

/**
 * GET /api/orders/:id
 * Lấy chi tiết 1 đơn hàng
 */
router.get('/:id', authenticateToken, orderController.getOrderById);

/**
 * GET /api/orders
 * Lấy danh sách đơn hàng (theo user hoặc all)
 */
router.get('/', authenticateToken, orderController.getOrders);

/**
 * PUT /api/orders/:id/cancel
 * Hủy đơn hàng (nếu chưa thanh toán)
 */
router.put('/:id/cancel', authenticateToken, orderController.cancelOrder);

/**
 * PUT /api/orders/:id/status
 * Cập nhật trạng thái đơn hàng (bởi admin)
 */
router.put('/:id/status', authenticateToken, orderController.updateOrderStatus);

/**
 * POST /api/orders/payment-completed
 * Cập nhật trạng thái khi thanh toán hoàn tất (được gọi từ Payment service)
 */
router.post('/payment-completed', orderController.handlePaymentCompleted);

/**
 * POST /api/orders/payment-failed
 * Cập nhật trạng thái khi thanh toán thất bại (được gọi từ Payment service)
 */
router.post('/payment-failed', orderController.handlePaymentFailed);

/**
 * POST /api/orders/cancel-order
 * Hủy đơn hàng khi thanh toán bị hủy (được gọi từ Payment service)
 */
router.post('/cancel-order', orderController.cancelOrder);

module.exports = router;
