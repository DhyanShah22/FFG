import React, { createContext, useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { assessmentService } from '../services/assessment.service';
import { DEFAULT_RIASEC_SCORES } from '../utils/constants';
import { calculateAssessmentResults } from '../utils/helpers';

export const AssessmentContext = createContext(null);

export const AssessmentProvider = ({ children }) => {
  const { currentUser } = useAuth();
  const [studentAssessments, setStudentAssessments] = useState([]);
  const [activeAssessment, setActiveAssessment] = useState(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [riasecScores, setRiasecScores] = useState(DEFAULT_RIASEC_SCORES);
  const [attemptId, setAttemptId] = useState(null);
  const [isLoadingAssessments, setIsLoadingAssessments] = useState(false);

  useEffect(() => {
    const loadAssessments = async () => {
      if (!currentUser || currentUser.role !== 'student') {
        setStudentAssessments([]);
        return;
      }

      setIsLoadingAssessments(true);
      try {
        const data = await assessmentService.getAssessments(currentUser.id);
        setStudentAssessments(data || []);
      } catch (error) {
        console.error('Unable to load assessments:', error);
        setStudentAssessments([]);
      } finally {
        setIsLoadingAssessments(false);
      }
    };

    loadAssessments();
  }, [currentUser]);

  const startAssessment = async (assessment) => {
    if (!assessment) return;

    setCurrentQuestionIndex(0);
    setSelectedAnswers({});
    setActiveAssessment(assessment);

    try {
      if (currentUser && currentUser.role === 'student') {
        const attempt = await assessmentService.startAssessment(assessment.id, currentUser.id);
        setAttemptId(attempt.attemptId || attempt.id || null);
      }
    } catch (error) {
      console.error('Unable to start assessment:', error);
    }
  };

  const handleSelectOption = (questionId, optionLetter, isMultiSelect = false) => {
    if (isMultiSelect) {
      setSelectedAnswers(prev => {
        const current = Array.isArray(prev[questionId]) ? prev[questionId] : [];
        // Toggle: if already selected, deselect; otherwise add
        const updated = current.includes(optionLetter)
          ? current.filter(l => l !== optionLetter)
          : [...current, optionLetter];
        return { ...prev, [questionId]: updated.length > 0 ? updated : undefined };
      });
    } else {
      setSelectedAnswers(prev => ({
        ...prev,
        [questionId]: optionLetter
      }));
    }
  };

  const handleNextQuestion = () => {
    if (activeAssessment && currentQuestionIndex < activeAssessment.questions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
    }
  };

  const handlePrevQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1);
    }
  };

  const finishAssessment = async () => {
    if (!activeAssessment) return null;

    const updatedScores = calculateAssessmentResults(activeAssessment, selectedAnswers, riasecScores);
    setRiasecScores(updatedScores);

    setStudentAssessments(prev =>
      prev.map(t => (t.id === activeAssessment.id ? { ...t, status: 'completed' } : t))
    );

    if (currentUser && currentUser.completedTests && !currentUser.completedTests.includes(activeAssessment.id)) {
      currentUser.completedTests.push(activeAssessment.id);
    }

    const finishedAssessmentTitle = activeAssessment.title;

    try {
      if (attemptId) {
        await assessmentService.submitAssessment(attemptId, selectedAnswers);
      }
    } catch (error) {
      console.error('Unable to submit assessment:', error);
    }

    setActiveAssessment(null);
    setSelectedAnswers({});
    setAttemptId(null);
    return finishedAssessmentTitle;
  };

  const resetSession = () => {
    setActiveAssessment(null);
    setCurrentQuestionIndex(0);
    setSelectedAnswers({});
    setAttemptId(null);
  };

  return (
    <AssessmentContext.Provider
      value={{
        studentAssessments,
        setStudentAssessments,
        activeAssessment,
        currentQuestionIndex,
        selectedAnswers,
        riasecScores,
        setRiasecScores,
        attemptId,
        isLoadingAssessments,
        startAssessment,
        handleSelectOption,
        handleNextQuestion,
        handlePrevQuestion,
        finishAssessment,
        resetSession
      }}
    >
      {children}
    </AssessmentContext.Provider>
  );
};
