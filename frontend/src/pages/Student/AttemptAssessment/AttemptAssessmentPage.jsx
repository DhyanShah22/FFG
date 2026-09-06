import React, { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAssessment } from '../../../hooks/useAssessment';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';
import { ProgressBar } from '../../../components/assessment/ProgressBar/ProgressBar';
import { QuestionCard } from '../../../components/assessment/QuestionCard/QuestionCard';
import { TestTimer } from '../../../components/assessment/TestTimer/TestTimer';

export const AttemptAssessmentPage = () => {
  const {
    activeAssessment,
    currentQuestionIndex,
    selectedAnswers,
    handleSelectOption,
    handleNextQuestion,
    handlePrevQuestion,
    finishAssessment,
    resetSession
  } = useAssessment();
  const { showToast } = useToast();
  const navigate = useNavigate();

  // Must be declared before any early return (Rules of Hooks)
  const handleTimerExpire = useCallback(async () => {
    showToast('⏰ Time is up! Submitting your answers automatically...');
    const finishedTitle = await finishAssessment();
    if (finishedTitle) {
      navigate(ROUTES.RESULTS);
    }
  }, [finishAssessment, navigate, showToast]);

  if (!activeAssessment) {
    navigate(ROUTES.STUDENT_DASHBOARD);
    return null;
  }

  const currentQuestion = activeAssessment.questions[currentQuestionIndex];

  const handleQuit = () => {
    if (window.confirm('Quit this assessment? Your selections will not be saved.')) {
      resetSession();
      showToast('Assessment session stopped.');
      navigate(ROUTES.STUDENT_DASHBOARD);
    }
  };

  const handleFinish = async () => {
    // Find unanswered mandatory questions
    const unansweredMandatory = activeAssessment.questions.filter(q => {
      if (!q.isMandatory) return false;
      const answer = selectedAnswers[q.id];
      return !answer || (Array.isArray(answer) && answer.length === 0);
    });

    if (unansweredMandatory.length > 0) {
      showToast(`Please answer all required questions (${unansweredMandatory.length} remaining).`);
      return;
    }

    const finishedTitle = await finishAssessment();
    if (finishedTitle) {
      showToast(`Congratulations! You completed ${finishedTitle}`);
      navigate(ROUTES.RESULTS);
    }
  };

  const timerEnabled = Boolean(activeAssessment.timerEnabled);
  const timerTotalSeconds = (activeAssessment.timerMinutes ?? 10) * 60;

  return (
    <div className="main-wrapper slide-up">
      <div className="test-taking-container">
        <div className="test-navbar">
          <div className="test-nav-title-group" style={{ textAlign: 'left' }}>
            <h2>{activeAssessment.title}</h2>
            <span style={{ fontSize: '12px', color: 'var(--color-text-muted)', fontWeight: 600 }}>
              Active Assessment Session
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {timerEnabled && (
              <TestTimer
                totalSeconds={timerTotalSeconds}
                onExpire={handleTimerExpire}
              />
            )}
            <button className="quit-test-btn" onClick={handleQuit}>
              Quit Test
            </button>
          </div>
        </div>

        <ProgressBar current={currentQuestionIndex} total={activeAssessment.questions.length} />

        <QuestionCard
          question={currentQuestion}
          questionIndex={currentQuestionIndex}
          totalQuestions={activeAssessment.questions.length}
          selectedAnswer={selectedAnswers[currentQuestion?.id]}
          onSelectOption={handleSelectOption}
          onPrev={handlePrevQuestion}
          onNext={handleNextQuestion}
          onFinish={handleFinish}
        />
      </div>
    </div>
  );
};
