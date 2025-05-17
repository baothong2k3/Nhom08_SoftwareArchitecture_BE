const express = require('express');
const router = express.Router();
const { decodeToken } = require('../middleware/authMiddleware');

/**
 * Endpoint duy nhất: Lấy userId trực tiếp từ token được truyền qua query param
 */
router.get('/get-user-id-from-token', (req, res) => {
  const token = req.query.token;
  
  if (!token) {
    return res.status(400).json({
      success: false,
      message: 'Token không được cung cấp'
    });
  }
  
  try {
    const result = decodeToken(token);
    
    if (!result.success) {
      return res.status(400).json(result);
    }
    
    const userId = result.user.userId;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'Token không chứa userId'
      });
    }
    
    return res.status(200).json({
      success: true,
      userId: userId
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi trích xuất userId từ token',
      error: error.message
    });
  }
});

/**
 * Endpoint 2: Lấy thông tin về role từ token
 */
router.get('/role', (req, res) => {
  const token = req.query.token;
  
  if (!token) {
    return res.status(400).json({
      success: false,
      message: 'Token không được cung cấp'
    });
  }
  
  try {
    const result = decodeToken(token);
    
    if (!result.success) {
      return res.status(400).json(result);
    }
    
    const role = result.user.role;
    
    if (!role) {
      return res.status(400).json({
        success: false,
        message: 'Token không chứa thông tin về role'
      });
    }
    
    // Xử lý các định dạng role khác nhau
    let roles = [];
    
    if (Array.isArray(role)) {
      // Nếu role là một mảng các đối tượng
      roles = role.map(r => r.authority || r);
    } else if (typeof role === 'string') {
      // Nếu role là chuỗi
      roles = [role];
    } else if (typeof role === 'object') {
      // Nếu role là một đối tượng
      roles = [role.authority || JSON.stringify(role)];
    }
    
    return res.status(200).json({
      success: true,
      roles: roles
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi trích xuất thông tin role từ token',
      error: error.message
    });
  }
});

module.exports = router;
