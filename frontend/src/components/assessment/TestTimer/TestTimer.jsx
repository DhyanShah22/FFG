import React, { useEffect, useState, useCallback } from 'react';
import { Timer, AlertTriangle } from 'lucide-react';

export const TestTimer = ({ totalSeconds, onExpire }) => {
  const [secondsLeft, setSecondsLeft] = useState(totalSeconds);
  const [hasExpired, setHasExpired] = useState(false);

  const handleExpire = useCallback(() => {
    if (!hasExpired) {
      setHasExpired(true);
      if (onExpire) onExpire();
    }
  }, [hasExpired, onExpire]);

  useEffect(() => {
    if (secondsLeft <= 0) {
      handleExpire();
      return;
    }
    const tick = setInterval(() => {
      setSecondsLeft(prev => {
        if (prev <= 1) {
          clearInterval(tick);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(tick);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (secondsLeft === 0) handleExpire();
  }, [secondsLeft, handleExpire]);

  const minutes = Math.floor(secondsLeft / 60);
  const seconds = secondsLeft % 60;
  const timeStr = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

  const isWarning = secondsLeft <= 60 && secondsLeft > 0;
  const isCritical = secondsLeft <= 30 && secondsLeft > 0;
  const isExpired = secondsLeft === 0;

  // Circular SVG progress ring (compact size)
  const radius = 15;
  const circumference = 2 * Math.PI * radius;
  const progress = totalSeconds > 0 ? secondsLeft / totalSeconds : 0;
  const dashOffset = circumference * (1 - progress);

  const ringColor = isExpired
    ? '#e53e3e'
    : isCritical
    ? '#e53e3e'
    : isWarning
    ? '#d69e2e'
    : 'var(--color-primary-green)';

  const bgColor = isExpired
    ? '#fff5f5'
    : isCritical
    ? '#fff5f5'
    : isWarning
    ? '#fffbeb'
    : 'rgba(80,167,113,0.06)';

  const borderColor = isExpired
    ? '#feb2b2'
    : isCritical
    ? '#feb2b2'
    : isWarning
    ? '#fbd38d'
    : 'rgba(80,167,113,0.3)';

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        padding: '5px 12px',
        borderRadius: '30px',
        backgroundColor: bgColor,
        border: `1.5px solid ${borderColor}`,
        transition: 'all 0.3s ease',
        animation: isCritical && !isExpired ? 'pulse-warning 1s ease-in-out infinite' : 'none'
      }}
    >
      {/* SVG Ring */}
      <div style={{ position: 'relative', width: '36px', height: '36px', flexShrink: 0 }}>
        <svg width="36" height="36" viewBox="0 0 36 36" style={{ transform: 'rotate(-90deg)' }}>
          {/* Background track */}
          <circle
            cx="18"
            cy="18"
            r={radius}
            fill="none"
            stroke="rgba(0,0,0,0.06)"
            strokeWidth="3"
          />
          {/* Progress arc */}
          <circle
            cx="18"
            cy="18"
            r={radius}
            fill="none"
            stroke={ringColor}
            strokeWidth="3"
            strokeDasharray={circumference}
            strokeDashoffset={dashOffset}
            strokeLinecap="round"
            style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.3s ease' }}
          />
        </svg>

        {/* Center icon */}
        <div style={{
          position: 'absolute', inset: 0,
          display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
          {isCritical || isExpired
            ? <AlertTriangle size={13} color={ringColor} />
            : <Timer size={13} color={ringColor} />
          }
        </div>
      </div>

      {/* Time display */}
      <div style={{ textAlign: 'left' }}>
        <div style={{
          fontSize: '15px',
          fontWeight: 800,
          fontFamily: 'monospace',
          color: ringColor,
          lineHeight: 1,
          letterSpacing: '0.5px'
        }}>
          {isExpired ? '00:00' : timeStr}
        </div>
        <div style={{ fontSize: '9px', fontWeight: 700, color: ringColor, opacity: 0.8, textTransform: 'uppercase', letterSpacing: '0.5px', marginTop: '1px' }}>
          {isExpired ? 'Time Up' : isCritical ? 'Hurry!' : isWarning ? 'Last Min' : 'Time Left'}
        </div>
      </div>

      {/* Keyframes injected inline for pulse animation */}
      <style>{`
        @keyframes pulse-warning {
          0%, 100% { box-shadow: 0 0 0 0 rgba(229, 62, 62, 0.25); }
          50% { box-shadow: 0 0 0 6px rgba(229, 62, 62, 0); }
        }
      `}</style>
    </div>
  );
};
