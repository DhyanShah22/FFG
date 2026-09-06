import React, { useState } from 'react';
import { Users, Search, Download, Mail } from 'lucide-react';
import { useAdmin } from '../../../hooks/useAdmin';
import { MOCK_STUDENTS } from '../../../utils/constants';

export const AdminStudentsPage = () => {
  const { adminTests } = useAdmin();
  const [searchQuery, setSearchQuery] = useState('');

  const filteredStudents = MOCK_STUDENTS.filter(s =>
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
          { label: 'Total Students', value: MOCK_STUDENTS.length, color: 'var(--color-primary-green)' },
          { label: 'Assessments Completed', value: MOCK_STUDENTS.reduce((a, s) => a + s.completedTests.length, 0), color: 'var(--color-primary-purple)' },
          { label: 'Active Centers', value: 2, color: 'var(--color-accent-yellow)' },
        ].map((stat) => (
          <div key={stat.label} style={{
            background: '#ffffff',
            border: '1.5px solid var(--color-border-light)',
            borderRadius: '14px',
            padding: '16px 24px',
            minWidth: '160px',
            boxShadow: 'var(--shadow-subtle)'
          }}>
            <div style={{ fontSize: '24px', fontWeight: 800, color: stat.color }}>{stat.value}</div>
            <div style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginTop: '2px' }}>{stat.label}</div>
          </div>
        ))}
      </div>

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
            {filteredStudents.length === 0 ? (
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
                          width: `${adminTests.length > 0 ? (s.completedTests.length / adminTests.length) * 100 : 0}%`,
                          height: '100%',
                          backgroundColor: 'var(--color-primary-green)',
                          borderRadius: '9999px'
                        }} />
                      </div>
                      <span style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
                        {s.completedTests.length} / {adminTests.length}
                      </span>
                    </div>
                  </td>
                  <td className="admin-td">
                    <span className="status-badge completed">Active</span>
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
