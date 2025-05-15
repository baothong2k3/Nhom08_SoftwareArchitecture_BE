const express = require("express");
const router = express.Router();

// Import các router
const paymentRoutes = require("./paymentRoutes");
const geocodingRoutes = require("./geocodingRoutes");

// Sử dụng các router với tiền tố đường dẫn
router.use("/payments", paymentRoutes);
router.use("/geocoding", geocodingRoutes);

// Route mặc định
router.get("/", (req, res) => {
  res.status(200).json({
    success: true,
    message: "Payment Service API",
    version: "1.0.0",
    endpoints: {
      payments: "/api/payments",
      geocoding: "/api/geocoding",
    },
  });
});

// Route health check
router.get("/health", (req, res) => {
  res.status(200).json({
    success: true,
    service: "payment-service",
    status: "up",
    timestamp: new Date().toISOString(),
  });
});

module.exports = router;
