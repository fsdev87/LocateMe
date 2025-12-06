// JWT authentication middleware
const { verifyToken } = require("../utils/jwt");

const authMiddleware = (req, res, next) => {
  try {
    // Get token from header
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({
        success: false,
        message: "No token provided",
      });
    }

    // Extract token (format: "Bearer <token>")
    const token = authHeader.split(" ")[1];

    // Verify token
    const decoded = verifyToken(token);

    // Attach user ID to request object for use in routes
    req.userId = decoded.userId;

    // Continue to next middleware/route handler
    next();
  } catch (error) {
    return res.status(401).json({
      success: false,
      message: "Invalid or expired token",
    });
  }
};

module.exports = authMiddleware;
