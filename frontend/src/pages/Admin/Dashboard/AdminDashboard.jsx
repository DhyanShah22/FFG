import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, HelpCircle, Users, CheckCircle2, Settings, PlusCircle, ChevronRight, Pencil } from 'lucide-react';
import { useAuth } from '../../../hooks/useAuth';
import { useAdmin } from '../../../hooks/useAdmin';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';
import { MOCK_STUDENTS } from '../../../utils/constants';

export const AdminDashboard = () => {
  const { currentUser } = useAuth();
  const {
    adminTests,
    setAdminActiveTab,
    adminCreateNewTest,
    adminOpenTestEditor,
    dashboardStats
  } = useAdmin();
  const { showToast } = useToast();
  const navigate = useNavigate();

  if (!currentUser || currentUser.role !== 'admin') return null;

  const totalAdminQ = adminTests.reduce((sum, t) => sum + (t.questions ? t.questions.length : 0), 0);

  const handleCreateTest = async () => {
    const newTest = await adminCreateNewTest();
    showToast('New test created.');
    navigate(ROUTES.ADMIN_TEST_EDITOR.replace(':id', newTest.id));
  };

  const handleEditTest = (test) => {
    adminOpenTestEditor(test);
    navigate(ROUTES.ADMIN_TEST_EDITOR.replace(':id', test.id));
  };

  const handleGoToTests = () => {
    setAdminActiveTab('tests');
    navigate(ROUTES.ADMIN_TESTS);
  };

  return (
    <main className="main-wrapper slide-up">
      {/* Admin Welcome */}
      <div className="welcome-section">
        <div className="welcome-info" style={{ textAlign: 'left' }}>
          <h1>👋 Welcome, {currentUser.name ? currentUser.name.split(' ')[0] : 'Admin'}!</h1>
          <p>Manage assessments, questions, and student progress from this panel.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-primary" style={{ width: 'auto', padding: '12px 20px' }} onClick={handleGoToTests}>
            <Settings size={16} /> Manage Tests
          </button>
          <button className="btn btn-outline" style={{ width: 'auto', padding: '12px 20px' }} onClick={handleCreateTest}>
            <PlusCircle size={16} /> New Test
          </button>
        </div>
      </div>

      {/* Stats Summary */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        <div className="stat-card">
          <div className="stat-icon-box coral"><FileText size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{dashboardStats?.totalAssessments ?? adminTests.length}</div>
            <div className="stat-label">Total Tests</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box blue"><HelpCircle size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{totalAdminQ}</div>
            <div className="stat-label">Total Questions</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box emerald"><Users size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{dashboardStats?.totalStudents ?? MOCK_STUDENTS.length}</div>
            <div className="stat-label">Registered Students</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box" style={{ background: 'rgba(225,174,37,0.1)', color: 'var(--color-accent-yellow)' }}>
            <CheckCircle2 size={24} />
          </div>
          <div className="stat-content">
            <div className="stat-number">{adminTests.filter(t => t.status === 'pending').length}</div>
            <div className="stat-label">Active Tests</div>
          </div>
        </div>
      </div>

      {/* Tests Overview Table */}
      <div className="section-header">
        <h3 className="section-title">Tests Overview</h3>
        <button className="btn btn-outline" style={{ width: 'auto', padding: '8px 16px', fontSize: '13px' }} onClick={handleGoToTests}>
          View All <ChevronRight size={14} />
        </button>
      </div>

      <div className="admin-table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Test Name</th>
              <th>Type</th>
              <th>Questions</th>
              <th>Duration</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {adminTests.map(test => (
              <tr key={test.id} className="admin-tr">
                <td className="admin-td"><strong>{test.title}</strong></td>
                <td className="admin-td">
                  <span className={`type-badge type-${test.type}`}>{test.type}</span>
                </td>
                <td className="admin-td">{test.questions.length}</td>
                <td className="admin-td">{test.duration}</td>
                <td className="admin-td">
                  <span className={`status-badge ${test.status === 'pending' ? 'completed' : 'locked'}`}>
                    {test.status === 'pending' ? 'Active' : 'Locked'}
                  </span>
                </td>
                <td className="admin-td">
                  <button className="admin-action-btn edit" onClick={() => handleEditTest(test)} title="Edit">
                    <Pencil size={14} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Registered Students Table */}
      <div className="section-header" style={{ marginTop: '40px' }}>
        <h3 className="section-title">Registered Students</h3>
      </div>
      <div className="admin-table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Username</th>
              <th>Center</th>
              <th>Student ID</th>
              <th>Tests Completed</th>
            </tr>
          </thead>
          <tbody>
            {MOCK_STUDENTS.map(s => (
              <tr key={s.id} className="admin-tr">
                <td className="admin-td">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div className="avatar" style={{ width: '32px', height: '32px', fontSize: '12px' }}>
                      {s.name.split(' ').map(n => n[0]).join('')}
                    </div>
                    <strong>{s.name}</strong>
                  </div>
                </td>
                <td className="admin-td">
                  <code style={{ background: '#f1f5f9', padding: '2px 6px', borderRadius: '4px', fontSize: '12px' }}>
                    {s.username}
                  </code>
                </td>
                <td className="admin-td" style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
                  {s.branch}
                </td>
                <td className="admin-td">
                  <code style={{ fontSize: '12px' }}>{s.id}</code>
                </td>
                <td className="admin-td">
                  {s.completedTests.length} / {adminTests.length}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
};
