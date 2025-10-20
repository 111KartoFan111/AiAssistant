import api from './api';

class InterviewService {
    startInterview(setupData) {
        return api.post('/interviews/start', setupData);
    }
    
    submitAnswer(interviewId, answerData) {
        return api.post(`/interviews/${interviewId}/answer`, answerData);
    }

    getInterviewHistory() {
        return api.get('/interviews');
    }

    getInterviewDetails(interviewId) {
        return api.get(`/interviews/${interviewId}`);
    }
}

export default new InterviewService();
