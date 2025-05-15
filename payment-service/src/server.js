require("dotenv").config();
const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const { sequelize, testConnection } = require("./config/database");
const routes = require("./routes");

// Khởi tạo Express app
const app = express();
const PORT = process.env.PORT || 8989;

// Cấu hình CORS để cho phép tất cả các nguồn
const corsOptions = {
  origin: "*", // Cho phép tất cả các origin
  methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
  allowedHeaders: [
    "Content-Type",
    "Authorization",
    "Accept",
    "Origin",
    "X-Requested-With",
  ],
  credentials: true,
  optionsSuccessStatus: 200,
};

// Middleware
app.use(cors(corsOptions));
app.use(express.json());
app.use(morgan("dev")); // Logging để debug

// Middleware thêm headers CORS cho mọi response
app.use((req, res, next) => {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader(
    "Access-Control-Allow-Methods",
    "GET, POST, PUT, DELETE, OPTIONS"
  );
  res.setHeader(
    "Access-Control-Allow-Headers",
    "Content-Type, Authorization, Accept, Origin, X-Requested-With"
  );
  res.setHeader("Access-Control-Allow-Credentials", "true");

  // Xử lý OPTIONS request
  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  next();
});

// Đăng ký routes API
app.use("/api", routes);

// Health check endpoint cho service registry
app.get("/health", (req, res) => {
  res.status(200).json({ status: "UP" });
});

// Root endpoint
app.get("/", (req, res) => {
  res.status(200).json({
    message: "Payment Service API",
    version: "1.0.0",
    endpoints: {
      payments: "/api/payments",
    },
  });
});

// Middleware xử lý lỗi
app.use((err, req, res, next) => {
  console.error("Lỗi server:", err);
  res.status(err.status || 500).json({
    success: false,
    status: err.status || 500,
    message: err.message || "Lỗi máy chủ nội bộ",
    stack: process.env.NODE_ENV === "development" ? err.stack : undefined,
  });
});

// Bắt lỗi route không tồn tại
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: "API endpoint không tồn tại",
  });
});

// Khởi động server sau khi kết nối database
(async () => {
  try {
    console.log("Đang khởi tạo kết nối database...");
    await testConnection();

    app.listen(PORT, () => {
      console.log(`Payment service đang chạy trên cổng ${PORT}`);
      console.log(`Kiểm tra API tại http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error("Không thể khởi động server do lỗi database:", error);
    process.exit(1);
  }
})();
