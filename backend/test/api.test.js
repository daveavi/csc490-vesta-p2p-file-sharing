const request = require('supertest');

const app = require('../src/app');

describe('GET /api/v1', () => {
  it('responds with a json message', function(done) {
    request(app)
      .get('/api/v1')
      .set('Accept', 'application/json')
      .expect('Content-Type', /json/)
      .expect(
        200,
        {
          message: 'API - Hit'
        },
        done
      );
  });
});

describe('GET /api/v1/members', () => {
  it('responds with a json message', function(done) {
    request(app)
      .get('/api/v1/members')
      .set('Accept', 'application/json')
      .expect('Content-Type', /json/)
      .expect(200, ['Avi', 'Kunal', 'Shabaz', 'Swetha', 'Tameem', 'Waleed'], done);
  });
});
