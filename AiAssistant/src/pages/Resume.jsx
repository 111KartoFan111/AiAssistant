import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import ResumeService from '../services/resumeService';

const Resume = () => {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [resumeData, setResumeData] = useState({
    personalInfo: {
      name: '',
      title: '',
      email: '',
      phone: '',
      location: '',
      linkedin: '',
      github: ''
    },
    summary: '',
    skills: [],
    experience: [],
    education: [],
    projects: []
  });

  useEffect(() => {
    loadResume();
  }, []);

  const loadResume = async () => {
    try {
      const response = await ResumeService.getMyResume();
      setResumeData(response.data);
      setIsLoading(false);
    } catch (err) {
      if (err.response?.status === 404 || err.response?.data?.message?.includes('not found')) {
        setIsLoading(false);
      } else {
        setError('Failed to load resume');
        setIsLoading(false);
      }
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    setError('');
    setSuccessMessage('');

    try {
      if (resumeData.id) {
        await ResumeService.updateResume(resumeData);
      } else {
        await ResumeService.createResume(resumeData);
      }
      setSuccessMessage('Resume saved successfully!');
      setIsEditing(false);
      await loadResume();
    } catch (err) {
      setError('Failed to save resume. Please try again.');
      console.error('Save error:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleInputChange = (field, value) => {
    setResumeData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handlePersonalInfoChange = (field, value) => {
    setResumeData(prev => ({
      ...prev,
      personalInfo: {
        ...prev.personalInfo,
        [field]: value
      }
    }));
  };

  const addSkill = () => {
    setResumeData(prev => ({
      ...prev,
      skills: [...prev.skills, '']
    }));
  };

  const updateSkill = (index, value) => {
    setResumeData(prev => ({
      ...prev,
      skills: prev.skills.map((skill, i) => i === index ? value : skill)
    }));
  };

  const removeSkill = (index) => {
    setResumeData(prev => ({
      ...prev,
      skills: prev.skills.filter((_, i) => i !== index)
    }));
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-5xl mx-auto px-6 pt-20 text-center">
          <p className="text-gray-600">Loading resume...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-5xl mx-auto px-6 pt-10 pb-20">
        <div className="flex items-center justify-between mb-8">
          <button 
            onClick={() => navigate('/dashboard')}
            className="flex items-center text-gray-600 hover:text-gray-900 transition"
          >
            <span className="mr-2">←</span> Back to Dashboard
          </button>

          <div className="flex gap-3">
            {isEditing ? (
              <>
                <button 
                  onClick={() => setIsEditing(false)}
                  className="px-6 py-2 border-2 border-gray-300 text-gray-700 hover:bg-gray-50 rounded-lg font-medium transition"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleSave}
                  disabled={isSaving}
                  className="px-6 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition disabled:bg-gray-400"
                >
                  {isSaving ? 'Saving...' : 'Save'}
                </button>
              </>
            ) : (
              <button 
                onClick={() => setIsEditing(true)}
                className="px-6 py-2 border-2 border-blue-500 text-blue-500 hover:bg-blue-50 rounded-lg font-medium transition"
              >
                Edit
              </button>
            )}
          </div>
        </div>

        {error && (
          <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {successMessage && (
          <div className="mb-4 bg-green-50 border-l-4 border-green-500 p-4 rounded">
            <p className="text-green-700">{successMessage}</p>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-lg p-10">
          {/* Personal Info */}
          <div className="border-b-2 border-gray-200 pb-6 mb-6">
            {isEditing ? (
              <div className="space-y-4">
                <input
                  type="text"
                  value={resumeData.personalInfo.name}
                  onChange={(e) => handlePersonalInfoChange('name', e.target.value)}
                  className="text-4xl font-bold text-gray-900 w-full border-b-2 border-gray-300 focus:border-blue-500 outline-none"
                  placeholder="Full Name"
                />
                <input
                  type="text"
                  value={resumeData.personalInfo.title}
                  onChange={(e) => handlePersonalInfoChange('title', e.target.value)}
                  className="text-xl text-blue-600 font-medium w-full border-b-2 border-gray-300 focus:border-blue-500 outline-none"
                  placeholder="Job Title"
                />
                <div className="grid grid-cols-2 gap-4">
                  <input
                    type="email"
                    value={resumeData.personalInfo.email}
                    onChange={(e) => handlePersonalInfoChange('email', e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="Email"
                  />
                  <input
                    type="tel"
                    value={resumeData.personalInfo.phone}
                    onChange={(e) => handlePersonalInfoChange('phone', e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="Phone"
                  />
                  <input
                    type="text"
                    value={resumeData.personalInfo.location}
                    onChange={(e) => handlePersonalInfoChange('location', e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="Location"
                  />
                  <input
                    type="text"
                    value={resumeData.personalInfo.linkedin}
                    onChange={(e) => handlePersonalInfoChange('linkedin', e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                    placeholder="LinkedIn"
                  />
                  <input
                    type="text"
                    value={resumeData.personalInfo.github}
                    onChange={(e) => handlePersonalInfoChange('github', e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none col-span-2"
                    placeholder="GitHub"
                  />
                </div>
              </div>
            ) : (
              <>
                <h1 className="text-4xl font-bold text-gray-900 mb-2">
                  {resumeData.personalInfo.name || 'Your Name'}
                </h1>
                <p className="text-xl text-blue-600 font-medium mb-4">
                  {resumeData.personalInfo.title || 'Your Job Title'}
                </p>
                <div className="flex flex-wrap gap-4 text-gray-600">
                  {resumeData.personalInfo.email && <span>📧 {resumeData.personalInfo.email}</span>}
                  {resumeData.personalInfo.phone && <span>📱 {resumeData.personalInfo.phone}</span>}
                  {resumeData.personalInfo.location && <span>📍 {resumeData.personalInfo.location}</span>}
                </div>
                {(resumeData.personalInfo.linkedin || resumeData.personalInfo.github) && (
                  <div className="flex flex-wrap gap-4 text-blue-600 mt-2">
                    {resumeData.personalInfo.linkedin && <span>🔗 {resumeData.personalInfo.linkedin}</span>}
                    {resumeData.personalInfo.github && <span>💻 {resumeData.personalInfo.github}</span>}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Professional Summary */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-3 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Professional Summary
            </h2>
            {isEditing ? (
              <textarea
                value={resumeData.summary}
                onChange={(e) => handleInputChange('summary', e.target.value)}
                rows="4"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                placeholder="Write a brief professional summary..."
              />
            ) : (
              <p className="text-gray-700 leading-relaxed">
                {resumeData.summary || 'No summary provided yet.'}
              </p>
            )}
          </div>

          {/* Skills */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-3 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Skills
            </h2>
            {isEditing ? (
              <div className="space-y-2">
                {resumeData.skills.map((skill, index) => (
                  <div key={index} className="flex gap-2">
                    <input
                      type="text"
                      value={skill}
                      onChange={(e) => updateSkill(index, e.target.value)}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                      placeholder="Skill name"
                    />
                    <button
                      onClick={() => removeSkill(index)}
                      className="px-3 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                    >
                      Remove
                    </button>
                  </div>
                ))}
                <button
                  onClick={addSkill}
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  + Add Skill
                </button>
              </div>
            ) : (
              <div className="flex flex-wrap gap-2">
                {resumeData.skills.length > 0 ? (
                  resumeData.skills.map((skill, index) => (
                    <span 
                      key={index}
                      className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg font-medium"
                    >
                      {skill}
                    </span>
                  ))
                ) : (
                  <p className="text-gray-500">No skills added yet.</p>
                )}
              </div>
            )}
          </div>
          {/* Information Message */}
          <div className="mt-6 bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
            <div className="flex items-start">
              <span className="text-blue-500 text-xl mr-3">💡</span>
              <div>
                <p className="text-blue-900 font-medium">Resume Manager</p>
                <p className="text-blue-700 text-sm mt-1">
                  Create and maintain your professional resume. You can edit any section and save your changes.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Resume;