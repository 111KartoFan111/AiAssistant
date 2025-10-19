import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const Resume = () => {
  const navigate = useNavigate();

  // Данные резюме (потом будут из AI анализа)
  const [resumeData, setResumeData] = useState({
    personalInfo: {
      name: 'Ashirbek Selin',
      title: 'Senior Software Developer',
      email: 'asirbekselin@gmail.com',
      phone: '+7 (700) 270 9670',
      location: 'Astana, KZ',
      linkedin: 'linkedin.com/in/selin',
      github: 'github.com/ashirbekselin'
    },
    summary: 'Experienced software developer with 5+ years of expertise in building scalable web applications. Strong background in Java, Spring Boot, and React. Proven track record of delivering high-quality solutions and leading development teams.',
    skills: [
      'Java', 'Spring Boot', 'React', 'JavaScript', 'TypeScript',
      'PostgreSQL', 'MongoDB', 'Docker', 'Kubernetes', 'AWS',
      'Microservices', 'REST APIs', 'Git', 'Agile/Scrum'
    ],
    experience: [
      {
        company: 'Tech Solutions Inc.',
        position: 'Senior Software Developer',
        period: 'Jan 2021 - Present',
        location: 'San Francisco, CA',
        achievements: [
          'Led development of microservices architecture serving 1M+ users',
          'Reduced API response time by 40% through optimization',
          'Mentored team of 5 junior developers',
          'Implemented CI/CD pipeline reducing deployment time by 60%'
        ]
      },
      {
        company: 'StartupXYZ',
        position: 'Full Stack Developer',
        period: 'Mar 2019 - Dec 2020',
        location: 'San Francisco, CA',
        achievements: [
          'Built customer-facing dashboard using React and Spring Boot',
          'Integrated payment processing system handling $2M+ annually',
          'Designed and implemented RESTful APIs',
          'Collaborated with cross-functional teams in Agile environment'
        ]
      }
    ],
    education: [
      {
        degree: 'Bachelor of Science in Computer Science',
        institution: 'University of California',
        period: '2015 - 2019',
        gpa: '3.8/4.0'
      }
    ],
    projects: [
      {
        name: 'E-commerce Platform',
        description: 'Built full-stack e-commerce application with payment integration',
        technologies: ['React', 'Node.js', 'MongoDB', 'Stripe API']
      },
      {
        name: 'Task Management System',
        description: 'Developed collaborative task management tool for teams',
        technologies: ['Spring Boot', 'React', 'PostgreSQL', 'WebSocket']
      }
    ]
  });

  const [isEditing, setIsEditing] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-5xl mx-auto px-6 pt-10 pb-20">
        {/* Навигация и действия */}
        <div className="flex items-center justify-between mb-8">
          <button 
            onClick={() => navigate('/dashboard')}
            className="flex items-center text-gray-600 hover:text-gray-900 transition"
          >
            <span className="mr-2">←</span> Back to Dashboard
          </button>

          <div className="flex gap-3">
            <button 
              onClick={() => setIsEditing(!isEditing)}
              className="px-6 py-2 border-2 border-blue-500 text-blue-500 hover:bg-blue-50 rounded-lg font-medium transition"
            >
              {isEditing ? 'Save' : 'Edit'}
            </button>
            <button className="px-6 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition">
              Download PDF
            </button>
            <button className="px-6 py-2 bg-green-500 hover:bg-green-600 text-white rounded-lg font-medium transition">
              Export
            </button>
          </div>
        </div>

        {/* Само резюме */}
        <div className="bg-white rounded-2xl shadow-lg p-10">
          {/* Заголовок - Personal Info */}
          <div className="border-b-2 border-gray-200 pb-6 mb-6">
            <h1 className="text-4xl font-bold text-gray-900 mb-2">
              {resumeData.personalInfo.name}
            </h1>
            <p className="text-xl text-blue-600 font-medium mb-4">
              {resumeData.personalInfo.title}
            </p>
            <div className="flex flex-wrap gap-4 text-gray-600">
              <span>📧 {resumeData.personalInfo.email}</span>
              <span>📱 {resumeData.personalInfo.phone}</span>
              <span>📍 {resumeData.personalInfo.location}</span>
            </div>
            <div className="flex flex-wrap gap-4 text-blue-600 mt-2">
              <a href="#" className="hover:underline">🔗 {resumeData.personalInfo.linkedin}</a>
              <a href="#" className="hover:underline">💻 {resumeData.personalInfo.github}</a>
            </div>
          </div>

          {/* Professional Summary */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-3 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Professional Summary
            </h2>
            <p className="text-gray-700 leading-relaxed">
              {resumeData.summary}
            </p>
          </div>

          {/* Skills */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-3 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Skills
            </h2>
            <div className="flex flex-wrap gap-2">
              {resumeData.skills.map((skill, index) => (
                <span 
                  key={index}
                  className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg font-medium"
                >
                  {skill}
                </span>
              ))}
            </div>
          </div>

          {/* Work Experience */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Work Experience
            </h2>
            {resumeData.experience.map((job, index) => (
              <div key={index} className="mb-6 last:mb-0">
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <h3 className="text-xl font-semibold text-gray-900">
                      {job.position}
                    </h3>
                    <p className="text-blue-600 font-medium">{job.company}</p>
                  </div>
                  <div className="text-right text-gray-600">
                    <p className="font-medium">{job.period}</p>
                    <p className="text-sm">{job.location}</p>
                  </div>
                </div>
                <ul className="space-y-2 mt-3">
                  {job.achievements.map((achievement, idx) => (
                    <li key={idx} className="text-gray-700 flex items-start">
                      <span className="text-blue-500 mr-2">•</span>
                      <span>{achievement}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>

          {/* Education */}
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Education
            </h2>
            {resumeData.education.map((edu, index) => (
              <div key={index} className="mb-4 last:mb-0">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-xl font-semibold text-gray-900">
                      {edu.degree}
                    </h3>
                    <p className="text-blue-600 font-medium">{edu.institution}</p>
                  </div>
                  <div className="text-right text-gray-600">
                    <p className="font-medium">{edu.period}</p>
                    <p className="text-sm">GPA: {edu.gpa}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Projects */}
          <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="w-1 h-6 bg-blue-500 mr-3"></span>
              Notable Projects
            </h2>
            {resumeData.projects.map((project, index) => (
              <div key={index} className="mb-4 last:mb-0">
                <h3 className="text-xl font-semibold text-gray-900 mb-1">
                  {project.name}
                </h3>
                <p className="text-gray-700 mb-2">{project.description}</p>
                <div className="flex flex-wrap gap-2">
                  {project.technologies.map((tech, idx) => (
                    <span 
                      key={idx}
                      className="px-3 py-1 bg-gray-100 text-gray-700 rounded text-sm font-medium"
                    >
                      {tech}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Информационное сообщение */}
        <div className="mt-6 bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
          <div className="flex items-start">
            <span className="text-blue-500 text-xl mr-3">💡</span>
            <div>
              <p className="text-blue-900 font-medium">AI-Generated Content</p>
              <p className="text-blue-700 text-sm mt-1">
                This resume was automatically generated and improved based on your interview performance and responses. 
                You can edit any section to personalize it further.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Resume;