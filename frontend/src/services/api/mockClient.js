import {
  createSuccessResponse,
  createPaginatedResponse,
  createErrorResponse
} from './responseBuilder';

import {
  initialMockUsers,
  initialMockOrgUnits,
  initialMockRoles,
  initialMockPermissions,
  initialMockFeedback,
  initialMockAuditLogs,
  MOCK_TENANT_ID,
  MOCK_ORG_UNIT_ID
} from './mockData';

import { assessmentsData, careerDatabase, fallbackCareers } from '../../data/assessments';
import { deepClone, genId } from '../../utils/formatters';

// Persistent in-memory storage for mock REST server simulation
let usersStore = [...initialMockUsers];
let testsStore = deepClone(assessmentsData);
let feedbackStore = [...initialMockFeedback];
let auditLogsStore = [...initialMockAuditLogs];
let consentStore = [
  {
    consentId: 'cns_001',
    userId: 'usr_student_001',
    consentType: 'GUARDIAN',
    consentGivenAt: '2026-07-01T10:00:00Z',
    status: 'ACTIVE'
  }
];
let configurationGroupsStore = [
  { id: 'grp_001', tenantId: MOCK_TENANT_ID, code: 'GRADE', name: 'Student Grade', isSystemDefined: true }
];
let configurationsStore = [];
let questionCategoriesStore = [
  { id: 'qc_001', code: 'VALUES', name: 'Values and Life Skills' }
];
let questionnairesStore = [];
let questionnaireVersionsStore = [];
let assessmentConfigurationGroupsStore = [];
let benchmarksStore = [];
let scoringRulesStore = [];
let scoringRuleVersionsStore = [];
let iarLogicConfigsStore = [];
let organizationalClustersStore = [];
let facilitatorAssignments = [];
let reportsStore = [
  {
    reportId: 'rep_001',
    studentId: 'usr_student_001',
    fileName: 'career-report-amit-kumar.pdf',
    generatedAt: '2026-07-24T10:00:00Z',
    downloadUrl: '/api/v1/reports/rep_001/download'
  }
];
let attemptsStore = [];
let studentAssignmentHistory = [
  {
    assignmentId: 'assign_001',
    studentId: 'usr_student_001',
    facilitatorId: 'usr_admin_001',
    assignedAt: '2026-07-15T10:00:00Z',
    status: 'ACTIVE'
  }
];
let studentAttemptHistory = [
  {
    attemptId: 'att_001',
    studentId: 'usr_student_001',
    assessmentId: 'holland_code',
    status: 'COMPLETED',
    score: 85,
    submittedAt: '2026-07-24T10:30:00Z'
  }
];
let activeAssessmentConfigurations = [
  {
    studentId: 'usr_student_001',
    configuration: {
      assessmentType: 'HOLLAND',
      language: 'en',
      sections: ['interests', 'values'],
      timeLimitMinutes: 20
    }
  }
];

const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms));

export const mockClient = {
  // GET request dispatcher
  get: async (url, config = {}) => {
    await delay(250);

    // 8.5 Get Logged-in User
    if (url === '/api/v1/auth/me') {
      const user = usersStore[0];
      return createSuccessResponse(user, "User profile fetched successfully");
    }

    // 9.2 Get User Consent History
    if (url.match(/\/api\/v1\/users\/[^/]+\/consents$/)) {
      const consentHistory = [
        {
          consentId: 'cns_001',
          userId: 'usr_student_001',
          consentType: 'GUARDIAN',
          consentGivenAt: '2026-07-01T10:00:00Z',
          status: 'ACTIVE'
        }
      ];
      return createSuccessResponse(consentHistory, "User consent history fetched successfully");
    }

    // 10.2 List Users
    if (url === '/api/v1/users') {
      return createPaginatedResponse(usersStore, 0, 20, usersStore.length, "Users fetched successfully");
    }

    // 10.3 Get User by ID
    if (url.match(/\/api\/v1\/users\/[^/]+$/)) {
      const parts = url.split('/');
      const userId = parts[parts.length - 1];
      const user = usersStore.find(u => u.id === userId || u.username === userId);
      if (user) {
        return createSuccessResponse(user, "User fetched successfully");
      }
      return createErrorResponse("RESOURCE_NOT_FOUND", "User not found", [], url);
    }

    // 11.1 List Roles
    if (url === '/api/v1/roles') {
      return createSuccessResponse(initialMockRoles, "Roles fetched successfully");
    }

    // 11.2 List Permissions
    if (url === '/api/v1/permissions') {
      return createSuccessResponse(initialMockPermissions, "Permissions fetched successfully");
    }

    // 12.2 List Organization Units
    if (url === '/api/v1/org-units') {
      return createPaginatedResponse(initialMockOrgUnits, 0, 20, initialMockOrgUnits.length, "Org units fetched successfully");
    }

    // 12.3 Get Organization Tree
    if (url === '/api/v1/org-units/tree') {
      const tree = {
        tenantId: MOCK_TENANT_ID,
        rootUnits: initialMockOrgUnits
      };
      return createSuccessResponse(tree, "Org tree fetched successfully");
    }

    // 12.4 Get Organization Unit by ID
    if (url.match(/\/api\/v1\/org-units\/[^/]+$/)) {
      const orgUnitId = url.split('/').pop();
      const orgUnit = initialMockOrgUnits.find(o => o.id === orgUnitId);
      if (orgUnit) {
        return createSuccessResponse(orgUnit, "Org unit fetched successfully");
      }
      return createErrorResponse("RESOURCE_NOT_FOUND", "Organization unit not found", [], url);
    }

    // 14.2 List Configuration Groups
    if (url === '/api/v1/configuration-groups') {
      return createSuccessResponse(configurationGroupsStore, "Configuration groups fetched successfully");
    }

    // 14.4 List Configurations
    if (url === '/api/v1/configurations') {
      return createSuccessResponse(configurationsStore, "Configurations fetched successfully");
    }

    // 14.5 List Languages
    if (url === '/api/v1/languages') {
      const languages = [
        { id: 'lang_en', code: 'en', name: 'English' },
        { id: 'lang_hi', code: 'hi', name: 'Hindi' },
        { id: 'lang_mr', code: 'mr', name: 'Marathi' }
      ];
      return createSuccessResponse(languages, "Languages fetched successfully");
    }

    // 15.1 List Question Categories
    if (url === '/api/v1/question-categories') {
      return createSuccessResponse(questionCategoriesStore, "Question categories fetched successfully");
    }

    // 16.1 List Questionnaires
    if (url === '/api/v1/questionnaires') {
      return createSuccessResponse(questionnairesStore, "Questionnaires fetched successfully");
    }

    // 16.7 List Questionnaire Versions
    if (url.match(/\/api\/v1\/questionnaires\/[^/]+\/versions$/)) {
      const questionnaireId = url.split('/')[4];
      const versions = questionnaireVersionsStore.filter(v => v.questionnaireId === questionnaireId);
      return createSuccessResponse(versions, "Questionnaire versions fetched successfully");
    }

    // 16.5 Preview Questionnaire Version
    if (url.match(/\/api\/v1\/questionnaire-versions\/[^/]+\/preview$/)) {
      const versionId = url.split('/')[3];
      const version = questionnaireVersionsStore.find(v => v.id === versionId);
      return createSuccessResponse(version || { id: versionId, questions: [] }, "Questionnaire preview fetched successfully");
    }

    // 19.1 List Available Assessments for Student
    if (url.match(/\/api\/v1\/students\/[^/]+\/assessments$/)) {
      return createSuccessResponse(testsStore, "Assessments fetched successfully");
    }

    // 19.x Active Assessment Configuration
    if (url.match(/\/api\/v1\/students\/[^/]+\/active-assessment-configuration$/)) {
      const studentId = url.split('/')[3];
      const config = activeAssessmentConfigurations.find(item => item.studentId === studentId);
      return createSuccessResponse(config ? config.configuration : {}, "Active assessment configuration fetched successfully");
    }

    // 13.4 Get Student Assignment History
    if (url.match(/\/api\/v1\/students\/[^/]+\/assignment-history$/)) {
      const studentId = url.split('/')[3];
      const history = studentAssignmentHistory.filter(item => item.studentId === studentId);
      return createSuccessResponse(history, "Student assignment history fetched successfully");
    }

    // 19.6 Get Student Attempt History
    if (url.match(/\/api\/v1\/students\/[^/]+\/attempt-history$/)) {
      const studentId = url.split('/')[3];
      const history = studentAttemptHistory.filter(item => item.studentId === studentId);
      return createSuccessResponse(history, "Student attempt history fetched successfully");
    }

    // 21.x Report Download
    if (url.match(/\/api\/v1\/reports\/[^/]+\/download$/)) {
      const reportId = url.split('/')[3];
      const report = reportsStore.find(r => r.reportId === reportId);
      if (report) {
        return createSuccessResponse(report, "Report download details fetched successfully");
      }
      return createErrorResponse("RESOURCE_NOT_FOUND", "Report not found", [], url);
    }

    // 11.3 List Organizational Cluster Members
    if (url.match(/\/api\/v1\/organizational-clusters\/[^/]+\/members$/)) {
      const clusterId = url.split('/')[3];
      const members = organizationalClustersStore.find(cluster => cluster.id === clusterId)?.members || [];
      return createSuccessResponse(members, "Organizational cluster members fetched successfully");
    }

    // 13.2 Get Facilitator Students
    if (url.match(/\/api\/v1\/facilitators\/[^/]+\/students$/)) {
      const facilitatorId = url.split('/')[3];
      const students = facilitatorAssignments.filter(item => item.facilitatorId === facilitatorId).map(item => item.student);
      return createSuccessResponse(students, "Facilitator assigned students fetched successfully");
    }

    // 15.3 List Questions
    if (url === '/api/v1/questions') {
      const allQuestions = testsStore.flatMap(t => t.questions || []);
      return createPaginatedResponse(allQuestions, 0, 20, allQuestions.length, "Questions fetched successfully");
    }

    // 19.1 List Available Assessments for Student
    if (url.match(/\/api\/v1\/students\/[^/]+\/assessments$/)) {
      return createSuccessResponse(testsStore, "Assessments fetched successfully");
    }

    // 20.6 Get Student Scores
    if (url.match(/\/api\/v1\/students\/[^/]+\/scores$/)) {
      const scores = { R: 45, I: 50, A: 40, S: 60, E: 55, C: 50 };
      return createSuccessResponse(scores, "Student scores fetched successfully");
    }

    // 22.5 Get Student Recommendations
    if (url.match(/\/api\/v1\/students\/[^/]+\/recommendations$/)) {
      const recs = {
        recommendationRunId: 'run_001',
        studentId: 'usr_student_001',
        recommendations: careerDatabase[0] ? careerDatabase[0].careers : fallbackCareers
      };
      return createSuccessResponse(recs, "Student recommendations fetched successfully");
    }

    // 23.5 Get Student Reports
    if (url.match(/\/api\/v1\/students\/[^/]+\/reports$/)) {
      const reports = [
        {
          reportId: 'rep_001',
          studentId: 'usr_student_001',
          fileName: 'career-report-amit-kumar.pdf',
          generatedAt: '2026-07-24T10:00:00Z',
          downloadUrl: '/api/v1/reports/rep_001/download'
        }
      ];
      return createSuccessResponse(reports, "Student reports fetched successfully");
    }

    // 24.2 Get Student Feedback
    if (url.match(/\/api\/v1\/students\/[^/]+\/feedback$/)) {
      return createSuccessResponse(feedbackStore, "Student feedback fetched successfully");
    }

    // 25.1 Student Dashboard
    if (url.match(/\/api\/v1\/students\/[^/]+\/dashboard$/)) {
      const dash = {
        completedCount: 1,
        totalAssessments: testsStore.length,
        upcomingSession: "Friday 4:00 PM",
        pathwayStatus: "Active"
      };
      return createSuccessResponse(dash, "Student dashboard data fetched successfully");
    }

    // 25.3 Admin Dashboard
    if (url === '/api/v1/admin/dashboard') {
      const dash = {
        totalTests: testsStore.length,
        totalQuestions: testsStore.reduce((acc, t) => acc + t.questions.length, 0),
        totalStudents: usersStore.filter(u => u.userType === 'STUDENT').length,
        activeTestsCount: testsStore.filter(t => t.status === 'pending').length,
        testsOverview: testsStore
      };
      return createSuccessResponse(dash, "Admin dashboard data fetched successfully");
    }

    // 28.1 List Audit Logs
    if (url === '/api/v1/audit-logs') {
      return createPaginatedResponse(auditLogsStore, 0, 20, auditLogsStore.length, "Audit logs fetched successfully");
    }

    // Default fallback
    return createSuccessResponse({}, "Data fetched successfully");
  },

  // POST request dispatcher
  post: async (url, payload = {}) => {
    await delay(300);

    // 8.1 Register User
    if (url === '/api/v1/auth/register') {
      const newUser = {
        id: genId(),
        tenantId: MOCK_TENANT_ID,
        primaryOrgUnitId: MOCK_ORG_UNIT_ID,
        username: payload.email ? payload.email.split('@')[0] : 'user',
        email: payload.email || 'user@example.com',
        firstName: payload.firstName || 'New',
        lastName: payload.lastName || 'User',
        userType: payload.userType || 'STUDENT',
        roles: [payload.userType || 'STUDENT'],
        status: 'PENDING',
        completedTests: []
      };
      usersStore.push(newUser);
      return createSuccessResponse({ userId: newUser.id, userType: newUser.userType, status: 'PENDING' }, "User registered successfully");
    }

    // 8.2 Login User
    if (url === '/api/v1/auth/login') {
      const loginId = (payload.loginId || payload.username || '').trim().toLowerCase();
      const pwd = payload.password;
      const user = usersStore.find(u => (u.username.toLowerCase() === loginId || u.email.toLowerCase() === loginId));
      
      if (user && (pwd === 'student123' || pwd === 'admin123')) {
        const authData = {
          accessToken: `jwt-token-${genId()}`,
          refreshToken: `refresh-token-${genId()}`,
          expiresIn: 3600,
          user
        };
        // Log activity
        auditLogsStore.unshift({
          id: genId(),
          entityName: 'USER',
          entityId: user.id,
          performedBy: user.id,
          action: 'USER_LOGIN',
          details: `User ${user.username} logged in`,
          timestamp: new Date().toISOString()
        });
        return createSuccessResponse(authData, "Login successful");
      }
      return createErrorResponse("AUTH_INVALID_CREDENTIALS", "Invalid Username or Password.", [], url);
    }

    // 8.4 Refresh Token
    if (url === '/api/v1/auth/refresh-token') {
      const refreshToken = payload.refreshToken || payload.refresh_token;
      if (refreshToken) {
        return createSuccessResponse({ accessToken: `jwt-token-${genId()}`, refreshToken, expiresIn: 3600 }, "Token refreshed successfully");
      }
      return createErrorResponse("AUTH_INVALID_TOKEN", "Refresh token missing or invalid.", [], url);
    }

    // 8.3 Logout
    if (url === '/api/v1/auth/logout') {
      return createSuccessResponse({}, "Logged out successfully");
    }

    // 9.1 Capture Consent
    if (url === '/api/v1/consents') {
      const consentRecord = {
        consentId: genId(),
        userId: payload.userId,
        consentType: payload.consentType || 'GUARDIAN',
        consentGivenAt: new Date().toISOString()
      };
      return createSuccessResponse(consentRecord, "Consent captured successfully");
    }

    // 10.1 Create User
    if (url === '/api/v1/users') {
      const newUser = {
        id: genId(),
        tenantId: payload.tenantId || MOCK_TENANT_ID,
        primaryOrgUnitId: payload.primaryOrgUnitId || MOCK_ORG_UNIT_ID,
        username: payload.email ? payload.email.split('@')[0] : 'user_' + genId(),
        email: payload.email,
        firstName: payload.firstName,
        lastName: payload.lastName,
        userType: payload.userType || 'STUDENT',
        roles: payload.roleIds || ['STUDENT'],
        status: 'ACTIVE',
        completedTests: []
      };
      usersStore.push(newUser);
      return createSuccessResponse(newUser, "User created successfully");
    }

    // 10.3 Assign Role to User
    if (url.match(/\/api\/v1\/users\/[^/]+\/roles$/)) {
      const userId = url.split('/')[3];
      usersStore = usersStore.map(u => u.id === userId ? { ...u, roles: [...new Set([...(u.roles || []), ...(payload.roleIds || [])])] } : u);
      return createSuccessResponse({ userId, roleIds: payload.roleIds }, "Roles assigned to user successfully");
    }

    // 12.5 Create Organizational Cluster
    if (url === '/api/v1/organizational-clusters') {
      const cluster = { id: genId(), name: payload.name || 'New Cluster', members: payload.members || [] };
      organizationalClustersStore.push(cluster);
      return createSuccessResponse(cluster, "Organizational cluster created successfully");
    }

    // 12.6 Add Cluster Members
    if (url.match(/\/api\/v1\/organizational-clusters\/[^/]+\/members$/)) {
      const clusterId = url.split('/')[3];
      const cluster = organizationalClustersStore.find(c => c.id === clusterId);
      if (cluster) {
        cluster.members = [...new Set([...(cluster.members || []), ...(payload.members || [])])];
      }
      return createSuccessResponse(cluster || { id: clusterId, members: payload.members || [] }, "Organizational cluster members added successfully");
    }

    // 12.6 Facilitate Student Assignment
    if (url.match(/\/api\/v1\/facilitators\/[^/]+\/students$/)) {
      const facilitatorId = url.split('/')[3];
      const studentId = payload.studentId;
      const student = usersStore.find(u => u.id === studentId);
      if (student) {
        facilitatorAssignments.push({ facilitatorId, studentId, student });
      }
      return createSuccessResponse({ facilitatorId, studentId }, "Student assigned to facilitator successfully");
    }

    // 14.3 Create Configuration
    if (url === '/api/v1/configurations') {
      const configuration = { id: genId(), ...payload };
      configurationsStore.push(configuration);
      return createSuccessResponse(configuration, "Configuration created successfully");
    }

    // 15.1 Create Question Category
    if (url === '/api/v1/question-categories') {
      const category = { id: genId(), ...payload };
      questionCategoriesStore.push(category);
      return createSuccessResponse(category, "Question category created successfully");
    }

    // 15.2 Create Question
    if (url === '/api/v1/questions') {
      const newQuestion = {
        id: genId(),
        question: payload.defaultText || payload.question || 'New Question',
        options: payload.options || [],
        correctAnswer: payload.correctAnswer || 'A'
      };
      return createSuccessResponse(newQuestion, "Question created successfully");
    }

    // 16.1 Create Questionnaire
    if (url === '/api/v1/questionnaires') {
      const questionnaire = { id: genId(), ...payload };
      questionnairesStore.push(questionnaire);
      return createSuccessResponse(questionnaire, "Questionnaire created successfully");
    }

    // 16.2 Create Questionnaire Version
    if (url.match(/\/api\/v1\/questionnaires\/[^/]+\/versions$/)) {
      const questionnaireId = url.split('/')[3];
      const version = { id: genId(), questionnaireId, ...payload };
      questionnaireVersionsStore.push(version);
      return createSuccessResponse(version, "Questionnaire version created successfully");
    }

    // 16.3 Add Questions to Questionnaire Version
    if (url.match(/\/api\/v1\/questionnaire-versions\/[^/]+\/questions$/)) {
      const versionId = url.split('/')[3];
      const version = questionnaireVersionsStore.find(v => v.id === versionId) || { id: versionId, questions: [] };
      version.questions = [...(version.questions || []), ...(payload.questions || [])];
      return createSuccessResponse(version, "Questionnaire version questions added successfully");
    }

    // 16.6 Publish Questionnaire Version
    if (url.match(/\/api\/v1\/questionnaire-versions\/[^/]+\/publish$/)) {
      const versionId = url.split('/')[3];
      const version = questionnaireVersionsStore.find(v => v.id === versionId);
      if (version) {
        version.status = 'PUBLISHED';
      }
      return createSuccessResponse({ versionId, status: 'PUBLISHED' }, "Questionnaire version published successfully");
    }

    // 17.1 Create Assessment
    if (url === '/api/v1/assessments') {
      const incomingQuestions = Array.isArray(payload.questions) ? deepClone(payload.questions) : [];
      const newTest = {
        id: payload.id || genId(),
        title: payload.title || payload.name || 'New Assessment',
        description: payload.description || 'Description',
        duration: payload.duration || '10 mins',
        questionsCount: incomingQuestions.length,
        type: payload.type || (payload.code ? payload.code.toLowerCase() : 'aptitude'),
        difficulty: payload.difficulty || 'Medium',
        status: payload.status || 'pending',
        accentClass: payload.accentClass || 'primary-accent',
        iconName: payload.iconName || 'BrainCircuit',
        questions: incomingQuestions
      };
      testsStore.push(newTest);
      return createSuccessResponse(newTest, "Assessment created successfully");
    }

    // 17.2 Create Assessment Version
    if (url.match(/\/api\/v1\/assessments\/[^/]+\/versions$/)) {
      const assessmentId = url.split('/')[3];
      const version = { id: genId(), assessmentId, ...payload };
      testsStore = testsStore.map(test => test.id === assessmentId ? { ...test, versions: [...(test.versions || []), version] } : test);
      return createSuccessResponse(version, "Assessment version created successfully");
    }

    // 17.3 Create Assessment Configuration
    if (url.match(/\/api\/v1\/assessment-versions\/[^/]+\/configuration$/)) {
      const versionId = url.split('/')[3];
      const configuration = { id: genId(), assessmentVersionId: versionId, ...payload };
      return createSuccessResponse(configuration, "Assessment version configuration created successfully");
    }

    // 18.1 Create Assessment Configuration Group
    if (url === '/api/v1/assessment-configuration-groups') {
      const group = { id: genId(), ...payload, active: false };
      assessmentConfigurationGroupsStore.push(group);
      return createSuccessResponse(group, "Assessment configuration group created successfully");
    }

    // 18.2 Add Group Item
    if (url.match(/\/api\/v1\/assessment-configuration-groups\/[^/]+\/items$/)) {
      const groupId = url.split('/')[3];
      const group = assessmentConfigurationGroupsStore.find(g => g.id === groupId) || { id: groupId, items: [] };
      group.items = [...(group.items || []), ...(payload.items || [])];
      return createSuccessResponse(group, "Assessment configuration group items created successfully");
    }

    // 18.3 Add Group Output
    if (url.match(/\/api\/v1\/assessment-configuration-groups\/[^/]+\/outputs$/)) {
      const groupId = url.split('/')[3];
      const group = assessmentConfigurationGroupsStore.find(g => g.id === groupId) || { id: groupId, outputs: [] };
      group.outputs = [...(group.outputs || []), ...(payload.outputs || [])];
      return createSuccessResponse(group, "Assessment configuration group outputs created successfully");
    }

    // 18.4 Create Group Assignment
    if (url.match(/\/api\/v1\/assessment-configuration-groups\/[^/]+\/assignments$/)) {
      const groupId = url.split('/')[3];
      return createSuccessResponse({ groupId, assignments: payload.assignments || [] }, "Assessment configuration group assignments created successfully");
    }

    // 18.5 Activate Group
    if (url.match(/\/api\/v1\/assessment-configuration-groups\/[^/]+\/activate$/)) {
      const groupId = url.split('/')[3];
      assessmentConfigurationGroupsStore = assessmentConfigurationGroupsStore.map(g => g.id === groupId ? { ...g, active: true } : g);
      return createSuccessResponse({ groupId, status: 'ACTIVE' }, "Assessment configuration group activated successfully");
    }

    // 19.2 Start Assessment
    if (url.match(/\/api\/v1\/assessments\/[^/]+\/start$/)) {
      const parts = url.split('/');
      const assessmentId = parts[parts.length - 2];
      const attemptData = {
        attemptId: genId(),
        assessmentId,
        timer: {
          timerMode: "COUNT_UP",
          startedAt: new Date().toISOString(),
          showTimerToStudent: true
        }
      };
      attemptsStore.push({ ...attemptData, studentId: payload.studentId });
      return createSuccessResponse(attemptData, "Assessment started successfully");
    }

    // 19.5 Submit Assessment
    if (url.match(/\/api\/v1\/attempts\/[^/]+\/submit$/)) {
      const attemptId = url.split('/')[2];
      const submitData = {
        attemptId,
        status: "SUBMITTED",
        submittedAt: new Date().toISOString(),
        elapsedSeconds: 180,
        submissionReason: "MANUAL_SUBMIT",
        scoreStatus: "COMPLETED"
      };
      studentAttemptHistory.unshift({ attemptId, ...submitData });
      return createSuccessResponse(submitData, "Assessment submitted successfully");
    }

    // 24.1 Add Counsellor Feedback
    if (url.match(/\/api\/v1\/students\/[^/]+\/feedback$/)) {
      const fb = {
        id: genId(),
        studentId: payload.studentId || 'usr_student_001',
        reportId: payload.reportId || 'rep_001',
        feedbackText: payload.feedbackText,
        recommendationNotes: payload.recommendationNotes,
        visibility: payload.visibility || 'VISIBLE_TO_STUDENT',
        createdAt: new Date().toISOString()
      };
      feedbackStore.unshift(fb);
      return createSuccessResponse(fb, "Feedback saved successfully");
    }

    // 20.1 Create Scoring Rule
    if (url === '/api/v1/scoring-rules') {
      const newRule = { id: genId(), ...payload, status: 'DRAFT' };
      scoringRulesStore.push(newRule);
      return createSuccessResponse(newRule, "Scoring rule created successfully");
    }

    // 20.2 Create Scoring Rule Version
    if (url.match(/\/api\/v1\/scoring-rules\/[^/]+\/versions$/)) {
      const ruleId = url.split('/')[3];
      const version = { id: genId(), scoringRuleId: ruleId, ...payload, status: 'DRAFT' };
      scoringRuleVersionsStore.push(version);
      return createSuccessResponse(version, "Scoring rule version created successfully");
    }

    // 20.3 Publish Scoring Rule Version
    if (url.match(/\/api\/v1\/scoring-rule-versions\/[^/]+\/publish$/)) {
      const versionId = url.split('/')[3];
      const version = scoringRuleVersionsStore.find(v => v.id === versionId);
      if (version) {
        version.status = 'PUBLISHED';
      }
      return createSuccessResponse({ versionId, status: 'PUBLISHED' }, "Scoring rule version published successfully");
    }

    // 20.4 Calculate Score
    if (url.match(/\/api\/v1\/scoring\/calculate\/[^/]+$/)) {
      const attemptId = url.split('/')[4];
      return createSuccessResponse({ attemptId, score: 80, status: 'CALCULATED' }, "Score calculated successfully");
    }

    // 15.6 Add Question Rules
    if (url.match(/\/api\/v1\/questions\/[^/]+\/rules$/)) {
      const questionId = url.split('/')[3];
      return createSuccessResponse({ questionId, rules: payload.rules || [] }, "Question rules added successfully");
    }

    // 20.5 Create Benchmark
    if (url === '/api/v1/benchmarks') {
      const benchmark = { id: genId(), ...payload };
      benchmarksStore.push(benchmark);
      return createSuccessResponse(benchmark, "Benchmark created successfully");
    }

    // 21.1 Create IAR Logic Config
    if (url === '/api/v1/iar-logic-configs') {
      const config = { id: genId(), ...payload };
      iarLogicConfigsStore.push(config);
      return createSuccessResponse(config, "IAR logic config created successfully");
    }

    // 21.2 Create IAR Logic Config Version
    if (url.match(/\/api\/v1\/iar-logic-configs\/[^/]+\/versions$/)) {
      const logicConfigId = url.split('/')[3];
      const version = { id: genId(), iarLogicConfigId: logicConfigId, ...payload, status: 'DRAFT' };
      iarLogicConfigsStore.push(version);
      return createSuccessResponse(version, "IAR logic config version created successfully");
    }

    // 21.3 Validate IAR Logic Version
    if (url.match(/\/api\/v1\/iar-logic-versions\/[^/]+\/validate$/)) {
      const versionId = url.split('/')[3];
      return createSuccessResponse({ versionId, valid: true }, "IAR logic version validated successfully");
    }

    // 21.4 Publish IAR Logic Version
    if (url.match(/\/api\/v1\/iar-logic-versions\/[^/]+\/publish$/)) {
      const versionId = url.split('/')[3];
      return createSuccessResponse({ versionId, status: 'PUBLISHED' }, "IAR logic version published successfully");
    }

    // 22.4 Generate Recommendations
    if (url.match(/\/api\/v1\/recommendations\/generate\/[^/]+$/)) {
      const studentId = url.split('/')[3];
      const payloadResponse = { studentId, recommendations: careerDatabase[0] ? careerDatabase[0].careers : fallbackCareers };
      return createSuccessResponse(payloadResponse, "Recommendations generated successfully");
    }

    // 23.4 Generate Report
    if (url.match(/\/api\/v1\/reports\/generate\/[^/]+$/)) {
      const studentId = url.split('/')[3];
      const report = {
        reportId: genId(),
        studentId,
        fileName: `career-report-${studentId}.pdf`,
        generatedAt: new Date().toISOString(),
        downloadUrl: `/api/v1/reports/${genId()}/download`
      };
      reportsStore.unshift(report);
      return createSuccessResponse(report, "Report generated successfully");
    }

    // 26.1 Send Notification
    if (url === '/api/v1/notifications/send') {
      return createSuccessResponse({ notificationId: genId(), status: 'SENT' }, "Notification sent successfully");
    }

    return createSuccessResponse(payload, "Operation completed successfully");
  },

  // PUT request dispatcher
  put: async (url, payload = {}) => {
    await delay(300);

    // 12.4 Update Organization Unit
    if (url.match(/\/api\/v1\/org-units\/[^/]+$/)) {
      const orgUnitId = url.split('/').pop();
      return createSuccessResponse({ id: orgUnitId, ...payload }, "Organization unit updated successfully");
    }

    // 10.4 Update User
    if (url.match(/\/api\/v1\/users\/[^/]+$/)) {
      const parts = url.split('/');
      const userId = parts[parts.length - 1];
      usersStore = usersStore.map(u => u.id === userId ? { ...u, ...payload } : u);
      return createSuccessResponse({ userId, ...payload }, "User updated successfully");
    }

    // 19.3 Update Attempt Responses
    if (url.match(/\/api\/v1\/attempts\/[^/]+\/responses$/)) {
      const attemptId = url.split('/')[2];
      return createSuccessResponse({ attemptId, responses: payload.responses || [] }, "Attempt responses saved successfully");
    }

    // 19.4 Update Attempt Timings
    if (url.match(/\/api\/v1\/attempts\/[^/]+\/question-timings$/)) {
      const attemptId = url.split('/')[2];
      return createSuccessResponse({ attemptId, timings: payload.timings || [] }, "Attempt question timings saved successfully");
    }

    // 10.6 Update User Platform Language
    if (url.match(/\/api\/v1\/users\/[^/]+\/platform-language$/)) {
      const userId = url.split('/')[4];
      usersStore = usersStore.map(u => u.id === userId ? { ...u, preferredPlatformLanguageCode: payload.languageCode } : u);
      return createSuccessResponse({ userId, platformLanguage: payload.languageCode }, "User platform language updated successfully");
    }

    // 10.7 Update User Assessment Language
    if (url.match(/\/api\/v1\/users\/[^/]+\/assessment-language$/)) {
      const userId = url.split('/')[4];
      usersStore = usersStore.map(u => u.id === userId ? { ...u, preferredAssessmentLanguageCode: payload.languageCode } : u);
      return createSuccessResponse({ userId, assessmentLanguage: payload.languageCode }, "User assessment language updated successfully");
    }

    // 15.4 Update Question
    if (url.match(/\/api\/v1\/questions\/[^/]+$/)) {
      return createSuccessResponse(payload, "Question updated successfully");
    }

    // 15.5 Update Question Translations
    if (url.match(/\/api\/v1\/questions\/[^/]+\/translations$/)) {
      return createSuccessResponse(payload, "Question translations updated successfully");
    }

    // 16.4 Reorder Questionnaire Questions
    if (url.match(/\/api\/v1\/questionnaire-versions\/[^/]+\/questions\/reorder$/)) {
      return createSuccessResponse(payload, "Question order updated successfully");
    }

    return createSuccessResponse(payload, "Updated successfully");
  },

  // PATCH request dispatcher
  patch: async (url, payload = {}) => {
    await delay(250);

    // 10.5 Update User Status
    if (url.match(/\/api\/v1\/users\/[^/]+\/status$/)) {
      const parts = url.split('/');
      const userId = parts[parts.length - 2];
      usersStore = usersStore.map(u => u.id === userId ? { ...u, status: payload.status } : u);
      return createSuccessResponse({ userId, status: payload.status }, "User status updated successfully");
    }

    // 9.3 Withdraw Consent
    if (url.match(/\/api\/v1\/consents\/[^/]+\/withdraw$/)) {
      const consentId = url.split('/')[4];
      consentStore = consentStore.map(c => c.consentId === consentId ? { ...c, status: 'WITHDRAWN' } : c);
      return createSuccessResponse({ consentId, status: 'WITHDRAWN' }, "Consent withdrawn successfully");
    }

    return createSuccessResponse(payload, "Status patched successfully");
  },

  // DELETE request dispatcher
  delete: async (url) => {
    await delay(250);

    // 11.4 Remove Role from User
    if (url.match(/\/api\/v1\/users\/[^/]+\/roles\/[^/]+$/)) {
      return createSuccessResponse({}, "Role removed from user successfully");
    }

    // 13.3 Remove Student from Facilitator
    if (url.match(/\/api\/v1\/facilitators\/[^/]+\/students\/[^/]+$/)) {
      const parts = url.split('/');
      const facilitatorId = parts[3];
      const studentId = parts[5];
      facilitatorAssignments = facilitatorAssignments.filter(item => !(item.facilitatorId === facilitatorId && item.studentId === studentId));
      return createSuccessResponse({}, "Student removed from facilitator successfully");
    }

    return createSuccessResponse({}, "Deleted successfully");
  }
};
