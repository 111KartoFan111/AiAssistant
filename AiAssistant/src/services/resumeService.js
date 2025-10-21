import api from './api';

class ResumeService {
    createResume(resumeData) {
        return api.post('/resume', resumeData);
    }

    getMyResume() {
        return api.get('/resume');
    }

    updateResume(resumeData) {
        return api.put('/resume', resumeData);
    }

    deleteResume() {
        return api.delete('/resume');
    }
}

export default new ResumeService();