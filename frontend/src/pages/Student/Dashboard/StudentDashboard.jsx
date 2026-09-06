import React from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, BookOpen, Hourglass, UserCheck, BrainCircuit, Compass, Award, ArrowRight, Lock } from 'lucide-react';
import { useAuth } from '../../../hooks/useAuth';
import { useAssessment } from '../../../hooks/useAssessment';
import { useToast } from '../../../hooks/useToast';
import { ROUTES } from '../../../config/routes';

export const StudentDashboard = () => {
  const { currentUser } = useAuth();
  const { studentAssessments, startAssessment } = useAssessment();
  const { showToast } = useToast();
  const navigate = useNavigate();

  if (!currentUser) return null;

  const completedCount = studentAssessments.filter(t => t.status === 'completed').length;

  const handleStartTest = async (assessment) => {
    try {
      await startAssessment(assessment);
      showToast(`Started ${assessment.title}`);
      navigate(ROUTES.ATTEMPT_ASSESSMENT.replace(':id', assessment.id));
    } catch (error) {
      showToast('Unable to start assessment. Please try again.');
    }
  };

  const handleViewResults = () => {
    navigate(ROUTES.RESULTS);
  };

  const handleViewReports = () => {
    navigate(ROUTES.STUDENT_REPORTS);
  };

  return (
    <main className="main-wrapper slide-up">
      {/* Welcome Banner */}
      <div className="welcome-section">
        <div className="welcome-info" style={{ textAlign: 'left' }}>
          <h1>Namaste, {currentUser.name}!</h1>
          <p>Welcome to your career portal. Find your path, build your goals.</p>
        </div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            background: 'white',
            padding: '10px 18px',
            borderRadius: '12px',
            border: '1.5px solid var(--color-border-light)',
            boxShadow: 'var(--shadow-subtle)'
          }}
        >
          <MapPin size={18} color="var(--color-primary-green)" />
          <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--color-dark-text)' }}>
            Center ID: {currentUser.id}
          </span>
        </div>
      </div>

      {/* Counseling Banner */}
      <div className="ngo-banner">
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left' }}>
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              background: '#fff9f0',
              border: '1.5px solid rgba(80,167,113,0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <Calendar size={22} color="var(--color-primary-green)" />
          </div>
          <div>
            <h5 className="ngo-banner-title">Upcoming Counseling Session</h5>
            <p className="ngo-banner-text">
              1-on-1 Review with Career Coach at <strong>4:00 PM Friday</strong> at {currentUser.branch ? currentUser.branch.split('-')[0] : 'Center'}
            </p>
          </div>
        </div>
        <div className="ngo-counselor-tag">Mentor Assigned</div>
      </div>

      {/* Stats Cards Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon-box coral"><BookOpen size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{completedCount} / 3</div>
            <div className="stat-label">Assessments Finished</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box blue"><Hourglass size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{3 - completedCount}</div>
            <div className="stat-label">Remaining Surveys</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box emerald"><UserCheck size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">Active</div>
            <div className="stat-label">Path Guidance Status</div>
          </div>
        </div>
      </div>

      {/* Assessments List */}
      <div className="section-header">
        <h3 className="section-title">Your Career Assessments</h3>
        <span style={{ fontSize: '13px', color: 'var(--color-text-muted)', fontWeight: 500 }}>
          Completed tests unlock counseling reports
        </span>
      </div>

      <div className="assessments-grid">
        {studentAssessments.map(assessment => {
          const Icon = assessment.iconName === 'BrainCircuit' ? BrainCircuit : assessment.iconName === 'Compass' ? Compass : Award;
          const isCompleted = assessment.status === 'completed';

          return (
            <div className="assessment-item-card" key={assessment.id}>
              <div className={`card-accent-strip ${assessment.accentClass}`}></div>
              <div className="card-padding">
                <div className="card-header-row">
                  <span className="assessment-duration" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Icon size={16} style={{ color: 'var(--color-primary-green)' }} />
                    <span>{assessment.duration}</span>
                  </span>
                  {isCompleted ? (
                    <span className="status-badge completed">Completed</span>
                  ) : assessment.status === 'pending' ? (
                    <span className="status-badge pending">Not Started</span>
                  ) : (
                    <span className="status-badge locked">Locked</span>
                  )}
                </div>
                <h3 className="assessment-title" style={{ color: 'var(--color-dark-text)' }}>
                  {assessment.title}
                </h3>
                <p className="assessment-description">{assessment.description}</p>
                <div className="card-footer-row">
                  <span className="questions-count">
                    {assessment.questionsCount ? `${assessment.questionsCount} Multiple Choice` : 'Survey Form'}
                  </span>
                  {isCompleted && assessment.id !== 'work_values' ? (
                    <button className="btn btn-outline btn-card-action" onClick={handleViewResults}>
                      View Results
                    </button>
                  ) : isCompleted ? (
                    <button className="btn btn-outline btn-card-action" disabled style={{ opacity: 0.6, cursor: 'not-allowed' }}>
                      Preloaded data
                    </button>
                  ) : (
                    <button className="btn btn-primary btn-card-action" onClick={() => handleStartTest(assessment)}>
                      Start Test <ArrowRight size={14} />
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Career Pathway Matcher Banner */}
      <div className="section-header"><h3 className="section-title">Career Pathway Matcher</h3></div>
      {completedCount >= 2 ? (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr auto',
            gap: '30px',
            alignItems: 'center',
            background: 'linear-gradient(135deg, #edf7f2 0%, #ffffff 100%)',
            border: '1.5px solid rgba(80,167,113,0.2)',
            borderRadius: '16px',
            padding: '30px',
            textAlign: 'left',
            marginBottom: '40px'
          }}
          className="slide-up"
        >
          <div>
            <h3 style={{ fontSize: '20px', color: 'var(--color-dark-text)', marginBottom: '8px' }}>
              🚀 Personalized Career Recommendations Ready!
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', lineHeight: 1.6, maxWidth: '800px' }}>
              Based on your completed Interests and Aptitude profiles, our system has customized a list of career profiles that fit your personality best.
            </p>
          </div>
          <div>
            <button className="btn btn-primary" onClick={handleViewReports} style={{ width: 'auto', padding: '14px 28px' }}>
              Go to Reports &amp; Careers <ArrowRight size={16} />
            </button>
          </div>
        </div>
      ) : (
        <div
          style={{
            background: '#ffffff',
            border: '2px dashed var(--color-border-light)',
            borderRadius: '16px',
            padding: '40px 30px',
            textAlign: 'center',
            marginBottom: '40px'
          }}
        >
          <Lock size={32} color="var(--color-text-subtle)" style={{ marginBottom: '16px' }} />
          <h3 style={{ fontSize: '18px', color: 'var(--color-dark-text)', marginBottom: '8px' }}>
            Recommendations Currently Locked
          </h3>
          <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', maxWidth: '500px', margin: '0 auto 16px' }}>
            Complete the remaining assessments (<strong>Aptitude</strong> or <strong>Interests</strong>) to unlock your custom careers path guidelines.
          </p>
          <button
            className="btn btn-primary"
            style={{ width: 'auto' }}
            onClick={() => {
              const pendingAssessment = studentAssessments.find(t => t.status === 'pending');
              if (pendingAssessment) handleStartTest(pendingAssessment);
            }}
          >
            Start Remaining Assessment
          </button>
        </div>
      )}
    </main>
  );
};
