/**
 * Tệp này tập hợp tất cả các routes
 */
const express = require('express');
const router = express.Router();

// Import các routes
const orderRoutes = require('./orderRoutes');

// Áp dụng routes
router.use('/orders', orderRoutes);

module.exports = router;
