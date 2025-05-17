const { Sequelize } = require('sequelize');
const mariadb = require('mariadb');

// Tên database
const DATABASE_NAME = process.env.DB_NAME || 'order-service-bookstore';
const DB_USER = process.env.DB_USER || 'root';
const DB_PASS = process.env.DB_PASS || 'root';
const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_PORT = process.env.DB_PORT || 3306;

// Tạo database nếu chưa tồn tại
async function createDatabaseIfNotExists() {
  // Kết nối tới MariaDB mà không chỉ định database
  const pool = mariadb.createPool({
    host: DB_HOST,
    port: DB_PORT,
    user: DB_USER,
    password: DB_PASS,
    connectionLimit: 5
  });

  let conn;
  try {
    conn = await pool.getConnection();
    console.log("Đang kiểm tra database...");

    // Tạo database nếu chưa tồn tại
    await conn.query(`CREATE DATABASE IF NOT EXISTS \`${DATABASE_NAME}\`;`);
    console.log(`Database '${DATABASE_NAME}' đã được tạo hoặc đã tồn tại.`);
    return true;
  } catch (err) {
    console.error('Lỗi khi tạo database:', err);
    return false;
  } finally {
    if (conn) await conn.release(); // Đóng kết nối
    await pool.end(); // Đóng pool
  }
}

// Tạo kết nối đến database
const sequelize = new Sequelize(
  DATABASE_NAME,
  DB_USER,
  DB_PASS,
  {
    host: DB_HOST,
    port: DB_PORT,
    dialect: 'mariadb',
    dialectOptions: {
      timezone: 'Asia/Ho_Chi_Minh',
    },
    pool: {
      max: 5,
      min: 0,
      acquire: 30000,
      idle: 10000
    },
    logging: process.env.NODE_ENV === 'development' ? console.log : false
  }
);

// Hàm kiểm tra kết nối
const testConnection = async () => {
  try {
    // Đảm bảo database tồn tại trước khi kiểm tra kết nối
    const dbCreated = await createDatabaseIfNotExists();
    if (!dbCreated) {
      throw new Error('Không thể tạo database');
    }

    // Thử kết nối đến database
    await sequelize.authenticate();
    console.log('Kết nối đến database thành công.');
    return true;
  } catch (error) {
    console.error('Không thể kết nối đến database:', error);
    throw error;
  }
};

module.exports = {
  sequelize,
  testConnection
};
