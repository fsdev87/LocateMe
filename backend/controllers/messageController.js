// Message controller
const { pool } = require("../config/db");
const { sendPushNotification } = require("../utils/fcm");

// Helper function to format message with full URL
const formatMessage = (message) => {
  if (message.media_url) {
    message.media_url = `${process.env.SERVER_URL}/${message.media_url}`;
  }
  if (message.sender_profile_pic) {
    message.sender_profile_pic = `${process.env.SERVER_URL}/${message.sender_profile_pic}`;
  }
  return message;
};

// Send message
exports.sendMessage = async (req, res) => {
  try {
    console.log("\n=== SEND MESSAGE REQUEST ===");
    console.log("[sendMessage] User ID:", req.userId);
    console.log("[sendMessage] Chat ID:", req.body.chatId);
    console.log("[sendMessage] Has messageImage:", !!req.body.messageImage);
    console.log("[sendMessage] Saved message image:", req.savedMessageImage);

    const userId = req.userId;
    const { chatId, content, type } = req.body;

    // Validation
    if (!chatId || !type) {
      return res.status(400).json({
        success: false,
        message: "Chat ID and type are required",
      });
    }

    const validTypes = ["TEXT", "IMAGE"];
    if (!validTypes.includes(type)) {
      return res.status(400).json({
        success: false,
        message: "Invalid message type",
      });
    }

    // Check if chat exists and user is part of it
    const [chats] = await pool.execute(
      "SELECT * FROM chats WHERE id = ? AND (user1_id = ? OR user2_id = ?)",
      [chatId, userId, userId]
    );

    if (chats.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Chat not found or you do not have access to it",
      });
    }

    const chat = chats[0];
    const receiverId = chat.user1_id === userId ? chat.user2_id : chat.user1_id;

    // Handle message based on type
    let messageContent = null;
    let mediaUrl = null;

    if (type === "TEXT") {
      if (!content) {
        return res.status(400).json({
          success: false,
          message: "Content is required for text messages",
        });
      }
      messageContent = content;
    } else if (type === "IMAGE") {
      if (!req.savedMessageImage) {
        return res.status(400).json({
          success: false,
          message: "Image is required for image messages",
        });
      }
      mediaUrl = req.savedMessageImage;
    }

    // Insert message
    const [result] = await pool.execute(
      `INSERT INTO messages (chat_id, sender_id, receiver_id, type, content, media_url) 
       VALUES (?, ?, ?, ?, ?, ?)`,
      [chatId, userId, receiverId, type, messageContent, mediaUrl]
    );

    // Update chat's last_message_at
    await pool.execute(
      "UPDATE chats SET last_message_at = CURRENT_TIMESTAMP WHERE id = ?",
      [chatId]
    );

    // Get sender info for notification
    const [senders] = await pool.execute(
      "SELECT full_name, profile_pic FROM users WHERE id = ?",
      [userId]
    );

    const sender = senders[0];
    const senderProfilePic = sender.profile_pic
      ? `${process.env.SERVER_URL}/${sender.profile_pic}`
      : null;

    // Get receiver's FCM token
    const [receivers] = await pool.execute(
      "SELECT fcm_token FROM users WHERE id = ?",
      [receiverId]
    );

    // Send push notification if receiver has FCM token
    if (receivers.length > 0 && receivers[0].fcm_token) {
      const notificationBody = type === "TEXT" ? content : "Sent an image";

      await sendPushNotification(
        receivers[0].fcm_token,
        sender.full_name,
        notificationBody,
        senderProfilePic,
        {
          chatId: chatId.toString(),
          senderId: userId.toString(),
          messageId: result.insertId.toString(),
        }
      );

      // Store notification in database
      await pool.execute(
        `INSERT INTO notifications (user_id, title, body, image_url, data) 
         VALUES (?, ?, ?, ?, ?)`,
        [
          receiverId,
          sender.full_name,
          notificationBody,
          senderProfilePic,
          JSON.stringify({
            chatId,
            senderId: userId,
            messageId: result.insertId,
          }),
        ]
      );
    }

    // Get created message
    const [messages] = await pool.execute(
      `SELECT m.*, u.full_name as sender_name, u.profile_pic as sender_profile_pic
       FROM messages m
       LEFT JOIN users u ON m.sender_id = u.id
       WHERE m.id = ?`,
      [result.insertId]
    );

    const message = formatMessage(messages[0]);

    res.status(201).json({
      success: true,
      message: "Message sent successfully",
      data: message,
    });
  } catch (error) {
    console.error("Send message error:", error);
    res.status(500).json({
      success: false,
      message: "Error sending message",
      error: error.message,
    });
  }
};

// Get messages for a chat
exports.getChatMessages = async (req, res) => {
  try {
    const userId = req.userId;
    const { chatId } = req.params;
    const { limit = 50, offset = 0 } = req.query;

    // Check if user has access to this chat
    const [chats] = await pool.execute(
      "SELECT * FROM chats WHERE id = ? AND (user1_id = ? OR user2_id = ?)",
      [chatId, userId, userId]
    );

    if (chats.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Chat not found or you do not have access to it",
      });
    }

    // Get messages
    const limitValue = parseInt(limit) || 50;
    const offsetValue = parseInt(offset) || 0;

    const [messages] = await pool.execute(
      `SELECT m.*, u.full_name as sender_name, u.profile_pic as sender_profile_pic
       FROM messages m
       LEFT JOIN users u ON m.sender_id = u.id
       WHERE m.chat_id = ?
       ORDER BY m.created_at DESC
       LIMIT ${limitValue} OFFSET ${offsetValue}`,
      [chatId]
    );

    const formattedMessages = messages.map((msg) => formatMessage(msg));

    // Mark messages as read
    await pool.execute(
      "UPDATE messages SET is_read = TRUE WHERE chat_id = ? AND receiver_id = ? AND is_read = FALSE",
      [chatId, userId]
    );

    res.status(200).json({
      success: true,
      data: formattedMessages.reverse(), // Return in chronological order
      pagination: {
        limit: parseInt(limit),
        offset: parseInt(offset),
        count: formattedMessages.length,
      },
    });
  } catch (error) {
    console.error("Get chat messages error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching messages",
      error: error.message,
    });
  }
};

// Mark messages as read
exports.markMessagesAsRead = async (req, res) => {
  try {
    const userId = req.userId;
    const { chatId } = req.params;

    await pool.execute(
      "UPDATE messages SET is_read = TRUE WHERE chat_id = ? AND receiver_id = ? AND is_read = FALSE",
      [chatId, userId]
    );

    res.status(200).json({
      success: true,
      message: "Messages marked as read",
    });
  } catch (error) {
    console.error("Mark messages as read error:", error);
    res.status(500).json({
      success: false,
      message: "Error marking messages as read",
      error: error.message,
    });
  }
};

// Delete message
exports.deleteMessage = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    // Check if message belongs to user
    const [messages] = await pool.execute(
      "SELECT * FROM messages WHERE id = ? AND sender_id = ?",
      [id, userId]
    );

    if (messages.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Message not found or you do not have permission to delete it",
      });
    }

    // Check if message is within 5 minutes of creation
    const message = messages[0];
    const messageTime = new Date(message.created_at);
    const currentTime = new Date();
    const timeDiff = (currentTime - messageTime) / 1000 / 60; // difference in minutes

    if (timeDiff > 5) {
      return res.status(403).json({
        success: false,
        message: "Messages can only be deleted within 5 minutes of sending",
      });
    }

    // Delete message
    await pool.execute("DELETE FROM messages WHERE id = ?", [id]);

    res.status(200).json({
      success: true,
      message: "Message deleted successfully",
    });
  } catch (error) {
    console.error("Delete message error:", error);
    res.status(500).json({
      success: false,
      message: "Error deleting message",
      error: error.message,
    });
  }
};

// Get unread message count
exports.getUnreadCount = async (req, res) => {
  try {
    const userId = req.userId;

    const [result] = await pool.execute(
      "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = FALSE",
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
