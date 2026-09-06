import React, { useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, FileText, ArrowLeft, Save, ListOrdered, PlusCircle, HelpCircle, Pencil, Trash2, X, Info, UploadCloud, Timer } from 'lucide-react';
import { useAdmin } from '../../../hooks/useAdmin';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';
import { genId } from '../../../utils/formatters';

export const TestEditorPage = () => {
  const {
    editingTestDraft,
    showQuestionModal,
    setShowQuestionModal,
    editingQuestion,
    editingQuestionIdx,
    adminSaveTest,
    updateDraftField,
    adminOpenQuestionEditor,
    adminSaveQuestion,
    adminImportQuestions,
    adminDeleteQuestion,
    updateQField,
    updateOptField,
    addOption,
    removeOption
  } = useAdmin();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const excelInputRef = useRef(null);

  if (!editingTestDraft) {
    navigate(ROUTES.ADMIN_TESTS);
    return null;
  }

  const handleSaveTest = async () => {
    const updated = await adminSaveTest();
    if (updated) {
      showToast(`"${updated.title}" saved successfully.`);
    }
  };

  const handleSaveQuestion = () => {
    try {
      adminSaveQuestion();
      showToast('Question saved. Click "Save Test" to persist.');
    } catch (err) {
      showToast(err.message);
    }
  };

  const handleDeleteQuestion = (idx) => {
    if (window.confirm('Delete this question?')) {
      adminDeleteQuestion(idx);
      showToast('Question deleted.');
    }
  };

  const parseCsvLine = (line) => {
    const columns = [];
    let cell = '';
    let insideQuotes = false;

    for (let i = 0; i < line.length; i += 1) {
      const ch = line[i];
      const next = line[i + 1];

      if (ch === '"') {
        if (insideQuotes && next === '"') {
          cell += '"';
          i += 1;
        } else {
          insideQuotes = !insideQuotes;
        }
      } else if (ch === ',' && !insideQuotes) {
        columns.push(cell);
        cell = '';
      } else {
        cell += ch;
      }
    }

    columns.push(cell);
    return columns;
  };

  const handleExcelImport = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        throw new Error('The upload flow only accepts .csv question sheets in this build.');
      }

      const text = await file.text();
      const lines = text.split(/\r?\n/).filter(line => line.trim().length > 0);
      if (lines.length < 2) {
        throw new Error('The selected file does not contain a question table.');
      }

      const headers = parseCsvLine(lines[0]).map(value => String(value || '').trim().toLowerCase());
      const rows = lines.slice(1).map(line => {
        const values = parseCsvLine(line);
        return headers.reduce((acc, header, idx) => {
          acc[header] = values[idx] || '';
          return acc;
        }, {});
      });

      const fileQuestions = rows.map((row, index) => {
        const questionText = row.question || row.question_text || row.prompt || row.text || row['question text'];
        if (!questionText || !String(questionText).trim()) {
          return null;
        }

        const correctAnswerKey = row.correctanswer || row.correct_answer || row.answer || row.correct || row['correct answer'];
        const correctAnswer = String(correctAnswerKey || 'A').trim().toUpperCase();

        const optionMap = {
          a: row.optiona || row.option_a || row.option1 || row.option_1 || row.a || row['a.'] || row['option a'],
          b: row.optionb || row.option_b || row.option2 || row.option_2 || row.b || row['b.'] || row['option b'],
          c: row.optionc || row.option_c || row.option3 || row.option_3 || row.c || row['c.'] || row['option c'],
          d: row.optiond || row.option_d || row.option4 || row.option_4 || row.d || row['d.'] || row['option d']
        };

        const options = ['A', 'B', 'C', 'D'].map((letter) => {
          const value = optionMap[letter.toLowerCase()];
          return {
            letter,
            text: value || '',
            weight: 0,
            category: ''
          };
        }).filter(option => option.text && String(option.text).trim() !== '');

        if (options.length < 2) {
          throw new Error(`Question ${index + 1} must have at least two answer options.`);
        }

        return {
          id: genId(),
          question: String(questionText).trim(),
          explanation: row.explanation || row.reason || '',
          correctAnswer: ['A', 'B', 'C', 'D'].includes(correctAnswer) ? correctAnswer : 'A',
          options
        };
      }).filter(Boolean);

      if (fileQuestions.length === 0) {
        throw new Error('No valid question rows were detected in the CSV sheet.');
      }

      adminImportQuestions(fileQuestions);
      showToast(`${fileQuestions.length} question${fileQuestions.length === 1 ? '' : 's'} imported from ${file.name}.`);
    } catch (error) {
      console.error('Unable to import assessment questions:', error);
      showToast(error.message || 'The workbook could not be imported.');
    } finally {
      event.target.value = '';
    }
  };

  const handleAddOpt = () => {
    try {
      addOption();
    } catch (err) {
      showToast(err.message);
    }
  };

  const handleRemoveOpt = (oi) => {
    try {
      removeOption(oi);
    } catch (err) {
      showToast(err.message);
    }
  };

  return (
    <main className="main-wrapper slide-up">
      {/* Breadcrumb Navigation */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '24px', fontSize: '13px', color: 'var(--color-text-muted)' }}>
        <button
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-primary-green)', fontWeight: 600, padding: 0 }}
          onClick={() => navigate(ROUTES.ADMIN_TESTS)}
        >
          Tests
        </button>
        <ChevronRight size={14} />
        <span style={{ color: 'var(--color-dark-text)', fontWeight: 600 }}>
          {editingTestDraft.title || 'New Test'}
        </span>
      </div>

      <div className="admin-editor-grid">
        {/* Test Meta Editor Card */}
        <div>
          <div className="admin-editor-card">
            <div className="admin-editor-card-header">
              <FileText size={18} color="var(--color-primary-green)" />
              <h3>Test Details</h3>
            </div>
            <div className="form-group">
              <label className="form-label">Test Title</label>
              <input
                className="form-input"
                style={{ paddingLeft: '16px' }}
                type="text"
                value={editingTestDraft.title}
                onChange={e => updateDraftField('title', e.target.value)}
                placeholder="e.g. Interests"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea
                className="form-textarea"
                rows={3}
                value={editingTestDraft.description}
                onChange={e => updateDraftField('description', e.target.value)}
                placeholder="Describe what this test measures..."
              />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Duration</label>
                <input
                  className="form-input"
                  style={{ paddingLeft: '16px' }}
                  type="text"
                  value={editingTestDraft.duration}
                  onChange={e => updateDraftField('duration', e.target.value)}
                  placeholder="e.g. 15 mins"
                />
              </div>
              <div className="form-group">
                <label className="form-label">Difficulty</label>
                <select
                  className="form-select"
                  value={editingTestDraft.difficulty}
                  onChange={e => updateDraftField('difficulty', e.target.value)}
                >
                  <option>Easy</option>
                  <option>Medium</option>
                  <option>Hard</option>
                </select>
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Assessment Type</label>
                <select
                  className="form-select"
                  value={editingTestDraft.type}
                  onChange={e => updateDraftField('type', e.target.value)}
                >
                  <option value="aptitude">Aptitude</option>
                  <option value="personality">Personality / RIASEC</option>
                  <option value="values">Values</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Default Status</label>
                <select
                  className="form-select"
                  value={editingTestDraft.status}
                  onChange={e => updateDraftField('status', e.target.value)}
                >
                  <option value="pending">Active (Pending)</option>
                  <option value="locked">Locked</option>
                </select>
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Card Accent</label>
                <select
                  className="form-select"
                  value={editingTestDraft.accentClass}
                  onChange={e => updateDraftField('accentClass', e.target.value)}
                >
                  <option value="primary-accent">Green (Primary)</option>
                  <option value="secondary-accent">Purple (Secondary)</option>
                  <option value="accent-purple">Purple Dark</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Card Icon</label>
                <select
                  className="form-select"
                  value={editingTestDraft.iconName}
                  onChange={e => updateDraftField('iconName', e.target.value)}
                >
                  <option value="BrainCircuit">Brain Circuit</option>
                  <option value="Compass">Compass</option>
                  <option value="Award">Award</option>
                </select>
              </div>
            </div>
            {/* Timer Configuration */}
            <div style={{
              marginBottom: '20px',
              padding: '16px',
              borderRadius: '14px',
              border: editingTestDraft.timerEnabled
                ? '1.5px solid var(--color-accent-yellow)'
                : '1.5px solid var(--color-border-light)',
              backgroundColor: editingTestDraft.timerEnabled ? 'rgba(225,174,37,0.05)' : '#fff'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: editingTestDraft.timerEnabled ? '14px' : 0 }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={Boolean(editingTestDraft.timerEnabled)}
                    onChange={e => updateDraftField('timerEnabled', e.target.checked)}
                    style={{ width: '16px', height: '16px', accentColor: 'var(--color-accent-yellow)', cursor: 'pointer' }}
                  />
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Timer size={16} color={editingTestDraft.timerEnabled ? 'var(--color-accent-yellow)' : 'var(--color-text-muted)'} />
                    <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--color-dark-text)' }}>Enable Countdown Timer</span>
                  </div>
                </label>
                {editingTestDraft.timerEnabled && (
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-accent-yellow)', background: 'rgba(225,174,37,0.15)', padding: '3px 8px', borderRadius: '8px' }}>
                    TIMED
                  </span>
                )}
              </div>

              {editingTestDraft.timerEnabled && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Time Limit (minutes)</label>
                    <input
                      className="form-input"
                      style={{ paddingLeft: '16px' }}
                      type="number"
                      min={1}
                      max={180}
                      value={editingTestDraft.timerMinutes ?? 10}
                      onChange={e => updateDraftField('timerMinutes', Math.max(1, Number(e.target.value)))}
                    />
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', padding: '10px', background: '#f8fafc', borderRadius: '8px', lineHeight: 1.6, alignSelf: 'end' }}>
                    <strong>Auto-submit</strong> when time runs out.<br />
                    Students see a live countdown during the test.
                  </div>
                </div>
              )}
            </div>

            <div className="admin-save-bar">
              <button className="btn btn-outline" style={{ width: 'auto', padding: '10px 20px' }} onClick={() => navigate(ROUTES.ADMIN_TESTS)}>
                <ArrowLeft size={16} /> Back
              </button>
              <label className="btn btn-outline" style={{ width: 'auto', padding: '10px 20px', cursor: 'pointer' }} htmlFor="excel-question-upload">
                <UploadCloud size={16} /> Upload Excel
              </label>
              <input
                id="excel-question-upload"
                ref={excelInputRef}
                type="file"
                accept=".csv"
                style={{ display: 'none' }}
                onChange={handleExcelImport}
              />
              <button className="btn btn-primary" style={{ width: 'auto', padding: '10px 24px' }} onClick={handleSaveTest}>
                <Save size={16} /> Save Test
              </button>
            </div>
          </div>
        </div>

        {/* Questions List Card */}
        <div>
          <div className="admin-editor-card">
            <div className="admin-editor-card-header" style={{ justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <ListOrdered size={18} color="var(--color-primary-green)" />
                <h3>
                  Questions <span style={{ fontWeight: 400, color: 'var(--color-text-subtle)', fontSize: '14px' }}>({editingTestDraft.questions.length})</span>
                </h3>
              </div>
              <button className="btn btn-primary" style={{ width: 'auto', padding: '8px 16px', fontSize: '13px' }} onClick={() => adminOpenQuestionEditor(null, null)}>
                <PlusCircle size={14} /> Add Question
              </button>
            </div>

            {editingTestDraft.questions.length === 0 ? (
              <div className="empty-questions-state">
                <HelpCircle size={40} color="var(--color-border-light)" />
                <p>No questions yet. Click "Add Question" to get started.</p>
              </div>
            ) : (
              <div className="questions-editor-list">
                {editingTestDraft.questions.map((q, idx) => (
                  <div className="question-editor-card" key={q.id || idx}>
                    <div className="question-editor-num">Q{idx + 1}</div>
                    <div className="question-editor-body">
                      <p className="question-editor-text">
                        {q.question || <em style={{ color: 'var(--color-text-subtle)' }}>No question text</em>}
                      </p>
                      <div className="question-editor-meta" style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        <span>{q.options.length} options</span>
                        {q.correctAnswer && <span>· Correct: <strong>{q.correctAnswer}</strong></span>}
                        {q.isMandatory && (
                          <span style={{ color: '#c53030', fontWeight: 700, fontSize: '11px', background: '#fff5f5', border: '1px solid #feb2b2', padding: '1px 6px', borderRadius: '10px' }}>
                            ✱ Required
                          </span>
                        )}
                        {q.isMultiSelect && (
                          <span style={{ color: 'var(--color-primary-purple)', fontWeight: 700, fontSize: '11px', background: 'rgba(114,98,171,0.08)', border: '1px solid rgba(114,98,171,0.3)', padding: '1px 6px', borderRadius: '10px' }}>
                            ☑ Multi-Select
                          </span>
                        )}
                      </div>
                      <div className="question-editor-options-preview">
                        {q.options.map(o => (
                          <span key={o.letter} className="option-preview-chip">
                            <strong>{o.letter}.</strong> {o.text.length > 28 ? o.text.slice(0, 28) + '…' : o.text || '—'}
                            {o.weight > 0 && <em> (w:{o.weight})</em>}
                            {o.category && <em> [{o.category}]</em>}
                          </span>
                        ))}
                      </div>
                    </div>
                    <div className="question-editor-actions">
                      <button className="admin-action-btn edit" onClick={() => adminOpenQuestionEditor(q, idx)}>
                        <Pencil size={13} />
                      </button>
                      <button className="admin-action-btn delete" onClick={() => handleDeleteQuestion(idx)}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* QUESTION EDITOR MODAL OVERLAY */}
      {showQuestionModal && editingQuestion && (
        <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) setShowQuestionModal(false); }}>
          <div className="modal-card slide-up">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <HelpCircle size={20} color="var(--color-primary-green)" />
                <h3>{editingQuestionIdx !== null && editingQuestionIdx !== undefined ? `Edit Question ${editingQuestionIdx + 1}` : 'Add New Question'}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowQuestionModal(false)}>
                <X size={20} />
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Question Text <span style={{ color: 'var(--color-error)' }}>*</span></label>
                <textarea
                  className="form-textarea"
                  rows={3}
                  value={editingQuestion.question}
                  onChange={e => updateQField('question', e.target.value)}
                  placeholder="Enter the question that students will see..."
                />
              </div>
              <div className="form-group">
                <label className="form-label">Explanation (shown after answer)</label>
                <textarea
                  className="form-textarea"
                  rows={2}
                  value={editingQuestion.explanation || ''}
                  onChange={e => updateQField('explanation', e.target.value)}
                  placeholder="Explain why the correct answer is right..."
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '8px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Correct Answer</label>
                  <select
                    className="form-select"
                    value={editingQuestion.correctAnswer}
                    onChange={e => updateQField('correctAnswer', e.target.value)}
                  >
                    {editingQuestion.options.map(o => <option key={o.letter} value={o.letter}>Option {o.letter}</option>)}
                  </select>
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Info size={13} color="var(--color-text-subtle)" /> Field Guide
                  </label>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', padding: '10px', background: '#f8fafc', borderRadius: '8px', lineHeight: 1.6 }}>
                    <strong>Weight:</strong> score added when chosen<br />
                    <strong>Category:</strong> RIASEC code (R/I/A/S/E/C)
                  </div>
                </div>
              </div>

              {/* Multi-Select & Mandatory Toggles */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '20px' }}>
                <label
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 14px',
                    borderRadius: '12px', border: editingQuestion.isMultiSelect ? '1.5px solid var(--color-primary-purple)' : '1.5px solid var(--color-border-light)',
                    backgroundColor: editingQuestion.isMultiSelect ? 'rgba(114,98,171,0.06)' : '#fff',
                    cursor: 'pointer', transition: 'all 0.2s'
                  }}
                >
                  <input
                    type="checkbox"
                    checked={Boolean(editingQuestion.isMultiSelect)}
                    onChange={e => updateQField('isMultiSelect', e.target.checked)}
                    style={{ width: '16px', height: '16px', accentColor: 'var(--color-primary-purple)', cursor: 'pointer' }}
                  />
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-dark-text)' }}>Multi-Select</div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Allow selecting multiple answers</div>
                  </div>
                </label>

                <label
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 14px',
                    borderRadius: '12px', border: editingQuestion.isMandatory ? '1.5px solid #e53e3e' : '1.5px solid var(--color-border-light)',
                    backgroundColor: editingQuestion.isMandatory ? '#fff5f5' : '#fff',
                    cursor: 'pointer', transition: 'all 0.2s'
                  }}
                >
                  <input
                    type="checkbox"
                    checked={Boolean(editingQuestion.isMandatory)}
                    onChange={e => updateQField('isMandatory', e.target.checked)}
                    style={{ width: '16px', height: '16px', accentColor: '#e53e3e', cursor: 'pointer' }}
                  />
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-dark-text)' }}>Required</div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Student must answer before submitting</div>
                  </div>
                </label>
              </div>

              <div style={{ marginTop: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                  <label className="form-label" style={{ margin: 0 }}>Answer Options</label>
                  <button className="btn btn-outline" style={{ width: 'auto', padding: '6px 14px', fontSize: '12px' }} onClick={handleAddOpt}>
                    <PlusCircle size={13} /> Add Option
                  </button>
                </div>
                <div className="options-editor-list">
                  {editingQuestion.options.map((opt, oi) => (
                    <div className="option-editor-row" key={oi}>
                      <div className="option-editor-letter">{opt.letter}</div>
                      <input
                        className="form-input option-editor-text"
                        style={{ paddingLeft: '12px' }}
                        type="text"
                        value={opt.text}
                        onChange={e => updateOptField(oi, 'text', e.target.value)}
                        placeholder={`Option ${opt.letter} text...`}
                      />
                      <div className="option-editor-extras">
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                          <label style={{ fontSize: '10px', color: 'var(--color-text-subtle)', fontWeight: 600, textTransform: 'uppercase' }}>Weight</label>
                          <input
                            className="form-input"
                            style={{ paddingLeft: '10px', width: '70px', textAlign: 'center' }}
                            type="number"
                            min={0}
                            max={10}
                            value={opt.weight ?? 0}
                            onChange={e => updateOptField(oi, 'weight', e.target.value)}
                          />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                          <label style={{ fontSize: '10px', color: 'var(--color-text-subtle)', fontWeight: 600, textTransform: 'uppercase' }}>Category</label>
                          <input
                            className="form-input"
                            style={{ paddingLeft: '10px', width: '70px', textAlign: 'center' }}
                            type="text"
                            value={opt.category || ''}
                            onChange={e => updateOptField(oi, 'category', e.target.value)}
                            placeholder="R/I/A…"
                            maxLength={2}
                          />
                        </div>
                      </div>
                      <button className="admin-action-btn delete" style={{ flexShrink: 0 }} onClick={() => handleRemoveOpt(oi)} title="Remove option">
                        <X size={13} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" style={{ width: 'auto', padding: '10px 20px' }} onClick={() => setShowQuestionModal(false)}>
                Cancel
              </button>
              <button className="btn btn-primary" style={{ width: 'auto', padding: '10px 24px' }} onClick={handleSaveQuestion}>
                <Save size={16} /> Save Question
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
};
