/**
 * Tệp này tập hợp tất cả các routes
 */
const express = require('express');
const router = express.Router();

// Import các routes
const paymentRoutes = require('./paymentRoutes');
const userRoutes = require('./userRoutes');
const geocodingRoutes = require('./geocodingRoutes');

// Áp dụng routes
router.use('/payments', paymentRoutes);
router.use('/users', userRoutes);
router.use('/geocoding', geocodingRoutes);

module.exports = router;
