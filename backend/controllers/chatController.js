// Chat controller
const { pool } = require("../config/db");

// Helper function to format chat with user info
const formatChat = (chat) => {
  if (chat.user_profile_pic) {
    chat.user_profile_pic = `${process.env.SERVER_URL}/${chat.user_profile_pic}`;
  }
  return chat;
};

// Create or get existing chat
exports.createOrGetChat = async (req, res) => {
  try {
    const userId = req.userId;
    const { otherUserId } = req.body;

    if (!otherUserId) {
      return res.status(400).json({
        success: false,
        message: "Other user ID is required",
      });
    }

    if (userId === parseInt(otherUserId)) {
      return res.status(400).json({
        success: false,
        message: "Cannot create chat with yourself",
      });
    }

    // Check if chat already exists (in either direction)
    const [existingChats] = await pool.execute(
      `SELECT * FROM chats 
       WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)`,
      [userId, otherUserId, otherUserId, userId]
    );

    let chatId;

    if (existingChats.length > 0) {
      // Chat exists
      chatId = existingChats[0].id;
    } else {
      // Create new chat
      const [result] = await pool.execute(
        "INSERT INTO chats (user1_id, user2_id) VALUES (?, ?)",
        [userId, otherUserId]
      );
      chatId = result.insertId;
    }

    // Get chat with other user's info
    const [chats] = await pool.execute(
      `SELECT c.*, 
              u.id as other_user_id, u.full_name as other_user_name, 
              u.email as other_user_email, u.profile_pic as user_profile_pic
       FROM chats c
       JOIN users u ON (
         CASE 
           WHEN c.user1_id = ? THEN c.user2_id 
           ELSE c.user1_id 
         END = u.id
       )
       WHERE c.id = ?`,
      [userId, chatId]
    );

    const chat = formatChat(chats[0]);

    res.status(200).json({
      success: true,
      data: chat,
    });
  } catch (error) {
    console.error("Create/get chat error:", error);
    res.status(500).json({
      success: false,
      message: "Error creating/getting chat",
      error: error.message,
    });
  }
};

// Create or get chat from item (convenience method)
exports.createChatFromItem = async (req, res) => {
  try {
    const userId = req.userId;
    const { itemId } = req.params;

    // Get item owner
    const [items] = await pool.execute(
      "SELECT user_id FROM items WHERE id = ? AND deleted_at IS NULL",
      [itemId]
    );

    if (items.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Item not found",
      });
    }

    const itemOwnerId = items[0].user_id;

    // Check if user is trying to chat with themselves
    if (userId === itemOwnerId) {
      return res.status(400).json({
        success: false,
        message: "You cannot chat with yourself",
      });
    }

    // Check if chat already exists (in either direction)
    const [existingChats] = await pool.execute(
      `SELECT * FROM chats 
       WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)`,
      [userId, itemOwnerId, itemOwnerId, userId]
    );

    let chatId;

    if (existingChats.length > 0) {
      // Chat exists
      chatId = existingChats[0].id;
    } else {
      // Create new chat
      const [result] = await pool.execute(
        "INSERT INTO chats (user1_id, user2_id) VALUES (?, ?)",
        [userId, itemOwnerId]
      );
      chatId = result.insertId;
    }

    // Get chat with other user's info
    const [chats] = await pool.execute(
      `SELECT c.*, 
              u.id as other_user_id, u.full_name as other_user_name, 
              u.email as other_user_email, u.profile_pic as user_profile_pic
       FROM chats c
       JOIN users u ON (
         CASE 
           WHEN c.user1_id = ? THEN c.user2_id 
           ELSE c.user1_id 
         END = u.id
       )
       WHERE c.id = ?`,
      [userId, chatId]
    );

    const chat = formatChat(chats[0]);

    res.status(200).json({
      success: true,
      data: chat,
    });
  } catch (error) {
    console.error("Create chat from item error:", error);
    res.status(500).json({
      success: false,
      message: "Error creating chat from item",
      error: error.message,
    });
  }
};

// Get all user's chats
exports.getUserChats = async (req, res) => {
  try {
    const userId = req.userId;

    const [chats] = await pool.execute(
      `SELECT c.*, 
              u.id as other_user_id, u.full_name as other_user_name, 
              u.email as other_user_email, u.profile_pic as user_profile_pic,
              m.content as last_message, m.type as last_message_type, m.created_at as last_message_time
       FROM chats c
       JOIN users u ON (
         CASE 
           WHEN c.user1_id = ? THEN c.user2_id 
           ELSE c.user1_id 
         END = u.id
       )
       LEFT JOIN messages m ON m.id = (
         SELECT id FROM messages 
         WHERE chat_id = c.id 
         ORDER BY created_at DESC 
         LIMIT 1
       )
       WHERE c.user1_id = ? OR c.user2_id = ?
       ORDER BY COALESCE(c.last_message_at, c.created_at) DESC`,
      [userId, userId, userId]
    );

    const formattedChats = chats.map((chat) => formatChat(chat));

    res.status(200).json({
      success: true,
      data: formattedChats,
    });
  } catch (error) {
    console.error("Get user chats error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching chats",
      error: error.message,
    });
  }
};

// Get chat by ID
exports.getChatById = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    const [chats] = await pool.execute(
      `SELECT c.*, 
              u.id as other_user_id, u.full_name as other_user_name, 
              u.email as other_user_email, u.profile_pic as user_profile_pic
       FROM chats c
       JOIN users u ON (
         CASE 
           WHEN c.user1_id = ? THEN c.user2_id 
           ELSE c.user1_id 
         END = u.id
       )
       WHERE c.id = ? AND (c.user1_id = ? OR c.user2_id = ?)`,
      [userId, id, userId, userId]
    );

    if (chats.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Chat not found or you do not have access to it",
      });
    }

    const chat = formatChat(chats[0]);

    res.status(200).json({
      success: true,
      data: chat,
    });
  } catch (error) {
    console.error("Get chat error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching chat",
      error: error.message,
    });
  }
};

// Delete chat
exports.deleteChat = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    // Check if chat belongs to user
    const [chats] = await pool.execute(
      "SELECT * FROM chats WHERE id = ? AND (user1_id = ? OR user2_id = ?)",
      [id, userId, userId]
    );

    if (chats.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Chat not found or you do not have access to it",
      });
    }

    // Delete chat (will cascade delete messages)
    await pool.execute("DELETE FROM chats WHERE id = ?", [id]);

    res.status(200).json({
      success: true,
      message: "Chat deleted successfully",
    });
  } catch (error) {
    console.error("Delete chat error:", error);
    res.status(500).json({
      success: false,
      message: "Error deleting chat",
      error: error.message,
    });
  }
};
