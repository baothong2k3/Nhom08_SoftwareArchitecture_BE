const geocodingService = require('../services/geocodingService');

/**
 * Tìm kiếm địa chỉ dựa trên text
 */
exports.searchAddress = async (req, res) => {
  try {
    const { text } = req.body;
    
    if (!text) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu từ khóa tìm kiếm'
      });
    }
    
    // Lấy các tùy chọn từ query params (nếu có)
    const options = {};
    if (req.query.size) options.size = req.query.size;
    if (req.query.layers) options.layers = req.query.layers;
    if (req.query.boundary_country) options.boundary_country = req.query.boundary_country;
    
    // Gọi service để tìm kiếm
    const result = await geocodingService.searchAddress(text, options);
    
    if (!result.success) {
      return res.status(result.status || 500).json(result);
    }
    
    return res.status(200).json(result);
  } catch (error) {
    console.error('Lỗi controller searchAddress:', error);
    
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi tìm kiếm địa chỉ',
      error: error.message || 'Lỗi không xác định'
    });
  }
};

/**
 * Tìm kiếm và định dạng kết quả cho dễ sử dụng
 */
exports.formatSearchAddress = async (req, res) => {
  try {
    const { text } = req.body;
    
    if (!text) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu từ khóa tìm kiếm'
      });
    }
    
    // Gọi service để tìm kiếm và định dạng
    const result = await geocodingService.formatSearchResults(text);
    
    if (!result.success) {
      return res.status(500).json(result);
    }
    
    return res.status(200).json(result);
  } catch (error) {
    console.error('Lỗi controller formatSearchAddress:', error);
    
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi tìm kiếm và định dạng địa chỉ',
      error: error.message || 'Lỗi không xác định'
    });
  }
};

/**
 * Tìm địa chỉ từ tọa độ
 */
exports.reverseGeocode = async (req, res) => {
  try {
    const { lat, lng } = req.query;
    
    if (!lat || !lng) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu tọa độ (lat, lng)'
      });
    }
    
    // Gọi service để reverse geocoding
    const result = await geocodingService.reverseGeocode(lat, lng);
    
    if (!result.success) {
      return res.status(result.status || 500).json(result);
    }
    
    return res.status(200).json(result);
  } catch (error) {
    console.error('Lỗi controller reverseGeocode:', error);
    
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi tìm địa chỉ từ tọa độ',
      error: error.message || 'Lỗi không xác định'
    });
  }
};
