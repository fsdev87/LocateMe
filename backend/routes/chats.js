// Chat routes
const express = require("express");
const router = express.Router();
const chatController = require("../controllers/chatController");
const authMiddleware = require("../middleware/auth");

// All routes require authentication
router.use(authMiddleware);

// Get all user's chats
router.get("/", chatController.getUserChats);

// Create or get existing chat
router.post("/", chatController.createOrGetChat);

// Create or get chat from item (convenience endpoint)
router.post("/from-item/:itemId", chatController.createChatFromItem);

// Get chat by ID
router.get("/:id", chatController.getChatById);

// Delete chat
router.delete("/:id", chatController.deleteChat);

module.exports = router;
