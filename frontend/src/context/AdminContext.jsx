import React, { createContext, useState, useEffect } from 'react';
import { assessmentsData } from '../data/assessments';
import { deepClone, genId } from '../utils/formatters';
import { LETTERS } from '../utils/constants';
import { adminService } from '../services/admin.service';
import { useAuth } from '../hooks/useAuth';

export const AdminContext = createContext(null);

export const AdminProvider = ({ children }) => {
  const { currentUser } = useAuth();
  const [adminTests, setAdminTests] = useState(() => deepClone(assessmentsData));
  const [adminActiveTab, setAdminActiveTab] = useState('overview');
  const [editingTestDraft, setEditingTestDraft] = useState(null);
  const [showQuestionModal, setShowQuestionModal] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [editingQuestionIdx, setEditingQuestionIdx] = useState(null);
  const [dashboardStats, setDashboardStats] = useState(null);

  useEffect(() => {
    const loadDashboardStats = async () => {
      if (currentUser?.role !== 'admin') {
        setDashboardStats(null);
        return;
      }
      try {
        const stats = await adminService.getDashboardStats();
        setDashboardStats(stats);
      } catch (error) {
        console.error('Unable to fetch dashboard stats:', error);
      }
    };

    loadDashboardStats();
  }, [currentUser]);

  const adminCreateNewTest = async () => {
    const newTest = {
      id: genId(),
      title: 'New Assessment',
      duration: '10 mins',
      questionsCount: 0,
      type: 'aptitude',
      difficulty: 'Medium',
      status: 'pending',
      description: 'Enter a description for this assessment.',
      accentClass: 'primary-accent',
      iconName: 'BrainCircuit',
      timerEnabled: false,
      timerMinutes: 10,
      questions: []
    };

    try {
      const persisted = await adminService.createAssessment(newTest);
      const created = persisted || newTest;
      setAdminTests(prev => [...prev, created]);
      setEditingTestDraft(deepClone(created));
      return created;
    } catch (error) {
      console.error('Unable to create new test:', error);
      setAdminTests(prev => [...prev, newTest]);
      setEditingTestDraft(deepClone(newTest));
      return newTest;
    }
  };

  const adminOpenTestEditor = (test) => {
    setEditingTestDraft(deepClone(test));
  };

  const adminSaveTest = async () => {
    if (!editingTestDraft) return null;
    const updated = { ...editingTestDraft, questionsCount: editingTestDraft.questions.length };

    if (!adminTests.some(t => t.id === updated.id)) {
      try {
        const persisted = await adminService.createAssessment(updated);
        const created = persisted || updated;
        setAdminTests(prev => [...prev, created]);
        setEditingTestDraft(created);
        return created;
      } catch (error) {
        console.error('Unable to create assessment on save:', error);
      }
    }

    setAdminTests(prev => prev.map(t => (t.id === updated.id ? updated : t)));
    setEditingTestDraft(updated);
    return updated;
  };

  const adminDeleteTest = (id) => {
    setAdminTests(prev => prev.filter(t => t.id !== id));
  };

  const adminToggleStatus = (id) => {
    setAdminTests(prev =>
      prev.map(t => (t.id === id ? { ...t, status: t.status === 'pending' ? 'locked' : 'pending' } : t))
    );
  };

  const updateDraftField = (field, value) => {
    setEditingTestDraft(prev => (prev ? { ...prev, [field]: value } : prev));
  };

  const adminOpenQuestionEditor = (question, idx) => {
    setEditingQuestion(
      question
        ? deepClone(question)
        : {
            id: genId(),
            question: '',
            explanation: '',
            correctAnswer: 'A',
            correctAnswers: [],
            isMultiSelect: false,
            isMandatory: false,
            options: [
              { letter: 'A', text: '', weight: 0, category: '' },
              { letter: 'B', text: '', weight: 0, category: '' },
              { letter: 'C', text: '', weight: 0, category: '' },
              { letter: 'D', text: '', weight: 0, category: '' }
            ]
          }
    );
    setEditingQuestionIdx(idx);
    setShowQuestionModal(true);
  };

  const adminSaveQuestion = () => {
    if (!editingQuestion || !editingQuestion.question.trim()) {
      throw new Error('Question text cannot be empty.');
    }
    setEditingTestDraft(prev => {
      if (!prev) return prev;
      const questions = [...prev.questions];
      if (editingQuestionIdx === null || editingQuestionIdx === undefined) {
        questions.push(editingQuestion);
      } else {
        questions[editingQuestionIdx] = editingQuestion;
      }
      return { ...prev, questions };
    });
    setShowQuestionModal(false);
  };

  const adminImportQuestions = (questions = []) => {
    if (!editingTestDraft) return;

    const normalizedQuestions = questions
      .filter(Boolean)
      .map(question => ({
        ...question,
        id: question.id || genId(),
        options: question.options || []
      }));

    if (normalizedQuestions.length === 0) return;

    setEditingTestDraft(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        questions: [...(prev.questions || []), ...normalizedQuestions]
      };
    });
  };

  const adminDeleteQuestion = (idx) => {
    setEditingTestDraft(prev => (prev ? { ...prev, questions: prev.questions.filter((_, i) => i !== idx) } : prev));
  };

  const updateQField = (field, value) => {
    setEditingQuestion(prev => (prev ? { ...prev, [field]: value } : prev));
  };

  const updateOptField = (index, field, value) => {
    setEditingQuestion(prev => {
      if (!prev) return prev;
      const options = [...prev.options];
      options[index] = {
        ...options[index],
        [field]: field === 'weight' ? Number(value) : value
      };
      return { ...prev, options };
    });
  };

  const addOption = () => {
    if (!editingQuestion) return;
    if (editingQuestion.options.length >= 6) {
      throw new Error('Maximum 6 options.');
    }
    const letter = LETTERS[editingQuestion.options.length];
    setEditingQuestion(prev => ({
      ...prev,
      options: [...prev.options, { letter, text: '', weight: 0, category: '' }]
    }));
  };

  const removeOption = (index) => {
    if (!editingQuestion) return;
    if (editingQuestion.options.length <= 2) {
      throw new Error('Minimum 2 options required.');
    }
    setEditingQuestion(prev => {
      const options = prev.options
        .filter((_, idx) => idx !== index)
        .map((opt, idx) => ({ ...opt, letter: LETTERS[idx] }));
      return { ...prev, options };
    });
  };

  return (
    <AdminContext.Provider
      value={{
        adminTests,
        setAdminTests,
        adminActiveTab,
        setAdminActiveTab,
        editingTestDraft,
        setEditingTestDraft,
        showQuestionModal,
        setShowQuestionModal,
        editingQuestion,
        editingQuestionIdx,
        adminCreateNewTest,
        adminOpenTestEditor,
        adminSaveTest,
        adminDeleteTest,
        adminToggleStatus,
        updateDraftField,
        adminOpenQuestionEditor,
        adminSaveQuestion,
        adminImportQuestions,
        adminDeleteQuestion,
        updateQField,
        updateOptField,
        addOption,
        removeOption,
        dashboardStats
      }}
    >
      {children}
    </AdminContext.Provider>
  );
};
