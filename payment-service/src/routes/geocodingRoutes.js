const express = require('express');
const router = express.Router();
const geocodingController = require('../controllers/geocodingController');

/**
 * Tìm kiếm địa điểm theo text
 * POST /api/geocoding/search
 * Body: { "text": "địa chỉ cần tìm" }
 */
router.post('/search', geocodingController.searchAddress);

/**
 * Tìm kiếm và định dạng kết quả địa chỉ
 * POST /api/geocoding/format-search
 * Body: { "text": "địa chỉ cần tìm" }
 */
router.post('/format-search', geocodingController.formatSearchAddress);

/**
 * Tìm địa chỉ từ tọa độ
 * GET /api/geocoding/reverse?lat=<latitude>&lng=<longitude>
 */
router.get('/reverse', geocodingController.reverseGeocode);

module.exports = router;
