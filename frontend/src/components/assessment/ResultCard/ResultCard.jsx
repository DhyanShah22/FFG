import React from 'react';
import { TrendingUp, GraduationCap, Briefcase, Printer } from 'lucide-react';
import { getTopHollandCode, getMatchingCareers } from '../../../utils/helpers';
import { useToast } from '../../../hooks/useToast';

export const ResultCard = ({ riasecScores, onBack }) => {
  const { showToast } = useToast();
  const hollandCode = getTopHollandCode(riasecScores);
  const matchingCareers = getMatchingCareers(riasecScores);

  const hollandCategories = [
    ['R', 'Realistic'],
    ['I', 'Investigative'],
    ['A', 'Artistic'],
    ['S', 'Social'],
    ['E', 'Enterprising'],
    ['C', 'Conventional']
  ];

  return (
    <div className="slide-up">
      <div className="results-header-card">
        <div className="results-header-info">
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{
              background: 'var(--color-primary-green)',
              color: 'white',
              padding: '4px 10px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Holland code: {hollandCode}
            </span>
            <span style={{ opacity: 0.8, fontSize: '13px' }}>Assessment Profile</span>
          </div>
          <h1>Your Psychometric Assessment Report</h1>
          <p>
            Below is the detailed visual index of your vocational interest profiles (RIASEC) and custom career suggestions mapped out by our algorithms.
          </p>
        </div>
        <div className="results-action-btn-group">
          <button className="btn btn-outline" onClick={() => window.print()} style={{ color: 'var(--color-dark-text)', display: 'flex', gap: '8px', background: 'white' }}>
            <Printer size={18} />
            <span>Print Report</span>
          </button>
          <button className="btn btn-primary" onClick={onBack} style={{ width: 'auto' }}>
            Back to Dashboard
          </button>
        </div>
      </div>

      <div className="results-body-grid">
        <div>
          <div className="results-card">
            <h3 className="results-card-title">
              <TrendingUp size={22} color="var(--color-primary-green)" />
              <span>Interest Profile Analysis (RIASEC Model)</span>
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '24px', lineHeight: 1.6 }}>
              The RIASEC model breaks careers down into six core groups. Your answers demonstrate which quadrants match your style the closest:
            </p>
            <div className="holland-chart-container">
              {hollandCategories.map(([code, label]) => (
                <div className="holland-bar-row" key={code}>
                  <span className="holland-bar-label">{label} ({code})</span>
                  <div className="holland-bar-track">
                    <div
                      className={`holland-bar-fill color-${label.toLowerCase()}`}
                      style={{ width: `${riasecScores[code]}%` }}
                    >
                      {riasecScores[code]}%
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="riasec-description-row">
              <div className="riasec-desc-card">
                <h4>🥇 Primary Strength: Social &amp; Supportive</h4>
                <p className="riasec-desc-text">
                  You enjoy working with people, helping others grow, and building partnerships. Highly effective for counseling, education, and development.
                </p>
              </div>
              <div className="riasec-desc-card">
                <h4>🥈 Secondary Strength: Investigative</h4>
                <p className="riasec-desc-text">
                  You enjoy researching, finding answer keys to logical puzzles, and resolving problems through science or data analysis.
                </p>
              </div>
            </div>
          </div>

          <div className="results-card">
            <h3 className="results-card-title">
              <GraduationCap size={22} color="var(--color-primary-green)" />
              <span>Counselor Action Recommendations &amp; Guidance</span>
            </h3>
            <ul className="advice-bullet-list">
              <li className="advice-bullet-item">
                <strong>Vocational Focus:</strong> Leverage your outstanding combination of <strong>Social</strong> &amp; <strong>Investigative</strong> capabilities.
              </li>
              <li className="advice-bullet-item">
                <strong>NGO Training Support:</strong> Antarang advises enrolling in our <strong>Career Ready program</strong> to practice visual layout design, communication, or basic software coding.
              </li>
              <li className="advice-bullet-item">
                <strong>Action Steps:</strong> Attend the scheduled Friday coaching with your assigned counselor to practice mock interviews and select relevant college courses.
              </li>
            </ul>
          </div>
        </div>

        <div>
          <div className="results-card">
            <h3 className="results-card-title">
              <Briefcase size={22} color="var(--color-primary-green)" />
              <span>Career Path Recommendations</span>
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginBottom: '20px' }}>
              These matches align closely with your {hollandCode} profile attributes:
            </p>
            <div className="career-match-card-list">
              {matchingCareers.map((career, idx) => (
                <div className="career-match-card" key={idx}>
                  <div className="career-match-info">
                    <h4 className="career-match-title" style={{ color: 'var(--color-primary-purple)' }}>{career.name}</h4>
                    <div className="career-match-meta" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                      <span>Study path:</span>
                      <strong>{career.education}</strong>
                    </div>
                    <p style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '8px', lineHeight: 1.5 }}>
                      {career.description}
                    </p>
                  </div>
                  <div className="career-match-badge">{career.match}</div>
                </div>
              ))}
            </div>
            <button
              className="btn btn-outline"
              style={{ marginTop: '24px', display: 'flex', gap: '8px', borderStyle: 'dashed', width: '100%', justifyContent: 'center' }}
              onClick={() => showToast('Information request sent to your Center Coordinator!')}
            >
              <Briefcase size={16} />
              <span>Explore more career options</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
