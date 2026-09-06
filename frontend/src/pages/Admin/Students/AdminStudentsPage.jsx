import React, { useCallback, useEffect, useState } from 'react';
import { Users, Search, Download, Mail } from 'lucide-react';
import { useAdmin } from '../../../hooks/useAdmin';
import { useToast } from '../../../hooks/useToast';
import { userService } from '../../../services/user.service';
import { MOCK_STUDENTS } from '../../../utils/constants';

// The backend returns different field names depending on whether a record came
// from the real API (profileType/firstName/lastName) or the mock client
// (userType/name/branch). Normalize both shapes into one display-friendly record.
const normalizeStudent = (u) => ({
  id: u.id,
  name: u.name || `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.username || u.email || 'Unnamed',
  email: u.email || '',
  username: u.username || (u.email ? u.email.split('@')[0] : ''),
  branch: u.branch || u.primaryOrgUnitId || '—',
  status: u.status || 'ACTIVE',
  completedTests: u.completedTests || []
});

const isStudent = (u) => u.profileType === 'CAREER_EXPLORER' || u.userType === 'STUDENT';

export const AdminStudentsPage = () => {
  const { adminTests } = useAdmin();
  const { showToast } = useToast();
  const [searchQuery, setSearchQuery] = useState('');
  const [students, setStudents] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState(null);

  const loadStudents = useCallback(async () => {
    setIsLoading(true);
    setLoadError(null);
    try {
      const page = await userService.getUsers({ page: 0, size: 200 });
      const content = page?.content || page || [];
      setStudents(content.filter(isStudent).map(normalizeStudent));
    } catch (error) {
      console.error('Unable to load students:', error);
      setLoadError('Unable to load students. Showing sample data instead.');
      setStudents(MOCK_STUDENTS.map(normalizeStudent));
      showToast('Unable to load students from the server.');
    } finally {
      setIsLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    loadStudents();
  }, [loadStudents]);

  const filteredStudents = students.filter(s =>
    s.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    s.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (s.branch || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <main className="main-wrapper slide-up">
      {/* Page Header */}
      <div className="welcome-section">
        <div className="welcome-info" style={{ textAlign: 'left' }}>
          <h1 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Users size={28} color="var(--color-primary-green)" /> Students
          </h1>
          <p>View and manage all registered Career Explorers on the platform.</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-outline" style={{ width: 'auto', padding: '10px 18px', fontSize: '13px' }}>
            <Download size={15} /> Export CSV
          </button>
          <button className="btn btn-primary" style={{ width: 'auto', padding: '10px 18px', fontSize: '13px' }}>
            <Mail size={15} /> Invite Students
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div style={{ position: 'relative', marginBottom: '24px', maxWidth: '420px' }}>
        <Search size={16} color="var(--color-text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }} />
        <input
          type="text"
          className="form-input"
          placeholder="Search by name, username, or center..."
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          style={{ paddingLeft: '40px' }}
        />
      </div>

      {/* Stats Strip */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '28px', flexWrap: 'wrap' }}>
        {[
          { label: 'Total Students', value: students.length, color: 'var(--color-primary-green)' },
          { label: 'Active Students', value: students.filter(s => s.status === 'ACTIVE').length, color: 'var(--color-primary-purple)' },
          { label: 'Assessments Completed', value: students.reduce((a, s) => a + (s.completedTests?.length || 0), 0), color: 'var(--color-accent-yellow)' },
        ].map((stat) => (
          <div key={stat.label} style={{
            background: '#ffffff',
            border: '1.5px solid var(--color-border-light)',
            borderRadius: '14px',
            padding: '16px 24px',
            minWidth: '160px',
            boxShadow: 'var(--shadow-subtle)'
          }}>
            <div style={{ fontSize: '24px', fontWeight: 800, color: stat.color }}>{isLoading ? '…' : stat.value}</div>
            <div style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginTop: '2px' }}>{stat.label}</div>
          </div>
        ))}
      </div>

      {loadError && (
        <div style={{
          marginBottom: '20px',
          padding: '12px 16px',
          borderRadius: '10px',
          background: 'rgba(225,174,37,0.1)',
          color: 'var(--color-accent-yellow)',
          fontSize: '13px'
        }}>
          {loadError}
        </div>
      )}

      {/* Students Table */}
      <div className="admin-table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Username</th>
              <th>Center / Branch</th>
              <th>Student ID</th>
              <th>Tests Completed</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-subtle)' }}>
                  Loading students...
                </td>
              </tr>
            ) : filteredStudents.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-subtle)' }}>
                  No students found matching "{searchQuery}"
                </td>
              </tr>
            ) : (
              filteredStudents.map(s => (
                <tr key={s.id} className="admin-tr">
                  <td className="admin-td">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <div className="avatar" style={{ width: '36px', height: '36px', fontSize: '13px', flexShrink: 0 }}>
                        {s.name.split(' ').map(n => n[0]).join('').slice(0, 2)}
                      </div>
                      <div>
                        <strong style={{ display: 'block', fontSize: '14px' }}>{s.name}</strong>
                        <span style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>{s.email || `${s.username}@example.com`}</span>
                      </div>
                    </div>
                  </td>
                  <td className="admin-td">
                    <code style={{ background: '#f1f5f9', padding: '3px 8px', borderRadius: '6px', fontSize: '12px' }}>
                      {s.username}
                    </code>
                  </td>
                  <td className="admin-td" style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
                    {s.branch}
                  </td>
                  <td className="admin-td">
                    <code style={{ fontSize: '12px', color: 'var(--color-primary-purple)' }}>{s.id}</code>
                  </td>
                  <td className="admin-td">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div style={{
                        width: '80px',
                        height: '6px',
                        backgroundColor: 'var(--color-border-light)',
                        borderRadius: '9999px',
                        overflow: 'hidden'
                      }}>
                        <div style={{
                          width: `${adminTests.length > 0 ? ((s.completedTests?.length ?? 0) / adminTests.length) * 100 : 0}%`,
                          height: '100%',
                          backgroundColor: 'var(--color-primary-green)',
                          borderRadius: '9999px'
                        }} />
                      </div>
                      <span style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
                        {s.completedTests?.length ?? 0} / {adminTests.length}
                      </span>
                    </div>
                  </td>
                  <td className="admin-td">
                    <span className={`status-badge ${s.status === 'ACTIVE' ? 'completed' : 'locked'}`}>
                      {s.status === 'ACTIVE' ? 'Active' : s.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
};
