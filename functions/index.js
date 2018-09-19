const functions = require('firebase-functions');
// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.createCollection = functions.https.onRequest((request, response) => {
    var message = {
        data: {
            id: '850'
        },
        topic: "COLLECTION_CREATED"
    };
    admin.messaging().send(message)
        .then((response) => {
            //response.status(200).send('Successfully sent message')
            return 1
        })
        .catch((error) => {
            // response.status(500).send('Error sending message: ' + error)
            return 2
        });
    response.status(200).send('Successfully sent message')
});
