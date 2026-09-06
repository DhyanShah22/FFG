import { apiClient } from './api';

export const adminService = {
  getDashboardStats: async () => {
    const res = await apiClient.get('/admin/dashboard');
    return res.data;
  },

  createAssessment: async (testData) => {
    const res = await apiClient.post('/assessments', testData);
    return res.data;
  },

  createQuestion: async (questionData) => {
    const res = await apiClient.post('/questions', questionData);
    return res.data;
  },

  createQuestionCategory: async (categoryData) => {
    const res = await apiClient.post('/question-categories', categoryData);
    return res.data;
  },

  createQuestionnaire: async (questionnaireData) => {
    const res = await apiClient.post('/questionnaires', questionnaireData);
    return res.data;
  },

  getQuestionnaires: async () => {
    const res = await apiClient.get('/questionnaires');
    return res.data;
  },

  getQuestionnaireVersions: async (questionnaireId) => {
    const res = await apiClient.get(`/questionnaires/${questionnaireId}/versions`);
    return res.data;
  },

  createQuestionnaireVersion: async (questionnaireId, versionData) => {
    const res = await apiClient.post(`/questionnaires/${questionnaireId}/versions`, versionData);
    return res.data;
  },

  addQuestionsToQuestionnaireVersion: async (versionId, questions) => {
    const res = await apiClient.post(`/questionnaire-versions/${versionId}/questions`, { questions });
    return res.data;
  },

  reorderQuestionnaireVersionQuestions: async (versionId, questions) => {
    const res = await apiClient.put(`/questionnaire-versions/${versionId}/questions/reorder`, { questions });
    return res.data;
  },

  publishQuestionnaireVersion: async (versionId) => {
    const res = await apiClient.post(`/questionnaire-versions/${versionId}/publish`, {});
    return res.data;
  },

  updateQuestionTranslations: async (questionId, translations) => {
    const res = await apiClient.put(`/questions/${questionId}/translations`, { translations });
    return res.data;
  },

  addQuestionRules: async (questionId, rules) => {
    const res = await apiClient.post(`/questions/${questionId}/rules`, { rules });
    return res.data;
  },

  createAssessmentVersion: async (assessmentId, versionData) => {
    const res = await apiClient.post(`/assessments/${assessmentId}/versions`, versionData);
    return res.data;
  },

  createAssessmentVersionConfiguration: async (versionId, configurationData) => {
    const res = await apiClient.post(`/assessment-versions/${versionId}/configuration`, configurationData);
    return res.data;
  },

  createAssessmentConfigurationGroup: async (groupData) => {
    const res = await apiClient.post('/assessment-configuration-groups', groupData);
    return res.data;
  },

  addAssessmentConfigurationGroupItems: async (groupId, items) => {
    const res = await apiClient.post(`/assessment-configuration-groups/${groupId}/items`, { items });
    return res.data;
  },

  addAssessmentConfigurationGroupOutputs: async (groupId, outputs) => {
    const res = await apiClient.post(`/assessment-configuration-groups/${groupId}/outputs`, { outputs });
    return res.data;
  },

  assignAssessmentConfigurationGroup: async (groupId, assignments) => {
    const res = await apiClient.post(`/assessment-configuration-groups/${groupId}/assignments`, { assignments });
    return res.data;
  },

  activateAssessmentConfigurationGroup: async (groupId) => {
    const res = await apiClient.post(`/assessment-configuration-groups/${groupId}/activate`, {});
    return res.data;
  },

  createScoringRule: async (ruleData) => {
    const res = await apiClient.post('/scoring-rules', ruleData);
    return res.data;
  },

  createScoringRuleVersion: async (scoringRuleId, versionData) => {
    const res = await apiClient.post(`/scoring-rules/${scoringRuleId}/versions`, versionData);
    return res.data;
  },

  publishScoringRuleVersion: async (versionId) => {
    const res = await apiClient.post(`/scoring-rule-versions/${versionId}/publish`, {});
    return res.data;
  },

  createBenchmark: async (benchmarkData) => {
    const res = await apiClient.post('/benchmarks', benchmarkData);
    return res.data;
  },

  createIarLogicConfig: async (configData) => {
    const res = await apiClient.post('/iar-logic-configs', configData);
    return res.data;
  },

  createIarLogicConfigVersion: async (logicConfigId, versionData) => {
    const res = await apiClient.post(`/iar-logic-configs/${logicConfigId}/versions`, versionData);
    return res.data;
  },

  validateIarLogicVersion: async (versionId, validationData) => {
    const res = await apiClient.post(`/iar-logic-versions/${versionId}/validate`, validationData);
    return res.data;
  },

  publishIarLogicVersion: async (versionId) => {
    const res = await apiClient.post(`/iar-logic-versions/${versionId}/publish`, {});
    return res.data;
  },

  createOrganizationalCluster: async (clusterData) => {
    const res = await apiClient.post('/organizational-clusters', clusterData);
    return res.data;
  },

  addOrganizationalClusterMembers: async (clusterId, members) => {
    const res = await apiClient.post(`/organizational-clusters/${clusterId}/members`, { members });
    return res.data;
  },

  getOrganizationalClusterMembers: async (clusterId) => {
    const res = await apiClient.get(`/organizational-clusters/${clusterId}/members`);
    return res.data;
  },

  assignStudentToFacilitator: async (facilitatorId, studentId) => {
    const res = await apiClient.post(`/facilitators/${facilitatorId}/students`, { studentId });
    return res.data;
  },

  getFacilitatorStudents: async (facilitatorId) => {
    const res = await apiClient.get(`/facilitators/${facilitatorId}/students`);
    return res.data;
  },

  removeStudentFromFacilitator: async (facilitatorId, studentId) => {
    const res = await apiClient.delete(`/facilitators/${facilitatorId}/students/${studentId}`);
    return res.data;
  },

  sendNotification: async (notificationData) => {
    const res = await apiClient.post('/notifications/send', notificationData);
    return res.data;
  },

  getAuditLogs: async () => {
    const res = await apiClient.get('/audit-logs');
    return res.data;
  }
};
