import api from './api';

export const interviewService = {
  // Get remaining interviews for today
  getRemainingInterviews: () => api.get('/interview/remaining'),
  
  // Start a new interview
  startInterview: (data) => api.post('/interview/start', data),
  
  // Get next question
  getNextQuestion: (sessionId) => api.get(`/interview/question/${sessionId}`),
  
  // Submit answer
  submitAnswer: (sessionId, questionNumber, answer) => 
    api.post(`/interview/answer?sessionId=${sessionId}&questionNumber=${questionNumber}`, answer, {
      headers: { 'Content-Type': 'text/plain' }
    }),
  
  // ✅ FIXED: End session with properly-encoded reason & JSON-safe empty body
  endSession: (sessionId, reason) => {
    const url = `/interview/end/${encodeURIComponent(sessionId)}`;
    const params = new URLSearchParams();
    if (reason) params.set('reason', reason);
    const qs = params.toString();
    const fullUrl = qs ? `${url}?${qs}` : url;
    console.log('Ending session with URL:', fullUrl);
    return api.post(fullUrl, {}, {
      headers: { 'Content-Type': 'application/json' },
    });
  },
  
  // Get session summary
  getSummary: (sessionId) => api.get(`/interview/summary/${sessionId}`),
  
  // Get interview history
  getHistory: () => api.get('/interview/history'),
  
  // Check if user is active
  isActive: (sessionId) => api.get(`/interview/active/${sessionId}`),
};