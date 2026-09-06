import React from 'react';

export const ProgressBar = ({ current, total }) => {
  const percentage = total > 0 ? ((current + 1) / total) * 100 : 0;
  return (
    <div className="test-progress-bar-container">
      <div className="test-progress-bar-fill" style={{ width: `${percentage}%` }}></div>
    </div>
  );
};
