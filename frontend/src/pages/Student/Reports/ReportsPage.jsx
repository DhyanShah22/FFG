import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, ArrowLeft, Download, RefreshCcw } from 'lucide-react';
import { useAuth } from '../../../hooks/useAuth';
import { useToast } from '../../../hooks/useToast';
import { assessmentService } from '../../../services/assessment.service';
import { ROUTES } from '../../../config/routes';

export const StudentReportsPage = () => {
  const { currentUser } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [recommendations, setRecommendations] = useState(null);
  const [reports, setReports] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadData = useCallback(async () => {
    if (!currentUser) return;

    setIsLoading(true);
    try {
      const [recData, reportData] = await Promise.all([
        assessmentService.getRecommendations(currentUser.id),
        assessmentService.getStudentReports(currentUser.id)
      ]);
      setRecommendations(recData);
      setReports(reportData || []);
    } catch (error) {
      console.error('Unable to load reports or recommendations:', error);
      showToast('Unable to load reports. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  }, [currentUser, showToast]);

  useEffect(() => {
    loadData();
  }, [currentUser, loadData]);

  const refreshRecommendations = async () => {
    if (!currentUser) return;
    setIsRefreshing(true);
    try {
      const recData = await assessmentService.generateRecommendations(currentUser.id);
      setRecommendations(recData);
      showToast('Career recommendations updated.');
    } catch (error) {
      console.error('Unable to refresh recommendations:', error);
      showToast('Could not refresh recommendations.');
    } finally {
      setIsRefreshing(false);
    }
  };

  const generateNewReport = async () => {
    if (!currentUser) return;
    setIsRefreshing(true);
    try {
      const report = await assessmentService.generateReport(currentUser.id);
      setReports(prev => [report, ...prev]);
      showToast('Career report generated successfully.');
    } catch (error) {
      console.error('Unable to generate report:', error);
      showToast('Report generation failed.');
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleDownload = (downloadUrl) => {
    window.open(downloadUrl, '_blank');
  };

  const handleBack = () => {
    navigate(ROUTES.STUDENT_DASHBOARD);
  };

  if (!currentUser) return null;

  return (
    <main className="main-wrapper slide-up">
      <div className="welcome-section">
        <div className="welcome-info" style={{ textAlign: 'left' }}>
          <h1>Career Reports & Recommendations</h1>
          <p>View your latest career guidance documents and personalized recommendations.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-outline" style={{ width: 'auto', padding: '12px 20px' }} onClick={handleBack}>
            <ArrowLeft size={16} /> Back to Dashboard
          </button>
          <button className="btn btn-primary" style={{ width: 'auto', padding: '12px 20px' }} onClick={refreshRecommendations} disabled={isRefreshing}>
            <RefreshCcw size={16} /> Refresh Recommendations
          </button>
        </div>
      </div>

      <div className="stats-grid" style={{ marginBottom: '32px' }}>
        <div className="stat-card">
          <div className="stat-icon-box coral"><FileText size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{reports.length}</div>
            <div className="stat-label">Available Reports</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon-box blue"><RefreshCcw size={24} /></div>
          <div className="stat-content">
            <div className="stat-number">{recommendations?.recommendations?.length ?? 0}</div>
            <div className="stat-label">Career Matches</div>
          </div>
        </div>
      </div>

      <div className="section-header" style={{ marginBottom: '20px' }}>
        <h3 className="section-title">Your Career Recommendations</h3>
        <button className="btn btn-primary" style={{ width: 'auto', padding: '10px 20px' }} onClick={generateNewReport} disabled={isRefreshing}>
          <FileText size={16} /> Generate New Report
        </button>
      </div>

      {isLoading ? (
        <div style={{ padding: '40px 0', textAlign: 'center' }}>Loading your recommendations and reports...</div>
      ) : (
        <div className="admin-table-card" style={{ marginBottom: '32px' }}>
          {recommendations?.recommendations?.length ? (
            recommendations.recommendations.map((item, idx) => (
              <div key={item.name || idx} className="career-match-card" style={{ marginBottom: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '16px' }}>
                  <div>
                    <h4 style={{ margin: 0 }}>{item.name}</h4>
                    <p style={{ margin: '8px 0 0', color: 'var(--color-text-muted)', fontSize: '13px' }}>{item.description}</p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span className="career-match-badge">{item.match}</span>
                    <div style={{ marginTop: '8px', color: 'var(--color-text-muted)', fontSize: '12px' }}>{item.education}</div>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
              No recommendations available yet. Complete more assessments to unlock personalized career matches.
            </div>
          )}
        </div>
      )}

      <div className="section-header" style={{ marginBottom: '20px' }}>
        <h3 className="section-title">Your Generated Reports</h3>
      </div>

      <div className="admin-table-card">
        {reports.length === 0 ? (
          <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
            No reports have been generated yet. Generate one to download your career guidance summary.
          </div>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th>Report Name</th>
                <th>Generated On</th>
                <th style={{ textAlign: 'center' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {reports.map(report => (
                <tr key={report.reportId} className="admin-tr">
                  <td className="admin-td">{report.fileName}</td>
                  <td className="admin-td">{new Date(report.generatedAt).toLocaleDateString()}</td>
                  <td className="admin-td" style={{ textAlign: 'center' }}>
                    <button className="btn btn-outline" onClick={() => handleDownload(report.downloadUrl)}>
                      <Download size={14} /> Download
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </main>
  );
};
