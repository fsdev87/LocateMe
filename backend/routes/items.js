// Items CRUD routes
const express = require("express");
const router = express.Router();
const itemController = require("../controllers/itemController");
const authMiddleware = require("../middleware/auth");
const { uploadItemImages } = require("../middleware/upload");

// All routes require authentication
router.use(authMiddleware);

// Get all items (home feed)
router.get("/", itemController.getAllItems);

// Get user's own items
router.get("/my-items", itemController.getMyItems);

// Get saved items
router.get("/saved", itemController.getSavedItems);

// Get item by ID
router.get("/:id", itemController.getItemById);

// Create new item
router.post("/", uploadItemImages, itemController.createItem);

// Update item
router.put("/:id", uploadItemImages, itemController.updateItem);

// Delete item
router.delete("/:id", itemController.deleteItem);

// Save item
router.post("/save", itemController.saveItem);

// Unsave item
router.delete("/save/:itemId", itemController.unsaveItem);

module.exports = router;
