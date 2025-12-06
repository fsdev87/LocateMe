// User profile routes
const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authMiddleware = require("../middleware/auth");
const { uploadProfile } = require("../middleware/upload");

// All routes require authentication
router.use(authMiddleware);

// Get current user profile
router.get("/profile", userController.getProfile);

// Update profile (with optional profile picture)
router.put("/profile", uploadProfile, userController.updateProfile);

// Change password
router.put("/change-password", userController.changePassword);

// Get user by ID
router.get("/:id", userController.getUserById);

module.exports = router;
