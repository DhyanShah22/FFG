import { careerDatabase, fallbackCareers } from '../data/assessments';

export const getTopHollandCode = (riasecScores) => {
  if (!riasecScores) return 'SCI';
  return Object.keys(riasecScores)
    .sort((a, b) => riasecScores[b] - riasecScores[a])
    .slice(0, 3)
    .join('');
};

export const getMatchingCareers = (riasecScores) => {
  const code = getTopHollandCode(riasecScores);
  const match = careerDatabase.find(
    c => [...c.code].filter(ch => code.includes(ch)).length >= 2
  );
  return match ? match.careers : fallbackCareers;
};

export const calculateAssessmentResults = (activeAssessment, selectedAnswers, currentScores) => {
  let newScores = { ...currentScores };

  if (activeAssessment.type === 'personality') {
    const scores = { R: 25, I: 25, A: 25, S: 25, E: 25, C: 25 };
    activeAssessment.questions.forEach(q => {
      const ans = selectedAnswers[q.id];
      const selectedLetters = Array.isArray(ans) ? ans : (ans ? [ans] : []);
      selectedLetters.forEach(letter => {
        const selectedOption = q.options.find(op => op.letter === letter);
        if (selectedOption && selectedOption.category) {
          scores[selectedOption.category] += 12;
        }
      });
    });
    newScores = scores;
  } else if (activeAssessment.type === 'aptitude') {
    let correctCount = 0;
    activeAssessment.questions.forEach(q => {
      const ans = selectedAnswers[q.id];
      const selectedLetters = Array.isArray(ans) ? ans : (ans ? [ans] : []);
      if (selectedLetters.includes(q.correctAnswer)) {
        correctCount += 20;
      }
    });
    newScores = {
      ...currentScores,
      I: Math.min(currentScores.I + correctCount / 4, 95),
      C: Math.min(currentScores.C + correctCount / 4, 95)
    };
  }

  return newScores;
};
