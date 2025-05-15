const express = require("express");
const router = express.Router();
const geocodingController = require("../controllers/geocodingController");

// Routes tìm kiếm địa chỉ
router.post("/search", geocodingController.searchAddress);
router.post("/format-search", geocodingController.formatSearchAddress);
router.get("/reverse", geocodingController.reverseGeocode);

module.exports = router;
