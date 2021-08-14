const functions = require("firebase-functions");
const admin= require("firebase-admin");

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
exports.sendNotification = functions.database.ref("/messages/{chatID}/{msgID}")
    .onCreate((snapshot, context) => {
      const chatId = context.params.chatID;
      const messageFrom = snapshot.val().senderID;
      const userId = chatId.replace(messageFrom, "");
      const status = admin.firestore().collection("users").doc(userId)
          .get().then( (doc) => {
            //            const token = doc.data().deviceToken;
            console.log();
            return true;
          });

      return status;
    });
