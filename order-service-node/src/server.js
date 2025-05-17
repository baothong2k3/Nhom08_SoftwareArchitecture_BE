require('dotenv').config();
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const { sequelize, testConnection } = require('./config/database');
const routes = require('./routes');
const { decodeToken } = require('./middleware/authMiddleware');

// Khởi tạo Express app
const app = express();
const PORT = process.env.PORT || 8005;

// Cấu hình CORS để cho phép tất cả các nguồn
const corsOptions = {
  origin: '*', // Cho phép tất cả các origin
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'Accept', 'Origin', 'X-Requested-With'],
  credentials: true,
  optionsSuccessStatus: 200
};

// Middleware
app.use(cors(corsOptions));
app.use(express.json());
app.use(morgan('dev')); // Logging để debug

// Middleware thêm headers CORS cho mọi response
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept, Origin, X-Requested-With');
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  
  // Xử lý OPTIONS request
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }
  
  next();
});

// Áp dụng tất cả routes
app.use('/api', routes);

// Route kiểm tra token trực tiếp cho việc debug
app.get('/test-token', (req, res) => {
  const authHeader = req.headers['authorization'];
  
  if (!authHeader) {
    return res.status(400).json({
      success: false,
      message: 'Không tìm thấy Authorization header'
    });
  }
  
  console.log('Authorization Header:', authHeader);
  
  if (!authHeader.startsWith('Bearer ')) {
    return res.status(400).json({
      success: false,
      message: 'Định dạng token không đúng, cần bắt đầu bằng "Bearer "'
    });
  }
  
  const token = authHeader.substring(7);
  
  if (!token) {
    return res.status(400).json({
      success: false,
      message: 'Token rỗng'
    });
  }
  
  console.log('Token extracted:', token);
  
  try {
    // Thử giải mã token mà không xử lý lỗi
    const result = decodeToken(token);
    return res.status(200).json({
      success: true,
      token: token,
      decodedResult: result
    });
  } catch (error) {
    console.error('Error decoding token:', error);
    return res.status(500).json({
      success: false,
      message: 'Lỗi khi giải mã token',
      error: error.message
    });
  }
});

// Health check endpoint cho service registry
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'UP' });
});

// Kết nối đến database và đồng bộ models
const initializeDatabase = async () => {
  try {
    console.log('Đang kết nối đến database...');
    await testConnection();
    
    console.log('Đang đồng bộ các models...');
    // Đồng bộ các models với database
    await sequelize.sync({ alter: true }); // Sử dụng alter: true để tự động cập nhật schema
    console.log('Database đã được đồng bộ thành công');
    return true;
  } catch (error) {
    console.error('Lỗi khi khởi tạo database:', error);
    if (error.original && error.original.code === 'ER_BAD_DB_ERROR') {
      console.error('Database không tồn tại và không thể tự động tạo. Vui lòng kiểm tra quyền của user MariaDB.');
    }
    return false;
  }
};

// Root endpoint
app.get('/', (req, res) => {
  res.status(200).json({
    message: 'Order Service API',
    version: '1.0.0',
    endpoints: {
      orders: '/api/orders'
    }
  });
});

// Middleware xử lý lỗi
app.use((err, req, res, next) => {
  console.error('Lỗi server:', err);
  res.status(err.status || 500).json({
    success: false,
    status: err.status || 500,
    message: err.message || 'Lỗi máy chủ nội bộ',
    stack: process.env.NODE_ENV === 'development' ? err.stack : undefined
  });
});

// Bắt lỗi route không tồn tại
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: 'API endpoint không tồn tại'
  });
});

// Khởi động server sau khi kết nối database
(async () => {
  const dbInitialized = await initializeDatabase();
  
  if (!dbInitialized) {
    console.error('Không thể khởi động server do lỗi database. Vui lòng kiểm tra cấu hình và thử lại.');
    process.exit(1);
  }
  
  app.listen(PORT, () => {
    console.log(`Order service đang chạy trên cổng ${PORT}`);
    console.log(`Kiểm tra API tại http://localhost:${PORT}`);
  });
})();
