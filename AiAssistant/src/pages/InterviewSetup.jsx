import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const InterviewSetup = () => {
  const navigate = useNavigate();
  const [field, setField] = useState('');
  const [position, setPosition] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [resume, setResume] = useState(null);
  const [mode, setMode] = useState('voice'); // voice или text

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      setResume(file);
    }
  };

  const handleStartInterview = (e) => {
    e.preventDefault();
    
    // Передаем данные в интервью
    navigate('/interview', {
      state: {
        field,
        position,
        jobDescription,
        resume: resume?.name,
        mode
      }
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-3xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Back to Dashboard
        </button>

        <div className="bg-white rounded-2xl shadow-lg p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Setup Your Interview</h1>
          <p className="text-gray-600 mb-8">Configure your interview simulation</p>
          
          <form onSubmit={handleStartInterview} className="space-y-6">
            {/* Сфера деятельности */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Field / Industry *
              </label>
              <select
                value={field}
                onChange={(e) => setField(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                required
              >
                <option value="">Select a field</option>
                <option value="it">Information Technology</option>
                <option value="finance">Finance</option>
                <option value="marketing">Marketing</option>
                <option value="sales">Sales</option>
                <option value="hr">Human Resources</option>
                <option value="design">Design</option>
                <option value="engineering">Engineering</option>
                <option value="healthcare">Healthcare</option>
                <option value="education">Education</option>
              </select>
            </div>

            {/* Должность */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Position *
              </label>
              <input
                type="text"
                value={position}
                onChange={(e) => setPosition(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                placeholder="e.g. Senior Java Developer, Product Manager"
                required
              />
            </div>

            {/* Описание вакансии */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Description (Optional)
              </label>
              <textarea
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
                rows="4"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition resize-none"
                placeholder="Paste the job description here for more relevant questions..."
              />
            </div>

            {/* Загрузка резюме */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Upload Resume (Optional)
              </label>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition">
                <input
                  type="file"
                  accept=".pdf,.doc,.docx"
                  onChange={handleFileUpload}
                  className="hidden"
                  id="resume-upload"
                />
                <label 
                  htmlFor="resume-upload"
                  className="cursor-pointer"
                >
                  {resume ? (
                    <div className="text-blue-600">
                      <p className="font-medium">📄 {resume.name}</p>
                      <p className="text-sm text-gray-500 mt-1">Click to change</p>
                    </div>
                  ) : (
                    <div>
                      <p className="text-gray-600 font-medium">📤 Click to upload resume</p>
                      <p className="text-sm text-gray-500 mt-1">PDF, DOC, DOCX (max 5MB)</p>
                    </div>
                  )}
                </label>
              </div>
            </div>

            {/* Выбор режима */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Interview Mode *
              </label>
              <div className="grid grid-cols-2 gap-4">
                <button
                  type="button"
                  onClick={() => setMode('voice')}
                  className={`p-4 border-2 rounded-lg transition ${
                    mode === 'voice'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                >
                  <div className="text-3xl mb-2">🎤</div>
                  <div className="font-medium text-gray-900">Voice Mode</div>
                  <div className="text-sm text-gray-600 mt-1">Speak your answers</div>
                </button>

                <button
                  type="button"
                  onClick={() => setMode('text')}
                  className={`p-4 border-2 rounded-lg transition ${
                    mode === 'text'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                >
                  <div className="text-3xl mb-2">💬</div>
                  <div className="font-medium text-gray-900">Text Mode</div>
                  <div className="text-sm text-gray-600 mt-1">Type your answers</div>
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-4 rounded-lg transition shadow-md hover:shadow-lg"
            >
              Start Interview
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default InterviewSetup;