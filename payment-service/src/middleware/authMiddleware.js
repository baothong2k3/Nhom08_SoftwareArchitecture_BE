const jwt = require('jsonwebtoken');

/**
 * Debug token mà không verify signature
 */
const debugToken = (token) => {
  try {
    // Parse token mà không cần verify signature
    return jwt.decode(token, { complete: true });
  } catch (error) {
    console.error('Lỗi parsing token:', error.message);
    return null;
  }
};

/**
 * Giải mã JWT token và trả về thông tin
 */
const decodeToken = (token) => {
  if (!token) {
    return {
      success: false,
      message: 'Token không được cung cấp'
    };
  }

  try {
    // Sử dụng phương pháp decode để lấy thông tin mà không cần verify signature
    const decoded = jwt.decode(token);
    
    if (!decoded) {
      return {
        success: false,
        message: 'Token không hợp lệ hoặc không đúng định dạng JWT'
      };
    }
    
    return {
      success: true,
      user: {
        username: decoded.sub,
        userId: decoded.userId,
        role: decoded.role,
        iat: decoded.iat ? new Date(decoded.iat * 1000).toISOString() : undefined,
        exp: decoded.exp ? new Date(decoded.exp * 1000).toISOString() : undefined
      }
    };
  } catch (error) {
    console.error("Lỗi giải mã token:", error.message);
    return {
      success: false,
      message: 'Lỗi khi giải mã token',
      error: error.message
    };
  }
};

module.exports = {
  decodeToken,
  debugToken
};
