// Message routes
const express = require("express");
const router = express.Router();
const messageController = require("../controllers/messageController");
const authMiddleware = require("../middleware/auth");
const { uploadMessageImage } = require("../middleware/upload");

// All routes require authentication
router.use(authMiddleware);

// Get unread message count
router.get("/unread-count", messageController.getUnreadCount);

// Get messages for a specific chat
router.get("/chat/:chatId", messageController.getChatMessages);

// Send message (text or image)
router.post("/", uploadMessageImage, messageController.sendMessage);

// Mark messages as read
router.put("/chat/:chatId/read", messageController.markMessagesAsRead);

// Delete message
router.delete("/:id", messageController.deleteMessage);

module.exports = router;
