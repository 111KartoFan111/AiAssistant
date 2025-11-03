import api from './api';

class ProgressService {
    // Получить общую аналитику прогресса
    getProgressAnalytics() {
        return api.get('/analytics/progress');
    }

    // Получить аналитику по навыкам
    getSkillsAnalytics() {
        return api.get('/analytics/skills');
    }

    // Экспортировать результаты в PDF
    exportInterviewPDF(interviewId) {
        return api.get(`/export/${interviewId}`, {
            responseType: 'blob',
        });
    }

    // Экспортировать несколько интервью
    exportMultipleInterviews(interviewIds) {
        return api.post('/export/multiple', 
            { interviewIds, format: 'PDF' },
            { responseType: 'blob' }
        );
    }
}

export default new ProgressService();