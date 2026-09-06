import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAssessment } from '../../../hooks/useAssessment';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';
import { ResultCard } from '../../../components/assessment/ResultCard/ResultCard';

export const ResultsPage = () => {
  const { riasecScores } = useAssessment();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleBackToDashboard = () => {
    showToast('Returned to Dashboard.');
    navigate(ROUTES.STUDENT_DASHBOARD);
  };

  return (
    <main className="main-wrapper">
      <ResultCard riasecScores={riasecScores} onBack={handleBackToDashboard} />
    </main>
  );
};
