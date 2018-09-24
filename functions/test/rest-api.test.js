// const localUrl = "http://localhost:5000/photosurfer-aa0ff/us-central1";
// const chai = require('chai');
// chai.use(require('chai-http'));
// const should = chai.should();
// const chaiRequest = chai.request(localUrl)
// const expect = chai.expect;

// describe("Device Registration", () => {


//     var clear = function (done) {
//         chaiRequest
//             .get('/clear')
//             .end(() => {
//                 done()
//             })
//     }

//     before(clear)
//     after(clear)

//     it("Should register a device and return username and token when succeds", (done) => {
//         var device = {
//             username: "foo",
//             token: "bar"
//         };

//         chaiRequest
//             .get('/registerDevice')
//             .query(device)
//             .end(function (err, res) {
//                 res.should.have.status(200);
//                 res.body.should.be.eql(device)
//                 done();
//             });
//     })

//     it("Should get user with two devices token wher they register two devices", (done) => {
//         var device1 = {
//             username: "foo",
//             token: "bar"
//         };
//         var device2 = {
//             username: "foo",
//             token: "bar2"
//         };
//         var requester = chaiRequest.keepOpen()

//         return Promise.all([
//             requester.get('/registerDevice').query(device1),
//             requester.get('/registerDevice').query(device2),
//             requester.get('/obtainUserDevices')])
//             // .then(responses => {
//             //     // var res = responses[2]
//             //     // res.body.should.be.eql({
//             //     //     username: 'foo',
//             //     //     tokens: ['bar', 'bar2']
//             //     // })
//             //     return Promise.resolve()
//             // })
//             .then(() => {
//                 requester.close()
//             })
//             .catch(err => {
//                 throw err
//             })

//     })
// })
