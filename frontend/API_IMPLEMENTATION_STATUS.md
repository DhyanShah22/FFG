# Antarang CAP Platform - API Implementation Status & Documentation

This document provides a detailed tracking record of the REST API endpoints specified in the **Antarang CAP Platform Final API Specification Document (v1.0)** and their implementation status in the React frontend application.

---

## 📌 Specification Overview

- **Base URL**: `/api/v1`
- **Format**: JSON
- **Auth**: JWT Bearer Token (`Authorization: Bearer <token>`)
- **Tenant Context**: `X-Tenant-Id` header
- **Standard Success Format**:
  ```json
  {
    "success": true,
    "message": "Request processed successfully",
    "data": {},
    "timestamp": "2026-07-24T16:40:00Z"
  }
  ```
- **Standard Paginated Format**:
  ```json
  {
    "success": true,
    "message": "Records fetched successfully",
    "data": {
      "content": [],
      "page": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "last": false
    },
    "timestamp": "2026-07-24T16:40:00Z"
  }
  ```
- **Standard Error Format**:
  ```json
  {
    "success": false,
    "errorCode": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [],
    "timestamp": "2026-07-24T16:40:00Z",
    "path": "/api/v1/users"
  }
  ```

---

## 📊 Implementation Tracker by Module

### 1. Authentication APIs (Section 8)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 8.1 | `/api/v1/auth/register` | POST | ✅ Completed | `mockClient.post('/api/v1/auth/register')` |
| 8.2 | `/api/v1/auth/login` | POST | ✅ Completed | `mockClient.post('/api/v1/auth/login')` |
| 8.3 | `/api/v1/auth/logout` | POST | ✅ Completed | `mockClient.post('/api/v1/auth/logout')` |
| 8.4 | `/api/v1/auth/refresh-token` | POST | ✅ Completed | `mockClient.post('/api/v1/auth/refresh-token')` |
| 8.5 | `/api/v1/auth/me` | GET | ✅ Completed | `mockClient.get('/api/v1/auth/me')` |

### 2. Consent APIs (Section 9)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 9.1 | `/api/v1/consents` | POST | ✅ Completed | `mockClient.post('/api/v1/consents')` |
| 9.2 | `/api/v1/users/{userId}/consents` | GET | ✅ Completed | `mockClient.get('/api/v1/users/:id/consents')` |
| 9.3 | `/api/v1/consents/{consentId}/withdraw` | POST | ✅ Completed | `mockClient.post('/api/v1/consents/:id/withdraw')` |

### 3. User & Profile Management APIs (Section 10)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 10.1 | `/api/v1/users` | POST | ✅ Completed | `mockClient.post('/api/v1/users')` |
| 10.2 | `/api/v1/users` | GET | ✅ Completed | `mockClient.get('/api/v1/users')` |
| 10.3 | `/api/v1/users/{userId}` | GET | ✅ Completed | `mockClient.get('/api/v1/users/:id')` |
| 10.4 | `/api/v1/users/{userId}` | PUT | ✅ Completed | `mockClient.put('/api/v1/users/:id')` |
| 10.5 | `/api/v1/users/{userId}/status` | PATCH | ✅ Completed | `mockClient.patch('/api/v1/users/:id/status')` |
| 10.6 | `/api/v1/users/{userId}/platform-language` | PUT | ✅ Completed | `mockClient.put('/api/v1/users/:id/platform-language')` |
| 10.7 | `/api/v1/users/{userId}/assessment-language` | PUT | ✅ Completed | `mockClient.put('/api/v1/users/:id/assessment-language')` |

### 4. RBAC APIs (Section 11)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 11.1 | `/api/v1/roles` | GET | ✅ Completed | `mockClient.get('/api/v1/roles')` |
| 11.2 | `/api/v1/permissions` | GET | ✅ Completed | `mockClient.get('/api/v1/permissions')` |
| 11.3 | `/api/v1/users/{userId}/roles` | POST | ✅ Completed | `mockClient.post('/api/v1/users/:id/roles')` |
| 11.4 | `/api/v1/users/{userId}/roles/{roleId}` | DELETE | ✅ Completed | `mockClient.delete('/api/v1/users/:id/roles/:roleId')` |

### 5. Organization & Cluster APIs (Section 12)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 12.1 | `/api/v1/org-units` | POST | ✅ Completed | `mockClient.post('/api/v1/org-units')` |
| 12.2 | `/api/v1/org-units` | GET | ✅ Completed | `mockClient.get('/api/v1/org-units')` |
| 12.3 | `/api/v1/org-units/tree` | GET | ✅ Completed | `mockClient.get('/api/v1/org-units/tree')` |
| 12.4 | `/api/v1/org-units/{orgUnitId}` | PUT | ✅ Completed | `mockClient.put('/api/v1/org-units/:id')` |
| 12.5 | `/api/v1/organizational-clusters` | POST | ✅ Completed | `mockClient.post('/api/v1/organizational-clusters')` |
| 12.6 | `/api/v1/organizational-clusters/{clusterId}/members` | POST | ✅ Completed | `mockClient.post('/api/v1/organizational-clusters/:id/members')` |
| 12.7 | `/api/v1/organizational-clusters/{clusterId}/members` | GET | ✅ Completed | `mockClient.get('/api/v1/organizational-clusters/:id/members')` |

### 6. Student-Facilitator Assignment APIs (Section 13)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 13.1 | `/api/v1/facilitators/{facilitatorId}/students` | POST | ✅ Completed | `mockClient.post('/api/v1/facilitators/:id/students')` |
| 13.2 | `/api/v1/facilitators/{facilitatorId}/students` | GET | ✅ Completed | `mockClient.get('/api/v1/facilitators/:id/students')` |
| 13.3 | `/api/v1/facilitators/{facilitatorId}/students/{studentId}` | DELETE | ✅ Completed | `mockClient.delete('/api/v1/facilitators/:id/students/:sid')` |
| 13.4 | `/api/v1/students/{studentId}/assignment-history` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/assignment-history')` |

### 7. Configuration & Language APIs (Section 14)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 14.1 | `/api/v1/configuration-groups` | POST | ✅ Completed | `mockClient.post('/api/v1/configuration-groups')` |
| 14.2 | `/api/v1/configuration-groups` | GET | ✅ Completed | `mockClient.get('/api/v1/configuration-groups')` |
| 14.3 | `/api/v1/configurations` | POST | ✅ Completed | `mockClient.post('/api/v1/configurations')` |
| 14.4 | `/api/v1/configurations` | GET | ✅ Completed | `mockClient.get('/api/v1/configurations')` |
| 14.5 | `/api/v1/languages` | GET | ✅ Completed | `mockClient.get('/api/v1/languages')` |

### 8. Question Bank APIs (Section 15)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 15.1 | `/api/v1/question-categories` | POST | ✅ Completed | `mockClient.post('/api/v1/question-categories')` |
| 15.2 | `/api/v1/questions` | POST | ✅ Completed | `mockClient.post('/api/v1/questions')` |
| 15.3 | `/api/v1/questions` | GET | ✅ Completed | `mockClient.get('/api/v1/questions')` |
| 15.4 | `/api/v1/questions/{questionId}` | PUT | ✅ Completed | `mockClient.put('/api/v1/questions/:id')` |
| 15.5 | `/api/v1/questions/{questionId}/translations` | PUT | ✅ Completed | `mockClient.put('/api/v1/questions/:id/translations')` |
| 15.6 | `/api/v1/questions/{questionId}/rules` | POST | ✅ Completed | `mockClient.post('/api/v1/questions/:id/rules')` |

### 9. Questionnaire APIs (Section 16)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 16.1 | `/api/v1/questionnaires` | POST | ✅ Completed | `mockClient.post('/api/v1/questionnaires')` |
| 16.2 | `/api/v1/questionnaires/{questionnaireId}/versions` | POST | ✅ Completed | `mockClient.post('/api/v1/questionnaires/:id/versions')` |
| 16.3 | `/api/v1/questionnaire-versions/{versionId}/questions` | POST | ✅ Completed | `mockClient.post('/api/v1/questionnaire-versions/:id/questions')` |
| 16.4 | `/api/v1/questionnaire-versions/{versionId}/questions/reorder` | PUT | ✅ Completed | `mockClient.put('/api/v1/questionnaire-versions/:id/questions/reorder')` |
| 16.5 | `/api/v1/questionnaire-versions/{versionId}/preview` | GET | ✅ Completed | `mockClient.get('/api/v1/questionnaire-versions/:id/preview')` |
| 16.6 | `/api/v1/questionnaire-versions/{versionId}/publish` | POST | ✅ Completed | `mockClient.post('/api/v1/questionnaire-versions/:id/publish')` |
| 16.7 | `/api/v1/questionnaires/{questionnaireId}/versions` | GET | ✅ Completed | `mockClient.get('/api/v1/questionnaires/:id/versions')` |

### 10. Assessment Configuration APIs (Section 17 & 18)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 17.1 | `/api/v1/assessments` | POST | ✅ Completed | `mockClient.post('/api/v1/assessments')` |
| 17.2 | `/api/v1/assessments/{assessmentId}/versions` | POST | ✅ Completed | `mockClient.post('/api/v1/assessments/:id/versions')` |
| 17.3 | `/api/v1/assessment-versions/{versionId}/configuration` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-versions/:id/configuration')` |
| 18.1 | `/api/v1/assessment-configuration-groups` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-configuration-groups')` |
| 18.2 | `/api/v1/assessment-configuration-groups/{groupId}/items` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-configuration-groups/:id/items')` |
| 18.3 | `/api/v1/assessment-configuration-groups/{groupId}/outputs` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-configuration-groups/:id/outputs')` |
| 18.4 | `/api/v1/assessment-configuration-groups/{groupId}/assignments` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-configuration-groups/:id/assignments')` |
| 18.5 | `/api/v1/assessment-configuration-groups/{groupId}/activate` | POST | ✅ Completed | `mockClient.post('/api/v1/assessment-configuration-groups/:id/activate')` |
| 18.6 | `/api/v1/students/{studentId}/active-assessment-configuration` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/active-assessment-configuration')` |

### 11. Assessment Execution APIs (Section 19)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 19.1 | `/api/v1/students/{studentId}/assessments` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/assessments')` |
| 19.2 | `/api/v1/assessments/{assessmentId}/start` | POST | ✅ Completed | `mockClient.post('/api/v1/assessments/:id/start')` |
| 19.3 | `/api/v1/attempts/{attemptId}/responses` | PUT | ✅ Completed | `mockClient.put('/api/v1/attempts/:id/responses')` |
| 19.4 | `/api/v1/attempts/{attemptId}/question-timings` | PUT | ✅ Completed | `mockClient.put('/api/v1/attempts/:id/question-timings')` |
| 19.5 | `/api/v1/attempts/{attemptId}/submit` | POST | ✅ Completed | `mockClient.post('/api/v1/attempts/:id/submit')` |
| 19.6 | `/api/v1/students/{studentId}/attempt-history` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/attempt-history')` |

### 12. Scoring & Benchmark APIs (Section 20 & 21)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 20.1 | `/api/v1/scoring-rules` | POST | ✅ Completed | `mockClient.post('/api/v1/scoring-rules')` |
| 20.2 | `/api/v1/scoring-rules/{scoringRuleId}/versions` | POST | ✅ Completed | `mockClient.post('/api/v1/scoring-rules/:id/versions')` |
| 20.3 | `/api/v1/scoring-rule-versions/{versionId}/publish` | POST | ✅ Completed | `mockClient.post('/api/v1/scoring-rule-versions/:id/publish')` |
| 20.4 | `/api/v1/scoring/calculate/{attemptId}` | POST | ✅ Completed | `mockClient.post('/api/v1/scoring/calculate/:id')` |
| 20.5 | `/api/v1/benchmarks` | POST | ✅ Completed | `mockClient.post('/api/v1/benchmarks')` |
| 20.6 | `/api/v1/students/{studentId}/scores` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/scores')` |
| 21.1 | `/api/v1/iar-logic-configs` | POST | ✅ Completed | `mockClient.post('/api/v1/iar-logic-configs')` |
| 21.2 | `/api/v1/iar-logic-configs/{logicConfigId}/versions` | POST | ✅ Completed | `mockClient.post('/api/v1/iar-logic-configs/:id/versions')` |
| 21.3 | `/api/v1/iar-logic-versions/{versionId}/validate` | POST | ✅ Completed | `mockClient.post('/api/v1/iar-logic-versions/:id/validate')` |
| 21.4 | `/api/v1/iar-logic-versions/{versionId}/publish` | POST | ✅ Completed | `mockClient.post('/api/v1/iar-logic-versions/:id/publish')` |

### 13. Career, Report, Dashboard & External APIs (Sections 22-28)
| ID | Endpoint | Method | Status | Handler / Mock Function |
|---|---|---|---|---|
| 22.4 | `/api/v1/recommendations/generate/{studentId}` | POST | ✅ Completed | `mockClient.post('/api/v1/recommendations/generate/:id')` |
| 22.5 | `/api/v1/students/{studentId}/recommendations` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/recommendations')` |
| 23.4 | `/api/v1/reports/generate/{studentId}` | POST | ✅ Completed | `mockClient.post('/api/v1/reports/generate/:id')` |
| 23.5 | `/api/v1/students/{studentId}/reports` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/reports')` |
| 23.6 | `/api/v1/reports/{reportId}/download` | GET | ✅ Completed | `mockClient.get('/api/v1/reports/:id/download')` |
| 24.1 | `/api/v1/students/{studentId}/feedback` | POST | ✅ Completed | `mockClient.post('/api/v1/students/:id/feedback')` |
| 24.2 | `/api/v1/students/{studentId}/feedback` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/feedback')` |
| 25.1 | `/api/v1/students/{studentId}/dashboard` | GET | ✅ Completed | `mockClient.get('/api/v1/students/:id/dashboard')` |
| 25.3 | `/api/v1/admin/dashboard` | GET | ✅ Completed | `mockClient.get('/api/v1/admin/dashboard')` |
| 26.1 | `/api/v1/notifications/send` | POST | ✅ Completed | `mockClient.post('/api/v1/notifications/send')` |
| 28.1 | `/api/v1/audit-logs` | GET | ✅ Completed | `mockClient.get('/api/v1/audit-logs')` |

---

## 📝 Next Session Guidance

1. All mock API endpoints are routed through `src/services/api/mockClient.js`.
2. Mock responses follow the standardized payload formats specified in Sections 4.1, 4.2, and 4.3 of `Antarang CAP Platform API Specification.pdf`.
3. Authentication token handling (`Bearer jwt-token`) and `X-Tenant-Id` header handling are simulated inside `mockClient.js`.
