// Item controller
const { pool } = require("../config/db");

// Helper function to format item with full image URLs
const formatItem = (item) => {
  if (item.image_urls) {
    console.log("[formatItem] Raw image_urls from DB:", item.image_urls);
    console.log("[formatItem] Type:", typeof item.image_urls);

    try {
      // Check if already an array (MySQL JSON column auto-parses)
      let urls = item.image_urls;

      // If it's a string, parse it
      if (typeof item.image_urls === "string") {
        urls = JSON.parse(item.image_urls);
        console.log("[formatItem] Parsed URLs from string:", urls);
      } else {
        console.log("[formatItem] URLs already an array:", urls);
      }

      // Map to full URLs
      item.image_urls = Array.isArray(urls)
        ? urls.map((url) => `${process.env.SERVER_URL}/${url}`)
        : [];

      console.log("[formatItem] Final image URLs:", item.image_urls);
    } catch (error) {
      console.error("[formatItem] Error processing image URLs:", error.message);
      console.error("[formatItem] Invalid data:", item.image_urls);
      item.image_urls = [];
    }
  } else {
    item.image_urls = [];
  }

  // Add full URL for user profile pic if exists
  if (item.user_profile_pic) {
    item.user_profile_pic = `${process.env.SERVER_URL}/${item.user_profile_pic}`;
  }

  return item;
};

// Create item (lost or found)
exports.createItem = async (req, res) => {
  try {
    console.log("\n=== CREATE ITEM REQUEST ===");
    console.log("[createItem] User ID:", req.userId);
    console.log("[createItem] Body keys:", Object.keys(req.body));
    console.log("[createItem] Title:", req.body.title);
    console.log("[createItem] Category:", req.body.category);
    console.log("[createItem] Type:", req.body.type);
    console.log("[createItem] Has itemImages:", !!req.body.itemImages);
    console.log("[createItem] Saved item images:", req.savedItemImages);

    const userId = req.userId;
    const { title, description, category, location, type, expiresAt } =
      req.body;

    // Validation
    if (!title || !description || !category || !location || !type) {
      return res.status(400).json({
        success: false,
        message: "All required fields must be provided",
      });
    }

    // Validate enums
    const validCategories = [
      "ELECTRONICS",
      "BAGS",
      "KEYS",
      "CLOTHING",
      "OTHER",
    ];
    const validTypes = ["LOST", "FOUND"];

    if (!validCategories.includes(category) || !validTypes.includes(type)) {
      return res.status(400).json({
        success: false,
        message: "Invalid category or type",
      });
    }

    // Handle image uploads (max 5 images from base64)
    let imageUrls = [];
    if (req.savedItemImages && req.savedItemImages.length > 0) {
      imageUrls = req.savedItemImages;
      console.log("[createItem] Image URLs to save:", imageUrls);
    } else {
      console.log("[createItem] No images to save");
    }

    // Insert item
    const imageUrlsJson = JSON.stringify(imageUrls);
    console.log("[createItem] JSON stringified image URLs:", imageUrlsJson);

    const [result] = await pool.execute(
      `INSERT INTO items (user_id, title, description, image_urls, category, location, type, expires_at) 
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        userId,
        title,
        description,
        imageUrlsJson,
        category,
        location,
        type,
        expiresAt || null,
      ]
    );

    console.log("[createItem] Item created with ID:", result.insertId);

    // Get created item with user info
    const [items] = await pool.execute(
      `SELECT i.*, u.full_name as user_name, u.email as user_email, u.profile_pic as user_profile_pic
       FROM items i
       LEFT JOIN users u ON i.user_id = u.id
       WHERE i.id = ?`,
      [result.insertId]
    );

    const item = formatItem(items[0]);

    res.status(201).json({
      success: true,
      message: "Item created successfully",
      data: item,
    });
  } catch (error) {
    console.error("\n=== CREATE ITEM ERROR ===");
    console.error("[createItem] Error name:", error.name);
    console.error("[createItem] Error message:", error.message);
    console.error("[createItem] Error stack:", error.stack);
    console.error(
      "[createItem] Request body:",
      JSON.stringify(req.body, null, 2)
    );

    res.status(500).json({
      success: false,
      message: "Error creating item",
      error: error.message,
    });
  }
};

// Get all items (home feed) with filters
exports.getAllItems = async (req, res) => {
  try {
    const userId = req.userId; // Current logged-in user
    const {
      type,
      category,
      status,
      search,
      limit = 50,
      offset = 0,
    } = req.query;

    console.log("[getAllItems] Current user ID:", userId);

    let query = `
      SELECT i.*, u.full_name as user_name, u.email as user_email, u.profile_pic as user_profile_pic
      FROM items i
      LEFT JOIN users u ON i.user_id = u.id
      WHERE i.deleted_at IS NULL
      AND i.user_id != ?
    `;

    const params = [userId]; // Exclude current user's items

    // Apply filters
    if (type) {
      query += " AND i.type = ?";
      params.push(type);
    }

    if (category) {
      query += " AND i.category = ?";
      params.push(category);
    }

    if (status) {
      query += " AND i.status = ?";
      params.push(status);
    } else {
      // By default, show only ACTIVE items
      query += " AND i.status = ?";
      params.push("ACTIVE");
    }

    if (search) {
      query +=
        " AND (i.title LIKE ? OR i.description LIKE ? OR i.location LIKE ?)";
      const searchPattern = `%${search}%`;
      params.push(searchPattern, searchPattern, searchPattern);
    }

    // Convert limit and offset to integers
    const limitInt = parseInt(limit) || 50;
    const offsetInt = parseInt(offset) || 0;

    query += ` ORDER BY i.created_at DESC LIMIT ${limitInt} OFFSET ${offsetInt}`;

    console.log("[getAllItems] Final query:", query);
    console.log("[getAllItems] Params:", params);

    const [items] = await pool.execute(query, params);

    // Format items with full URLs
    const formattedItems = items.map((item) => formatItem(item));

    res.status(200).json({
      success: true,
      data: formattedItems,
      pagination: {
        limit: parseInt(limit),
        offset: parseInt(offset),
        count: formattedItems.length,
      },
    });
  } catch (error) {
    console.error("Get all items error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching items",
      error: error.message,
    });
  }
};

// Get item by ID
exports.getItemById = async (req, res) => {
  try {
    const { id } = req.params;

    const [items] = await pool.execute(
      `SELECT i.*, u.full_name as user_name, u.email as user_email, u.student_id as user_student_id,
              u.batch as user_batch, u.department as user_department, u.section as user_section,
              u.profile_pic as user_profile_pic
       FROM items i
       LEFT JOIN users u ON i.user_id = u.id
       WHERE i.id = ? AND i.deleted_at IS NULL`,
      [id]
    );

    if (items.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Item not found",
      });
    }

    const item = formatItem(items[0]);

    res.status(200).json({
      success: true,
      data: item,
    });
  } catch (error) {
    console.error("Get item error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching item",
      error: error.message,
    });
  }
};

// Get user's own items
exports.getMyItems = async (req, res) => {
  try {
    const userId = req.userId;
    const { type, status } = req.query;

    let query = `
      SELECT i.*, u.full_name as user_name, u.email as user_email, u.profile_pic as user_profile_pic
      FROM items i
      LEFT JOIN users u ON i.user_id = u.id
      WHERE i.user_id = ? AND i.deleted_at IS NULL
    `;

    const params = [userId];

    if (type) {
      query += " AND i.type = ?";
      params.push(type);
    }

    if (status) {
      query += " AND i.status = ?";
      params.push(status);
    }

    query += " ORDER BY i.created_at DESC";

    const [items] = await pool.execute(query, params);

    const formattedItems = items.map((item) => formatItem(item));

    res.status(200).json({
      success: true,
      data: formattedItems,
    });
  } catch (error) {
    console.error("Get my items error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching your items",
      error: error.message,
    });
  }
};

// Update item
exports.updateItem = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;
    const { title, description, category, location, type, status, expiresAt } =
      req.body;

    // Check if item exists and belongs to user
    const [items] = await pool.execute(
      "SELECT * FROM items WHERE id = ? AND user_id = ? AND deleted_at IS NULL",
      [id, userId]
    );

    if (items.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Item not found or you do not have permission to update it",
      });
    }

    // Build dynamic update query
    let updateFields = [];
    let values = [];

    if (title) {
      updateFields.push("title = ?");
      values.push(title);
    }
    if (description) {
      updateFields.push("description = ?");
      values.push(description);
    }
    if (category) {
      updateFields.push("category = ?");
      values.push(category);
    }
    if (location) {
      updateFields.push("location = ?");
      values.push(location);
    }
    if (type) {
      updateFields.push("type = ?");
      values.push(type);
    }
    if (status) {
      updateFields.push("status = ?");
      values.push(status);
    }
    if (expiresAt !== undefined) {
      updateFields.push("expires_at = ?");
      values.push(expiresAt);
    }

    // Handle new image uploads (base64 converted to file paths)
    if (req.savedItemImages && req.savedItemImages.length > 0) {
      updateFields.push("image_urls = ?");
      values.push(JSON.stringify(req.savedItemImages));
    }

    if (updateFields.length === 0) {
      return res.status(400).json({
        success: false,
        message: "No fields to update",
      });
    }

    values.push(id);

    await pool.execute(
      `UPDATE items SET ${updateFields.join(", ")} WHERE id = ?`,
      values
    );

    // Get updated item
    const [updatedItems] = await pool.execute(
      `SELECT i.*, u.full_name as user_name, u.email as user_email, u.profile_pic as user_profile_pic
       FROM items i
       LEFT JOIN users u ON i.user_id = u.id
       WHERE i.id = ?`,
      [id]
    );

    const item = formatItem(updatedItems[0]);

    res.status(200).json({
      success: true,
      message: "Item updated successfully",
      data: item,
    });
  } catch (error) {
    console.error("Update item error:", error);
    res.status(500).json({
      success: false,
      message: "Error updating item",
      error: error.message,
    });
  }
};

// Delete item (soft delete)
exports.deleteItem = async (req, res) => {
  try {
    const userId = req.userId;
    const { id } = req.params;

    // Check if item exists and belongs to user
    const [items] = await pool.execute(
      "SELECT * FROM items WHERE id = ? AND user_id = ? AND deleted_at IS NULL",
      [id, userId]
    );

    if (items.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Item not found or you do not have permission to delete it",
      });
    }

    // Soft delete
    await pool.execute(
      "UPDATE items SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?",
      [id]
    );

    res.status(200).json({
      success: true,
      message: "Item deleted successfully",
    });
  } catch (error) {
    console.error("Delete item error:", error);
    res.status(500).json({
      success: false,
      message: "Error deleting item",
      error: error.message,
    });
  }
};

// Save item
exports.saveItem = async (req, res) => {
  try {
    const userId = req.userId;
    const { itemId } = req.body;

    if (!itemId) {
      return res.status(400).json({
        success: false,
        message: "Item ID is required",
      });
    }

    // Check if item exists
    const [items] = await pool.execute(
      "SELECT id FROM items WHERE id = ? AND deleted_at IS NULL",
      [itemId]
    );

    if (items.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Item not found",
      });
    }

    // Check if already saved
    const [existing] = await pool.execute(
      "SELECT id FROM saved_items WHERE user_id = ? AND item_id = ?",
      [userId, itemId]
    );

    if (existing.length > 0) {
      return res.status(400).json({
        success: false,
        message: "Item already saved",
      });
    }

    // Save item
    await pool.execute(
      "INSERT INTO saved_items (user_id, item_id) VALUES (?, ?)",
      [userId, itemId]
    );

    res.status(201).json({
      success: true,
      message: "Item saved successfully",
    });
  } catch (error) {
    console.error("Save item error:", error);
    res.status(500).json({
      success: false,
      message: "Error saving item",
      error: error.message,
    });
  }
};

// Unsave item
exports.unsaveItem = async (req, res) => {
  try {
    const userId = req.userId;
    const { itemId } = req.params;

    const [result] = await pool.execute(
      "DELETE FROM saved_items WHERE user_id = ? AND item_id = ?",
      [userId, itemId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        success: false,
        message: "Saved item not found",
      });
    }

    res.status(200).json({
      success: true,
      message: "Item unsaved successfully",
    });
  } catch (error) {
    console.error("Unsave item error:", error);
    res.status(500).json({
      success: false,
      message: "Error unsaving item",
      error: error.message,
    });
  }
};

// Get saved items
exports.getSavedItems = async (req, res) => {
  try {
    const userId = req.userId;

    const [items] = await pool.execute(
      `SELECT i.*, u.full_name as user_name, u.email as user_email, u.profile_pic as user_profile_pic,
              si.created_at as saved_at
       FROM saved_items si
       JOIN items i ON si.item_id = i.id
       LEFT JOIN users u ON i.user_id = u.id
       WHERE si.user_id = ? AND i.deleted_at IS NULL
       ORDER BY si.created_at DESC`,
      [userId]
    );

    const formattedItems = items.map((item) => formatItem(item));

    res.status(200).json({
      success: true,
      data: formattedItems,
    });
  } catch (error) {
    console.error("Get saved items error:", error);
    res.status(500).json({
      success: false,
      message: "Error fetching saved items",
      error: error.message,
    });
  }
};
