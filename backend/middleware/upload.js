const path = require("path");
const fs = require("fs");

// Create uploads directories if they don't exist
const createUploadDirs = () => {
  const dirs = [
    "uploads",
    "uploads/profiles",
    "uploads/items",
    "uploads/messages",
  ];
  dirs.forEach((dir) => {
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
      console.log(`✅ Created directory: ${dir}`);
    }
  });
};

// Create directories on startup
createUploadDirs();

/**
 * Decode base64 string and determine image type
 * @param {string} base64String - Base64 encoded image string
 * @returns {Object} - { buffer: Buffer, extension: string, mimeType: string }
 */
const decodeBase64Image = (base64String) => {
  // Check if base64 string includes data URI prefix
  let base64Data = base64String;
  let mimeType = null;

  // Extract MIME type and actual base64 data if data URI format
  const matches = base64String.match(/^data:([A-Za-z-+\/]+);base64,(.+)$/);
  if (matches && matches.length === 3) {
    mimeType = matches[1];
    base64Data = matches[2];
  }

  // Decode base64 to buffer
  const buffer = Buffer.from(base64Data, "base64");

  // Determine image type from MIME type or buffer signature
  let extension = ".jpg"; // default
  if (mimeType) {
    if (mimeType === "image/png") extension = ".png";
    else if (mimeType === "image/gif") extension = ".gif";
    else if (mimeType === "image/jpeg" || mimeType === "image/jpg")
      extension = ".jpg";
  } else {
    // Fallback: detect from buffer signature
    if (buffer[0] === 0x89 && buffer[1] === 0x50) extension = ".png";
    else if (buffer[0] === 0x47 && buffer[1] === 0x49) extension = ".gif";
  }

  return {
    buffer,
    extension,
    mimeType: mimeType || "image/jpeg",
  };
};

/**
 * Validate image size and type
 * @param {Buffer} buffer - Image buffer
 * @param {string} mimeType - MIME type
 * @returns {Object} - { valid: boolean, error: string }
 */
const validateImage = (buffer, mimeType) => {
  const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/gif"];
  const maxSize = 5 * 1024 * 1024; // 5MB

  if (!allowedTypes.includes(mimeType)) {
    return {
      valid: false,
      error: "Invalid file type. Only JPEG, PNG and GIF are allowed.",
    };
  }

  if (buffer.length > maxSize) {
    return {
      valid: false,
      error: "File size exceeds 5MB limit.",
    };
  }

  return { valid: true };
};

/**
 * Save base64 image to disk
 * @param {string} base64String - Base64 encoded image
 * @param {string} uploadDir - Directory to save (profiles/items/messages)
 * @param {string} fieldName - Field name for filename prefix
 * @returns {Promise<string>} - Saved file path
 */
const saveBase64Image = async (base64String, uploadDir, fieldName) => {
  try {
    // Decode base64
    const { buffer, extension, mimeType } = decodeBase64Image(base64String);

    // Validate image
    const validation = validateImage(buffer, mimeType);
    if (!validation.valid) {
      throw new Error(validation.error);
    }

    // Generate unique filename
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    const filename = `${fieldName}-${uniqueSuffix}${extension}`;
    const uploadPath = path.join("uploads", uploadDir);
    const filePath = path.join(uploadPath, filename);

    // Ensure directory exists
    if (!fs.existsSync(uploadPath)) {
      fs.mkdirSync(uploadPath, { recursive: true });
    }

    // Write file to disk
    await fs.promises.writeFile(filePath, buffer);

    // Return path in consistent format (with forward slashes)
    return filePath.replace(/\\/g, "/");
  } catch (error) {
    throw new Error(`Failed to save image: ${error.message}`);
  }
};

/**
 * Middleware to handle base64 profile picture
 */
const processProfilePic = async (req, res, next) => {
  try {
    console.log("[processProfilePic] Has profilePic:", !!req.body.profilePic);

    if (req.body.profilePic) {
      const filePath = await saveBase64Image(
        req.body.profilePic,
        "profiles",
        "profilePic"
      );
      console.log("[processProfilePic] Saved to:", filePath);
      req.savedProfilePic = filePath;
    }
    next();
  } catch (error) {
    console.error("[processProfilePic] Error:", error.message);
    return res.status(400).json({
      success: false,
      message: error.message,
    });
  }
};

/**
 * Middleware to handle base64 item images (array)
 */
const processItemImages = async (req, res, next) => {
  try {
    console.log("[processItemImages] Has itemImages:", !!req.body.itemImages);
    console.log(
      "[processItemImages] Is array:",
      Array.isArray(req.body.itemImages)
    );

    if (req.body.itemImages && Array.isArray(req.body.itemImages)) {
      console.log(
        "[processItemImages] Number of images:",
        req.body.itemImages.length
      );

      if (req.body.itemImages.length > 5) {
        return res.status(400).json({
          success: false,
          message: "Maximum 5 images allowed",
        });
      }

      const savedPaths = [];
      for (let i = 0; i < req.body.itemImages.length; i++) {
        const imageData = req.body.itemImages[i];
        console.log(
          `[processItemImages] Processing image ${i + 1}/${
            req.body.itemImages.length
          }`
        );

        // Check if this is an existing file path or new base64 image
        if (imageData.startsWith("uploads/")) {
          // Existing image - keep the path as-is
          console.log(`[processItemImages] Keeping existing image: ${imageData}`);
          savedPaths.push(imageData);
        } else {
          // New base64 image - decode and save
          console.log(`[processItemImages] Saving new base64 image ${i + 1}`);
          const filePath = await saveBase64Image(
            imageData,
            "items",
            "itemImage"
          );
          console.log(`[processItemImages] Saved image ${i + 1} to:`, filePath);
          savedPaths.push(filePath);
        }
      }

      req.savedItemImages = savedPaths;
      console.log("[processItemImages] All saved paths:", savedPaths);
    } else {
      console.log("[processItemImages] No item images to process");
    }
    next();
  } catch (error) {
    console.error("[processItemImages] Error:", error.message);
    console.error("[processItemImages] Stack:", error.stack);
    return res.status(400).json({
      success: false,
      message: error.message,
    });
  }
};

/**
 * Middleware to handle base64 message image
 */
const processMessageImage = async (req, res, next) => {
  try {
    if (req.body.messageImage) {
      const filePath = await saveBase64Image(
        req.body.messageImage,
        "messages",
        "messageImage"
      );
      req.savedMessageImage = filePath;
    }
    next();
  } catch (error) {
    return res.status(400).json({
      success: false,
      message: error.message,
    });
  }
};

module.exports = {
  processProfilePic,
  processItemImages,
  processMessageImage,
  saveBase64Image, // Export for direct use if needed
};
