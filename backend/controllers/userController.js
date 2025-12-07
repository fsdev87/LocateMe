// User controller
const { pool } = require("../config/db");
const bcrypt = require("bcryptjs");

// Get user profile
exports.getProfile = async (req, res) => {
  try {
    const userId = req.userId;

    const [users] = await pool.execute(
      `SELECT id, full_name, email, student_id, batch, department, section, 
              profile_pic, created_at, updated_at 
       FROM users WHERE id = ?`,
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    // Get user statistics
    const [stats] = await pool.execute(
      `SELECT 
        COUNT(*) as total_items,
        SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_items
       FROM items 
       WHERE user_id = ? AND deleted_at IS NULL`,
      [userId]
    );

    const totalItems = stats[0].total_items || 0;
    const resolvedItems = stats[0].resolved_items || 0;
    const successRate =
      totalItems > 0 ? ((resolvedItems / totalItems) * 100).toFixed(2) : 0;

    // Add full URL for profile pic if exists
    const user = users[0];
    if (user.profile_pic) {
      user.profile_pic = `${process.env.SERVER_URL}/${user.profile_pic}`;
    }

    // Add statistics to user object
    user.stats = {
      total_items: totalItems,
      resolved_items: resolvedItems,
      success_rate: parseFloat(successRate),
    };

    res.status(200).json({
      success: true,
      data: user,
    });
  } catch (error) {
    console.error("Get profile error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching profile",
      error: error.message,
    });
  }
};

// Update user profile
exports.updateProfile = async (req, res) => {
  try {
    console.log("\n=== UPDATE PROFILE REQUEST ===");
    console.log("[updateProfile] User ID:", req.userId);
    console.log("[updateProfile] Body keys:", Object.keys(req.body));
    console.log("[updateProfile] Has profilePic:", !!req.body.profilePic);
    console.log("[updateProfile] Saved profile pic:", req.savedProfilePic);

    const userId = req.userId;
    const { fullName, studentId, batch, department, section } = req.body;

    // Build dynamic update query
    let updateFields = [];
    let values = [];

    if (fullName) {
      updateFields.push("full_name = ?");
      values.push(fullName);
    }
    if (studentId) {
      updateFields.push("student_id = ?");
      values.push(studentId);
    }
    if (batch) {
      updateFields.push("batch = ?");
      values.push(batch);
    }
    if (department) {
      updateFields.push("department = ?");
      values.push(department);
    }
    if (section) {
      updateFields.push("section = ?");
      values.push(section);
    }

    // Handle profile picture (base64 converted to file path)
    if (req.savedProfilePic) {
      updateFields.push("profile_pic = ?");
      values.push(req.savedProfilePic);
    }

    if (updateFields.length === 0) {
      return res.status(400).json({
        success: false,
        message: "No fields to update",
      });
    }

    values.push(userId);

    await pool.execute(
      `UPDATE users SET ${updateFields.join(", ")} WHERE id = ?`,
      values
    );

    // Get updated user
    const [updatedUsers] = await pool.execute(
      `SELECT id, full_name, email, student_id, batch, department, section, 
              profile_pic, created_at, updated_at 
       FROM users WHERE id = ?`,
      [userId]
    );

    // Get user statistics
    const [stats] = await pool.execute(
      `SELECT 
        COUNT(*) as total_items,
        SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_items
       FROM items 
       WHERE user_id = ? AND deleted_at IS NULL`,
      [userId]
    );

    const totalItems = stats[0].total_items || 0;
    const resolvedItems = stats[0].resolved_items || 0;
    const successRate =
      totalItems > 0 ? ((resolvedItems / totalItems) * 100).toFixed(2) : 0;

    const user = updatedUsers[0];
    if (user.profile_pic) {
      user.profile_pic = `${process.env.SERVER_URL}/${user.profile_pic}`;
    }

    // Add statistics to user object
    user.stats = {
      total_items: totalItems,
      resolved_items: resolvedItems,
      success_rate: parseFloat(successRate),
    };

    console.log("[updateProfile] Profile updated successfully");

    res.status(200).json({
      success: true,
      message: "Profile updated successfully",
      data: user,
    });
  } catch (error) {
    console.error("Update profile error:", error);
    res.status(500).json({
      success: false,
      message: "Error updating profile",
      error: error.message,
    });
  }
};

// Change password
exports.changePassword = async (req, res) => {
  try {
    console.log("\n=== CHANGE PASSWORD REQUEST ===");
    console.log("[changePassword] User ID:", req.userId);

    const userId = req.userId;
    const { currentPassword, newPassword } = req.body;

    if (!currentPassword || !newPassword) {
      return res.status(400).json({
        success: false,
        message: "Current password and new password are required",
      });
    }

    // Get user with password
    const [users] = await pool.execute(
      "SELECT password FROM users WHERE id = ?",
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    // Verify current password
    const isPasswordValid = await bcrypt.compare(
      currentPassword,
      users[0].password
    );

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: "Current password is incorrect",
      });
    }

    // Hash new password
    const hashedPassword = await bcrypt.hash(newPassword, 10);

    // Update password
    await pool.execute("UPDATE users SET password = ? WHERE id = ?", [
      hashedPassword,
      userId,
    ]);

    console.log("[changePassword] Password changed successfully");

    res.status(200).json({
      success: true,
      message: "Password changed successfully",
    });
  } catch (error) {
    console.error("\n=== CHANGE PASSWORD ERROR ===");
    console.error("[changePassword] Error:", error.message);
    res.status(500).json({
      success: false,
      message: "Error changing password",
      error: error.message,
    });
  }
};

// Get user by ID (for viewing other user profiles)
exports.getUserById = async (req, res) => {
  try {
    const { id } = req.params;

    const [users] = await pool.execute(
      `SELECT id, full_name, email, student_id, batch, department, section, 
              profile_pic, created_at 
       FROM users WHERE id = ?`,
      [id]
    );

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    const user = users[0];
    if (user.profile_pic) {
      user.profile_pic = `${process.env.SERVER_URL}/${user.profile_pic}`;
    }

    res.status(200).json({
      success: true,
      data: user,
    });
  } catch (error) {
    console.error("Get user error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching user",
      error: error.message,
    });
  }
};

// Delete account (soft delete)
exports.deleteAccount = async (req, res) => {
  try {
    console.log("\n=== DELETE ACCOUNT REQUEST ===");
    console.log("[deleteAccount] User ID:", req.userId);

    const userId = req.userId;
    const { password } = req.body;

    // Verify password before deletion
    if (!password) {
      return res.status(400).json({
        success: false,
        message: "Password is required to delete account",
      });
    }

    // Get user with password
    const [users] = await pool.execute(
      "SELECT password FROM users WHERE id = ?",
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, users[0].password);

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: "Incorrect password",
      });
    }

    // Soft delete user account
    await pool.execute(
      "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?",
      [userId]
    );

    // Also soft delete all user's items
    await pool.execute(
      "UPDATE items SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?",
      [userId]
    );

    console.log("[deleteAccount] Account deleted successfully");

    res.status(200).json({
      success: true,
      message: "Account deleted successfully",
    });
  } catch (error) {
    console.error("\n=== DELETE ACCOUNT ERROR ===");
    console.error("[deleteAccount] Error:", error.message);
    res.status(500).json({
      success: false,
      message: "Error deleting account",
      error: error.message,
    });
  }
};
