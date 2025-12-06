const multer = require("multer");
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

createUploadDirs();

// Storage configuration
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    // Determine destination based on file field name
    let uploadPath = "uploads/";
    if (file.fieldname === "profilePic") {
      uploadPath += "profiles/";
    } else if (file.fieldname === "itemImages") {
      uploadPath += "items/";
    } else if (file.fieldname === "messageImage") {
      uploadPath += "messages/";
    }
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    // Generate unique filename: timestamp-randomstring.extension
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    const ext = path.extname(file.originalname);
    cb(null, file.fieldname + "-" + uniqueSuffix + ext);
  },
});

// File filter - only allow images
const fileFilter = (req, file, cb) => {
  const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/gif"];

  if (allowedTypes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    cb(
      new Error("Invalid file type. Only JPEG, PNG and GIF are allowed."),
      false
    );
  }
};

// Multer configuration
const upload = multer({
  storage: storage,
  fileFilter: fileFilter,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB limit
  },
});

// Upload configurations for different routes
const uploadProfile = upload.single("profilePic");
const uploadItemImages = upload.array("itemImages", 5); // Max 5 images
const uploadMessageImage = upload.single("messageImage");

module.exports = {
  uploadProfile,
  uploadItemImages,
  uploadMessageImage,
};
