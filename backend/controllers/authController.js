// Authentication controller
const bcrypt = require("bcryptjs");
const { pool } = require("../config/db");
const { generateToken } = require("../utils/jwt");

// Signup
exports.signup = async (req, res) => {
  try {
    const { fullName, email, password, studentId, batch, department, section } =
      req.body;

    // Validation
    if (
      !fullName ||
      !email ||
      !password ||
      !studentId ||
      !batch ||
      !department ||
      !section
    ) {
      return res.status(400).json({
        success: false,
        message: "All fields are required",
      });
    }

    // Check if user already exists
    const [existingUsers] = await pool.execute(
      "SELECT id FROM users WHERE email = ?",
      [email]
    );

    if (existingUsers.length > 0) {
      return res.status(400).json({
        success: false,
        message: "User with this email already exists",
      });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Insert user
    const [result] = await pool.execute(
      `INSERT INTO users (full_name, email, password, student_id, batch, department, section) 
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [fullName, email, hashedPassword, studentId, batch, department, section]
    );

    // Generate token
    const token = generateToken(result.insertId);

    // Get created user
    const [newUser] = await pool.execute(
      "SELECT id, full_name, email, student_id, batch, department, section, profile_pic, created_at FROM users WHERE id = ?",
      [result.insertId]
    );

    res.status(201).json({
      success: true,
      message: "User registered successfully",
      data: {
        token,
        user: newUser[0],
      },
    });
  } catch (error) {
    console.error("Signup error:", error);
    res.status(500).json({
      success: false,
      message: "Error creating user",
      error: error.message,
    });
  }
};

// Login
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validation
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: "Email and password are required",
      });
    }

    // Find user
    const [users] = await pool.execute("SELECT * FROM users WHERE email = ?", [
      email,
    ]);

    if (users.length === 0) {
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    const user = users[0];

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, user.password);

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    // Generate token
    const token = generateToken(user.id);

    // Remove password from response
    delete user.password;

    res.status(200).json({
      success: true,
      message: "Login successful",
      data: {
        token,
        user,
      },
    });
  } catch (error) {
    console.error("Login error:", error);
    res.status(500).json({
      success: false,
      message: "Error logging in",
      error: error.message,
    });
  }
};

// Update FCM Token
exports.updateFcmToken = async (req, res) => {
  try {
    const { fcmToken } = req.body;
    const userId = req.userId;

    if (!fcmToken) {
      return res.status(400).json({
        success: false,
        message: "FCM token is required",
      });
    }

    await pool.execute("UPDATE users SET fcm_token = ? WHERE id = ?", [
      fcmToken,
      userId,
    ]);

    res.status(200).json({
      success: true,
      message: "FCM token updated successfully",
    });
  } catch (error) {
    console.error("Update FCM token error:", error);
    res.status(500).json({
      success: false,
      message: "Error updating FCM token",
      error: error.message,
    });
  }
};
