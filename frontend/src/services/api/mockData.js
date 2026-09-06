export const MOCK_TENANT_ID = 'tnt_77f8a92b-8a4c-4e12-b91d-001122334455';
export const MOCK_ORG_UNIT_ID = 'org_99a8b76c-5d4e-3f2a-1b0c-998877665544';

export const initialMockUsers = [
  {
    id: 'usr_student_001',
    tenantId: MOCK_TENANT_ID,
    primaryOrgUnitId: MOCK_ORG_UNIT_ID,
    username: 'amit.kumar',
    email: 'amit.kumar@example.com',
    mobileNumber: '9876543210',
    firstName: 'Amit',
    lastName: 'Kumar',
    userType: 'STUDENT',
    roles: ['STUDENT'],
    permissions: ['ASSESSMENT_VIEW', 'ASSESSMENT_SUBMIT'],
    status: 'ACTIVE',
    branch: 'Antarang Center - Dharavi, Mumbai',
    completedTests: ['values'],
    preferredPlatformLanguageCode: 'en',
    preferredAssessmentLanguageCode: 'hi'
  },
  {
    id: 'usr_student_002',
    tenantId: MOCK_TENANT_ID,
    primaryOrgUnitId: MOCK_ORG_UNIT_ID,
    username: 'sneha.sharma',
    email: 'sneha.sharma@example.com',
    mobileNumber: '9876543211',
    firstName: 'Sneha',
    lastName: 'Sharma',
    userType: 'STUDENT',
    roles: ['STUDENT'],
    permissions: ['ASSESSMENT_VIEW', 'ASSESSMENT_SUBMIT'],
    status: 'ACTIVE',
    branch: 'Antarang Center - Shivaji Nagar, Pune',
    completedTests: ['values'],
    preferredPlatformLanguageCode: 'en',
    preferredAssessmentLanguageCode: 'en'
  },
  {
    id: 'usr_admin_001',
    tenantId: MOCK_TENANT_ID,
    primaryOrgUnitId: MOCK_ORG_UNIT_ID,
    username: 'admin',
    email: 'admin@antarang.org',
    mobileNumber: '9876500000',
    firstName: 'Admin',
    lastName: 'User',
    userType: 'ADMIN',
    roles: ['ADMIN'],
    permissions: [
      'ALL_ACCESS',
      'USER_MANAGE',
      'TEST_MANAGE',
      'RBAC_MANAGE',
      'REPORTS_GENERATE',
      'AUDIT_VIEW'
    ],
    status: 'ACTIVE',
    branch: 'Antarang HQ - Mumbai',
    preferredPlatformLanguageCode: 'en',
    preferredAssessmentLanguageCode: 'en'
  }
];

export const initialMockOrgUnits = [
  {
    id: MOCK_ORG_UNIT_ID,
    tenantId: MOCK_TENANT_ID,
    parentOrgUnitId: null,
    orgUnitType: 'INSTITUTION',
    code: 'ANTARANG_HQ',
    name: 'Antarang Center - Dharavi, Mumbai',
    description: 'Main flagship youth career center',
    address: 'Dharavi, Mumbai, Maharashtra'
  },
  {
    id: 'org_pune_002',
    tenantId: MOCK_TENANT_ID,
    parentOrgUnitId: MOCK_ORG_UNIT_ID,
    orgUnitType: 'SCHOOL',
    code: 'ANTARANG_PUNE',
    name: 'Antarang Center - Shivaji Nagar, Pune',
    description: 'Regional career navigation hub',
    address: 'Shivaji Nagar, Pune, Maharashtra'
  }
];

export const initialMockRoles = [
  { id: 'role_student', code: 'STUDENT', name: 'Student Learner', description: 'Can attempt assessments and view reports' },
  { id: 'role_facilitator', code: 'FACILITATOR', name: 'Counselor / Facilitator', description: 'Can view assigned students and add guidance feedback' },
  { id: 'role_admin', code: 'ADMIN', name: 'System Administrator', description: 'Full system management and configuration access' }
];

export const initialMockPermissions = [
  { id: 'perm_view', code: 'ASSESSMENT_VIEW', description: 'View available assessments' },
  { id: 'perm_submit', code: 'ASSESSMENT_SUBMIT', description: 'Submit assessment responses' },
  { id: 'perm_manage', code: 'TEST_MANAGE', description: 'Create and edit questionnaires' },
  { id: 'perm_audit', code: 'AUDIT_VIEW', description: 'View platform audit logs' }
];

export const initialMockFeedback = [
  {
    id: 'fb_101',
    studentId: 'usr_student_001',
    reportId: 'rep_001',
    feedbackText: 'Student displays strong analytical inclination and high social empathy.',
    recommendationNotes: 'Recommend pursuing Data Analytics or Development Coaching pathways.',
    visibility: 'VISIBLE_TO_STUDENT',
    createdAt: '2026-07-20T10:00:00Z'
  }
];

export const initialMockAuditLogs = [
  {
    id: 'log_001',
    entityName: 'USER',
    entityId: 'usr_student_001',
    performedBy: 'usr_admin_001',
    action: 'USER_LOGIN',
    details: 'User authenticated successfully',
    timestamp: '2026-07-24T10:00:00Z'
  },
  {
    id: 'log_002',
    entityName: 'ASSESSMENT',
    entityId: 'holland_code',
    performedBy: 'usr_student_001',
    action: 'ASSESSMENT_START',
    details: 'Started Holland Code Interests Assessment',
    timestamp: '2026-07-24T10:15:00Z'
  }
];
