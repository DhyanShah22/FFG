import React from 'react';
import { AlertCircle, CheckSquare } from 'lucide-react';

export const QuestionCard = ({
  question,
  questionIndex,
  totalQuestions,
  selectedAnswer,
  onSelectOption,
  onPrev,
  onNext,
  onFinish
}) => {
  if (!question) return null;

  const isMultiSelect = Boolean(question.isMultiSelect);
  const isMandatory = Boolean(question.isMandatory);
  const isLastQuestion = questionIndex === totalQuestions - 1;

  // Normalise selected answer:
  // single-select → string | undefined
  // multi-select  → string[] (may be empty/undefined)
  const selectedLetters = isMultiSelect
    ? (Array.isArray(selectedAnswer) ? selectedAnswer : [])
    : selectedAnswer;

  const isOptionSelected = (letter) =>
    isMultiSelect
      ? selectedLetters.includes(letter)
      : selectedLetters === letter;

  // "Answered" for navigation/submit purposes
  const isAnswered = isMultiSelect
    ? selectedLetters.length > 0
    : Boolean(selectedLetters);

  // Non-mandatory questions can always be skipped
  const canProceed = isAnswered || !isMandatory;

  const handleOptionClick = (letter) => {
    onSelectOption(question.id, letter, isMultiSelect);
  };

  return (
    <div className="test-question-box">
      {/* Question header row */}
      <div className="question-meta" style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
        <span>Question {questionIndex + 1} of {totalQuestions}</span>

        {isMandatory && (
          <span style={{
            display: 'inline-flex', alignItems: 'center', gap: '4px',
            fontSize: '11px', fontWeight: 700, color: '#c53030',
            background: '#fff5f5', border: '1px solid #feb2b2',
            padding: '2px 8px', borderRadius: '20px'
          }}>
            <AlertCircle size={11} /> Required
          </span>
        )}

        {isMultiSelect && (
          <span style={{
            display: 'inline-flex', alignItems: 'center', gap: '4px',
            fontSize: '11px', fontWeight: 700, color: 'var(--color-primary-purple)',
            background: 'rgba(114,98,171,0.08)', border: '1px solid rgba(114,98,171,0.3)',
            padding: '2px 8px', borderRadius: '20px'
          }}>
            <CheckSquare size={11} /> Select all that apply
          </span>
        )}
      </div>

      <div className="question-text">{question.question}</div>

      <div className="options-list">
        {question.options.map(opt => {
          const isSelected = isOptionSelected(opt.letter);
          return (
            <div
              className={`option-item-card ${isSelected ? 'selected' : ''}`}
              key={opt.letter}
              onClick={() => handleOptionClick(opt.letter)}
              style={{ cursor: 'pointer' }}
            >
              {/* Checkbox or radio indicator */}
              <div
                style={{
                  width: '20px', height: '20px', flexShrink: 0,
                  borderRadius: isMultiSelect ? '4px' : '50%',
                  border: isSelected
                    ? '2px solid var(--color-primary-green)'
                    : '2px solid var(--color-border-light)',
                  backgroundColor: isSelected ? 'var(--color-primary-green)' : '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  transition: 'all 0.15s ease'
                }}
              >
                {isSelected && (
                  isMultiSelect
                    ? <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M2 6l3 3 5-5" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                    : <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#fff' }} />
                )}
              </div>

              <div className="option-letter-box">{opt.letter}</div>
              <div className="option-text-val">{opt.text}</div>
            </div>
          );
        })}
      </div>

      {/* Skip hint for non-mandatory unanswered */}
      {!isMandatory && !isAnswered && (
        <p style={{ fontSize: '12px', color: 'var(--color-text-subtle)', marginTop: '8px', textAlign: 'center' }}>
          This question is optional — you can skip it.
        </p>
      )}

      <div className="test-control-actions">
        <button
          className="btn btn-outline btn-navigation"
          onClick={onPrev}
          disabled={questionIndex === 0}
          style={{ opacity: questionIndex === 0 ? 0.5 : 1 }}
        >
          Previous
        </button>
        {isLastQuestion ? (
          <button
            className="btn btn-primary btn-navigation"
            onClick={onFinish}
            disabled={isMandatory && !isAnswered}
            style={{ opacity: (isMandatory && !isAnswered) ? 0.6 : 1 }}
          >
            Submit Test
          </button>
        ) : (
          <button
            className="btn btn-secondary btn-navigation"
            onClick={onNext}
            disabled={!canProceed}
            style={{ opacity: !canProceed ? 0.6 : 1 }}
          >
            {isAnswered ? 'Next' : 'Skip'}
          </button>
        )}
      </div>
    </div>
  );
};
