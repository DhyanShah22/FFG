import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import { AuthProvider } from './context/AuthContext';
import { AssessmentProvider } from './context/AssessmentContext';
import { AdminProvider } from './context/AdminContext';
import { AppRoutes } from './routes/AppRoutes';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <AssessmentProvider>
            <AdminProvider>
              <AppRoutes />
            </AdminProvider>
          </AssessmentProvider>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}

export default App;
