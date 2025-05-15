const express = require("express");
const router = express.Router();
const paymentController = require("../controllers/paymentController");
const checkoutController = require("../controllers/checkoutController");

// Các routes xử lý thanh toán cơ bản
router.post("/create", paymentController.createPayment);
router.get("/status/:orderCode", paymentController.checkPaymentStatus);
router.post("/webhook", paymentController.handleWebhook);
router.post("/cancel/:orderCode", paymentController.cancelPayment);

// Routes lấy danh sách giao dịch
router.get("/transactions", paymentController.getAllTransactions);
router.get("/transactions/user/:userId", paymentController.getUserTransactions);

// Routes xử lý checkout
router.post("/checkout", checkoutController.processCheckout);
router.get("/payment-result", checkoutController.handlePaymentResult);
router.post("/orders/:orderCode/cancel", checkoutController.cancelOrder);
router.post("/orders/:orderCode/confirm", checkoutController.confirmOrder);
router.get("/orders/:orderCode/details", checkoutController.getPaymentDetails);

module.exports = router;
