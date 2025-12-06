require('dotenv').config();
const mysql = require('mysql2/promise');

async function testConnection() {
  let connection;
  
  try {
    console.log('🔄 Testing database connection...');
    console.log('📍 Host:', process.env.DB_HOST);
    console.log('📍 Database:', process.env.DB_NAME);
    console.log('📍 User:', process.env.DB_USER);
    
    // Create connection
    connection = await mysql.createConnection({
      host: process.env.DB_HOST,
      port: process.env.DB_PORT || 3306,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME
    });
    
    console.log('✅ Database connected successfully!\n');
    
    // Show all tables
    const [tables] = await connection.execute('SHOW TABLES');
    
    if (tables.length > 0) {
      console.log('📋 Tables in database:');
      tables.forEach(table => {
        console.log(`   - ${Object.values(table)[0]}`);
      });
      
      // Count records in each table
      console.log('\n📊 Record counts:');
      for (const table of tables) {
        const tableName = Object.values(table)[0];
        const [count] = await connection.execute(`SELECT COUNT(*) as count FROM ${tableName}`);
        console.log(`   - ${tableName}: ${count[0].count} records`);
      }
    } else {
      console.log('⚠️  No tables found. Run "npm run setup-db" to create tables.');
    }
    
    console.log('\n✅ Connection test completed successfully!');
    
  } catch (error) {
    console.error('\n❌ Database connection failed!');
    console.error('Error:', error.message);
    console.error('\n🔍 Troubleshooting:');
    console.error('   1. Check if .env file exists with correct credentials');
    console.error('   2. Verify Railway MySQL service is running');
    console.error('   3. Check if DB_HOST, DB_USER, DB_PASSWORD are correct');
    console.error('   4. Ensure your IP is whitelisted (Railway usually allows all)');
    process.exit(1);
  } finally {
    if (connection) {
      await connection.end();
      console.log('🔌 Connection closed\n');
    }
  }
}

testConnection();