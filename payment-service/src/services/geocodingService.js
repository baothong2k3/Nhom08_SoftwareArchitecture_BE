const axios = require('axios');
const querystring = require('querystring');

// Lấy API key từ biến môi trường
const API_KEY = process.env.YOUR_API_KEY_MAP;
const ORS_BASE_URL = 'https://api.openrouteservice.org/geocode';

/**
 * Tìm kiếm địa điểm dựa trên text
 * @param {String} searchText - Địa chỉ cần tìm
 * @param {Object} options - Các tùy chọn tìm kiếm bổ sung
 * @returns {Promise<Object>} - Kết quả tìm kiếm
 */
exports.searchAddress = async (searchText, options = {}) => {
  try {
    if (!searchText) {
      throw new Error('Thiếu từ khóa tìm kiếm');
    }
    
    // Chuẩn bị query parameters
    const params = {
      api_key: API_KEY,
      text: searchText,
      ...options
    };

    // Log cho việc debug
    console.log(`Đang tìm kiếm địa chỉ với text: ${searchText}`);
    
    // Gọi API OpenRouteService
    const response = await axios.get(`${ORS_BASE_URL}/search?${querystring.stringify(params)}`);
    
    return {
      success: true,
      data: response.data,
      message: 'Tìm kiếm địa chỉ thành công'
    };
  } catch (error) {
    console.error('Lỗi khi tìm kiếm địa chỉ:', error);
    
    return {
      success: false,
      message: 'Lỗi khi tìm kiếm địa chỉ',
      error: error.message || 'Lỗi không xác định',
      status: error.response?.status || 500
    };
  }
};

/**
 * Định dạng kết quả tìm kiếm để dễ sử dụng
 * @param {String} searchText - Địa chỉ cần tìm
 * @returns {Promise<Object>} - Kết quả tìm kiếm đã được định dạng
 */
exports.formatSearchResults = async (searchText) => {
  try {
    // Gọi API tìm kiếm
    const searchResult = await exports.searchAddress(searchText);
    
    if (!searchResult.success) {
      return searchResult; // Trả về lỗi nếu có
    }
    
    const { data } = searchResult;
    
    // Định dạng kết quả để dễ sử dụng
    const formattedResults = data.features.map(feature => {
      const { properties, geometry } = feature;
      
      return {
        id: feature.id || properties.id,
        name: properties.name,
        label: properties.label,
        coordinates: {
          lat: geometry.coordinates[1],
          lng: geometry.coordinates[0]
        },
        country: properties.country,
        region: properties.region,
        county: properties.county,
        locality: properties.locality,
        confidence: properties.confidence
      };
    });
    
    return {
      success: true,
      results: formattedResults,
      query: searchText,
      count: formattedResults.length
    };
  } catch (error) {
    console.error('Lỗi khi định dạng kết quả tìm kiếm:', error);
    
    return {
      success: false,
      message: 'Lỗi khi định dạng kết quả tìm kiếm',
      error: error.message || 'Lỗi không xác định'
    };
  }
};

/**
 * Tìm địa chỉ từ tọa độ (reverse geocoding)
 * @param {Number} lat - Vĩ độ
 * @param {Number} lng - Kinh độ
 * @returns {Promise<Object>} - Kết quả tìm kiếm
 */
exports.reverseGeocode = async (lat, lng) => {
  try {
    if (!lat || !lng) {
      throw new Error('Thiếu tọa độ (lat, lng)');
    }
    
    // Chuẩn bị query parameters
    const params = {
      api_key: API_KEY,
      'point.lat': lat,
      'point.lon': lng
    };
    
    // Gọi API OpenRouteService
    const response = await axios.get(`${ORS_BASE_URL}/reverse?${querystring.stringify(params)}`);
    
    // Định dạng kết quả
    const formattedResults = response.data.features.map(feature => {
      const { properties } = feature;
      
      return {
        name: properties.name,
        label: properties.label,
        country: properties.country,
        region: properties.region,
        county: properties.county,
        locality: properties.locality,
        street: properties.street,
        postalCode: properties.postalcode,
        confidence: properties.confidence
      };
    });
    
    return {
      success: true,
      results: formattedResults,
      coordinates: { lat, lng },
      count: formattedResults.length
    };
  } catch (error) {
    console.error('Lỗi khi reverse geocoding:', error);
    
    return {
      success: false,
      message: 'Lỗi khi reverse geocoding',
      error: error.message || 'Lỗi không xác định',
      status: error.response?.status || 500
    };
  }
};
