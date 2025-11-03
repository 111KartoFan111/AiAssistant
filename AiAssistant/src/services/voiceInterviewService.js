import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const getAuthToken = () => {
    return localStorage.getItem('token');
};

const voiceInterviewService = {
    getInterviewDetails: async (sessionId) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/voice-interviews/${sessionId}`,
                {
                    headers: {
                        'Authorization': `Bearer ${getAuthToken()}`
                    }
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching interview details:', error);
            throw error;
        }
    },

    submitAudioAnswer: async (sessionId, audioBlob) => {
        try {
            const formData = new FormData();
            formData.append('audio', audioBlob, 'audio.webm');

            const response = await axios.post(
                `${API_BASE_URL}/voice-interviews/${sessionId}/answer-audio`,
                formData,
                {
                    headers: {
                        'Authorization': `Bearer ${getAuthToken()}`,
                        'Content-Type': 'multipart/form-data'
                    }
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error submitting audio answer:', error);
            throw error;
        }
    }
};

export { voiceInterviewService };