import React from 'react';
import { useNavigate } from 'react-router-dom';
import { PlusCircle, ToggleRight, ToggleLeft, Pencil, Trash2 } from 'lucide-react';
import { useAdmin } from '../../../hooks/useAdmin';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';

export const TestManagementPage = () => {
  const {
    adminTests,
    adminCreateNewTest,
    adminOpenTestEditor,
    adminDeleteTest,
    adminToggleStatus
  } = useAdmin();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleAddTest = async () => {
    const newTest = await adminCreateNewTest();
    showToast('New test created.');
    navigate(ROUTES.ADMIN_TEST_EDITOR.replace(':id', newTest.id));
  };

  const handleEditTest = (test) => {
    adminOpenTestEditor(test);
    navigate(ROUTES.ADMIN_TEST_EDITOR.replace(':id', test.id));
  };

  const handleDeleteTest = (id) => {
    if (window.confirm('Delete this test permanently?')) {
      adminDeleteTest(id);
      showToast('Test deleted.');
    }
  };

  const handleToggleStatus = (id) => {
    adminToggleStatus(id);
  };

  return (
    <main className="main-wrapper slide-up">
      <div className="welcome-section">
        <div className="welcome-info" style={{ textAlign: 'left' }}>
          <h1>Test Management</h1>
          <p>Configure, add, or remove assessments. Toggle status to hide/show tests from students.</p>
        </div>
        <button className="btn btn-primary" style={{ width: 'auto', padding: '12px 24px' }} onClick={handleAddTest}>
          <PlusCircle size={16} /> Add New Test
        </button>
      </div>

      <div className="admin-table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Test Name</th>
              <th>Type</th>
              <th>Difficulty</th>
              <th>Questions</th>
              <th>Duration</th>
              <th>Status</th>
              <th style={{ textAlign: 'center' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {adminTests.map((test, idx) => (
              <tr key={test.id} className="admin-tr">
                <td className="admin-td" style={{ color: 'var(--color-text-subtle)', fontSize: '13px' }}>
                  {idx + 1}
                </td>
                <td className="admin-td">
                  <div style={{ fontWeight: 700, color: 'var(--color-dark-text)' }}>{test.title}</div>
                  <div
                    style={{
                      fontSize: '12px',
                      color: 'var(--color-text-muted)',
                      maxWidth: '300px',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }}
                  >
                    {test.description}
                  </div>
                </td>
                <td className="admin-td">
                  <span className={`type-badge type-${test.type}`}>{test.type}</span>
                </td>
                <td className="admin-td">
                  <span className="difficulty-badge">{test.difficulty}</span>
                </td>
                <td className="admin-td" style={{ fontWeight: 600 }}>{test.questions.length}</td>
                <td className="admin-td" style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
                  {test.duration}
                </td>
                <td className="admin-td">
                  <button className="toggle-status-btn" onClick={() => handleToggleStatus(test.id)}>
                    {test.status === 'pending' ? (
                      <>
                        <ToggleRight size={20} color="var(--color-primary-green)" />
                        <span style={{ color: 'var(--color-primary-green)' }}>Active</span>
                      </>
                    ) : (
                      <>
                        <ToggleLeft size={20} color="var(--color-text-subtle)" />
                        <span style={{ color: 'var(--color-text-subtle)' }}>Locked</span>
                      </>
                    )}
                  </button>
                </td>
                <td className="admin-td" style={{ textAlign: 'center' }}>
                  <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                    <button className="admin-action-btn edit" onClick={() => handleEditTest(test)} title="Edit test">
                      <Pencil size={14} />
                    </button>
                    <button className="admin-action-btn delete" onClick={() => handleDeleteTest(test.id)} title="Delete test">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
};
