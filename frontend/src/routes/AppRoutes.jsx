import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../config/routes';
import { ROLES } from '../config/roles';
import { ProtectedRoute } from './ProtectedRoute';

import { AuthLayout } from '../layouts/AuthLayout';
import { StudentLayout } from '../layouts/StudentLayout';
import { AdminLayout } from '../layouts/AdminLayout';

import { LoginPage } from '../pages/Auth/Login/LoginPage';
import { StudentDashboard } from '../pages/Student/Dashboard/StudentDashboard';
import { AttemptAssessmentPage } from '../pages/Student/AttemptAssessment/AttemptAssessmentPage';
import { ResultsPage } from '../pages/Student/Results/ResultsPage';
import { StudentReportsPage } from '../pages/Student/Reports/ReportsPage';

import { AdminDashboard } from '../pages/Admin/Dashboard/AdminDashboard';
import { TestManagementPage } from '../pages/Admin/TestManagement/TestManagementPage';
import { TestEditorPage } from '../pages/Admin/TestEditor/TestEditorPage';
import { AdminStudentsPage } from '../pages/Admin/Students/AdminStudentsPage';

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Auth Routes */}
      <Route element={<AuthLayout />}>
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      </Route>

      {/* Protected Student Portal Routes */}
      <Route
        element={
          <ProtectedRoute requiredRole={ROLES.STUDENT}>
            <StudentLayout />
          </ProtectedRoute>
        }
      >
        <Route path={ROUTES.STUDENT_DASHBOARD} element={<StudentDashboard />} />
        <Route path={ROUTES.ATTEMPT_ASSESSMENT} element={<AttemptAssessmentPage />} />
        <Route path={ROUTES.RESULTS} element={<ResultsPage />} />
        <Route path={ROUTES.STUDENT_REPORTS} element={<StudentReportsPage />} />
      </Route>

      {/* Protected Admin Portal Routes */}
      <Route
        element={
          <ProtectedRoute requiredRole={ROLES.ADMIN}>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route path={ROUTES.ADMIN_DASHBOARD} element={<AdminDashboard />} />
        <Route path={ROUTES.ADMIN_TESTS} element={<TestManagementPage />} />
        <Route path={ROUTES.ADMIN_TEST_EDITOR} element={<TestEditorPage />} />
        <Route path={ROUTES.ADMIN_STUDENTS} element={<AdminStudentsPage />} />
      </Route>

      {/* Fallback Catch-all Route */}
      <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
    </Routes>
  );
};
