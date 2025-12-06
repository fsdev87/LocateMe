// Authentication routes
const express = require("express");
const router = express.Router();
const authController = require("../controllers/authController");
const authMiddleware = require("../middleware/auth");

// Public routes
router.post("/signup", authController.signup);
router.post("/login", authController.login);

// Protected routes
router.put("/fcm-token", authMiddleware, authController.updateFcmToken);

module.exports = router;
