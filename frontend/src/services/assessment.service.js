import { apiClient } from './api';

export const assessmentService = {
  getAssessments: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/assessments`);
    return res.data;
  },

  startAssessment: async (assessmentId, studentId = 'usr_student_001') => {
    const res = await apiClient.post(`/assessments/${assessmentId}/start`, { studentId });
    return res.data;
  },

  submitAssessment: async (attemptId, responses) => {
    const res = await apiClient.post(`/attempts/${attemptId}/submit`, { responses });
    return res.data;
  },

  saveAttemptResponses: async (attemptId, responses) => {
    const res = await apiClient.put(`/attempts/${attemptId}/responses`, { responses });
    return res.data;
  },

  saveAttemptQuestionTimings: async (attemptId, timings) => {
    const res = await apiClient.put(`/attempts/${attemptId}/question-timings`, { timings });
    return res.data;
  },

  getActiveAssessmentConfiguration: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/active-assessment-configuration`);
    return res.data;
  },

  getAssignmentHistory: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/assignment-history`);
    return res.data;
  },

  getAttemptHistory: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/attempt-history`);
    return res.data;
  },

  calculateScore: async (attemptId) => {
    const res = await apiClient.post(`/scoring/calculate/${attemptId}`, {});
    return res.data;
  },

  generateRecommendations: async (studentId = 'usr_student_001') => {
    const res = await apiClient.post(`/recommendations/generate/${studentId}`, {});
    return res.data;
  },

  generateReport: async (studentId = 'usr_student_001') => {
    const res = await apiClient.post(`/reports/generate/${studentId}`, {});
    return res.data;
  },

  getReportDownload: async (reportId) => {
    const res = await apiClient.get(`/reports/${reportId}/download`);
    return res.data;
  },

  getStudentScores: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/scores`);
    return res.data;
  },

  getRecommendations: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/recommendations`);
    return res.data;
  },

  getStudentReports: async (studentId = 'usr_student_001') => {
    const res = await apiClient.get(`/students/${studentId}/reports`);
    return res.data;
  }
};
