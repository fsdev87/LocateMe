// Notification controller
const { pool } = require("../config/db");

// Helper function to format notification
const formatNotification = (notification) => {
  if (notification.image_url) {
    notification.image_url = `${process.env.SERVER_URL}/${notification.image_url}`;
  }
  if (notification.data) {
    try {
      notification.data = JSON.parse(notification.data);
    } catch (e) {
      notification.data = null;
    }
  }
  return notification;
};

// Get user notifications
exports.getNotifications = async (req, res) => {
  try {
    const userId = req.userId;
    const { limit = 50, offset = 0 } = req.query;

    const [notifications] = await pool.execute(
      `SELECT * FROM notifications 
       WHERE user_id = ? 
       ORDER BY created_at DESC 
       LIMIT ? OFFSET ?`,
      [userId, parseInt(limit), parseInt(offset)]
    );

    const formattedNotifications = notifications.map((n) =>
      formatNotification(n)
    );

    res.status(200).json({
      success: true,
      data: formattedNotifications,
      pagination: {
        limit: parseInt(limit),
        offset: parseInt(offset),
        count: formattedNotifications.length,
      },
    });
  } catch (error) {
    console.error("Get notifications error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching notifications",
      error: error.message,
    });
  }
};

// Get unread notifications count
exports.getUnreadCount = async (req, res) => {
  try {
    const userId = req.userId;

    const [result] = await pool.execute(
      "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = FALSE",
      [userId]
    );

    res.status(200).json({
      success: true,
      data: {
        unreadCount: result[0].count,
      },
    });
  } catch (error) {
    console.error("Get unread count error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching unread count",
      error: error.message,
    });
  }
};

// Mark notification as read
exports.markAsRead = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    // Check if notification belongs to user
    const [notifications] = await pool.execute(
      "SELECT * FROM notifications WHERE id = ? AND user_id = ?",
      [id, userId]
    );

    if (notifications.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Notification not found",
      });
    }

    // Mark as read
    await pool.execute("UPDATE notifications SET is_read = TRUE WHERE id = ?", [
      id,
    ]);

    res.status(200).json({
      success: true,
      message: "Notification marked as read",
    });
  } catch (error) {
    console.error("Mark notification as read error:", error);
    res.status(500).json({
      success: false,
      message: "Error marking notification as read",
      error: error.message,
    });
  }
};

// Mark all notifications as read
exports.markAllAsRead = async (req, res) => {
  try {
    const userId = req.userId;

    await pool.execute(
      "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE",
      [userId]
    );

    res.status(200).json({
      success: true,
      message: "All notifications marked as read",
    });
  } catch (error) {
    console.error("Mark all as read error:", error);
    res.status(500).json({
      success: false,
      message: "Error marking all notifications as read",
      error: error.message,
    });
  }
};

// Delete notification
exports.deleteNotification = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    const [result] = await pool.execute(
      "DELETE FROM notifications WHERE id = ? AND user_id = ?",
      [id, userId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        success: false,
        message: "Notification not found",
      });
    }

    res.status(200).json({
      success: true,
      message: "Notification deleted successfully",
    });
  } catch (error) {
    console.error("Delete notification error:", error);
    res.status(500).json({
      success: false,
      message: "Error deleting notification",
      error: error.message,
    });
  }
};

// Delete all notifications
exports.deleteAllNotifications = async (req, res) => {
  try {
    const userId = req.userId;

    await pool.execute("DELETE FROM notifications WHERE user_id = ?", [userId]);

    res.status(200).json({
      success: true,
      message: "All notifications deleted successfully",
    });
  } catch (error) {
    console.error("Delete all notifications error:", error);
    res.status(500).json({
      success: false,
      message: "Error deleting all notifications",
      error: error.message,
    });
  }
};
