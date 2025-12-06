// Firebase Cloud Messaging
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
try {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, "\n"),
    }),
  });
  console.log("✅ Firebase Admin SDK initialized");
} catch (error) {
  console.error("❌ Firebase initialization error:", error.message);
}

// Send push notification
const sendPushNotification = async (
  fcmToken,
  title,
  body,
  imageUrl = null,
  data = {}
) => {
  try {
    const message = {
      notification: {
        title,
        body,
      },
      data: {
        ...data,
        click_action: "FLUTTER_NOTIFICATION_CLICK",
      },
      token: fcmToken,
    };

    // Add image if provided
    if (imageUrl) {
      message.notification.imageUrl = imageUrl;
      message.data.image = imageUrl;
    }

    const response = await admin.messaging().send(message);
    console.log("✅ Notification sent successfully:", response);
    return { success: true, response };
  } catch (error) {
    console.error("❌ Error sending notification:", error);
    return { success: false, error: error.message };
  }
};

// Send notification to multiple tokens
const sendMulticastNotification = async (
  fcmTokens,
  title,
  body,
  imageUrl = null,
  data = {}
) => {
  try {
    const message = {
      notification: {
        title,
        body,
      },
      data: {
        ...data,
        click_action: "FLUTTER_NOTIFICATION_CLICK",
      },
      tokens: fcmTokens,
    };

    if (imageUrl) {
      message.notification.imageUrl = imageUrl;
      message.data.image = imageUrl;
    }

    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`✅ ${response.successCount} notifications sent successfully`);
    return { success: true, response };
  } catch (error) {
    console.error("❌ Error sending multicast notification:", error);
    return { success: false, error: error.message };
  }
};

module.exports = {
  sendPushNotification,
  sendMulticastNotification,
};
