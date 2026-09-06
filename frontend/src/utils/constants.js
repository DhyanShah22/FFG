export const MOCK_STUDENTS = [
  {
    username: 'amit.kumar',
    password: 'student123',
    name: 'Amit Kumar',
    branch: 'Antarang Center - Dharavi, Mumbai',
    id: 'ANT-2026-089A',
    role: 'student',
    completedTests: ['values'],
    testScores: { values: { Independence: 80, Achievement: 90, Relationships: 70 } }
  },
  {
    username: 'sneha.sharma',
    password: 'student123',
    name: 'Sneha Sharma',
    branch: 'Antarang Center - Shivaji Nagar, Pune',
    id: 'ANT-2026-112B',
    role: 'student',
    completedTests: ['values'],
    testScores: { values: { Support: 90, Relationships: 85, WorkingConditions: 70 } }
  }
];

export const MOCK_ADMINS = [
  {
    username: 'admin',
    password: 'admin123',
    name: 'Admin User',
    branch: 'Antarang HQ - Mumbai',
    id: 'ADM-2026-001',
    role: 'admin'
  }
];

export const LETTERS = ['A', 'B', 'C', 'D', 'E', 'F'];

export const DEFAULT_RIASEC_SCORES = {
  R: 45,
  I: 50,
  A: 40,
  S: 60,
  E: 55,
  C: 50
};
