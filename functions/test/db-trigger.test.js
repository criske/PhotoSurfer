const test = require('firebase-functions-test')({
    databaseURL: 'https://photosurfer-aa0ff.firebaseio.com',
    storageBucket: 'photosurfer-aa0ff.appspot.com',
    projectId: 'photosurfer-aa0ff',
}, '../functions/photosurfer-aa0ff-7c110eef87c3.json');

const sinon = require('sinon');
const proxyquire = require('proxyquire')
const messageOpsStub = {
    './message-ops': {
        sendMessage: (action, tokens) => {
            console.log(`${action} ${tokens}`)
            return Promise.resolve()
        }
    },
};
const cloudFunctions = proxyquire('../index.js', messageOpsStub);
// const cloudFunctions = require('../index.js');
const admin = require('firebase-admin');
adminInitStub = sinon.stub(admin, 'initializeApp');

const dbOps = require('../db-ops.js')

describe('Cloud Functions', () => {

    after(() => {
        // Do cleanup tasks.
        test.cleanup();
        // Reset the database.
        admin.database().ref(dbOps.FCM_USER_GROUP_DEVICES).remove();
        admin.database().ref(dbOps.QUEUE).remove();
    });
    it('should trigger sending message on create', () => {
        test.database.makeDataSnapshot({
            tokens: ['token1', 'token2', 'token3']
        }, `${dbOps.FCM_USER_GROUP_DEVICES}/foo`);
        var snap = test.database.makeDataSnapshot({
            username: 'foo',
            token: 'token1',
            actionType: 'foo-action',
            id: 'id1'
        }, dbOps.QUEUE)
        const wrapped = test.wrap(cloudFunctions.queueActionOperationTriggerCreate);
        return wrapped(snap);
    })

})