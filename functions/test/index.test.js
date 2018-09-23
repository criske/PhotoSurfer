// const sinon = require('sinon')

// const test = require('firebase-functions-test')({
//     databaseURL: 'https://photosurfer-aa0ff.firebaseio.com',
//     storageBucket: 'photosurfer-aa0ff.appspot.com',
//     projectId: 'photosurfer-aa0ff',
// }, '../functions/photosurfer-aa0ff-7c110eef87c3.json');

// const cloudFunctions = require('../index.js');
// const admin = require('firebase-admin');
// adminInitStub = sinon.stub(admin, 'initializeApp');

// const db = require('../db-ops.js')

// var assert = require('assert');

// // describe('Cloud Functions', () => {

// //     let oldDatabase;

// //     before(() => {
// //         adminInitStub = sinon.stub(admin, 'initializeApp');
// //         oldDatabase = admin.database;
// //     })

// //     after(() => {
// //         // Restore admin.initializeApp() to its original method.
// //         adminInitStub.restore();
// //         admin.database = oldDatabase;
// //         // Do other cleanup tasks.
// //         test.cleanup();
// //     });
// // })

// describe("Database testing", () => {

//     before(() => {
//         db.initRoot()
//     })

//     after(() => {
//         test.cleanup()
//         db.clear()
//     })

//     it("should insert data", () => {
//         // let username = "foo"
//         // db.upsertUserToGroupDevice(username, 'foo-token')
//         //     // .then(() => {return db.getUser(username)})
//         //     // .then(data => {console.log(data.val())})
//         //     .then(value =>{
//         //         console.log(value)
//         //     })
//         //     .catch(err => {console.log(err)})
//         db.root.once('value', (snap)=>{
//             console.log(snap.val())
//         })
//     })
// })

