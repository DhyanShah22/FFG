import React, { createContext, useState, useEffect } from 'react';
import { CheckCircle2 } from 'lucide-react';

export const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toastMessage, setToastMessage] = useState(null);

  useEffect(() => {
    if (toastMessage) {
      const t = setTimeout(() => setToastMessage(null), 4000);
      return () => clearTimeout(t);
    }
  }, [toastMessage]);

  const showToast = (msg) => {
    setToastMessage(msg);
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {toastMessage && (
        <div className="toast-alert success-toast slide-up">
          <CheckCircle2 size={18} color="var(--color-primary-green)" style={{ flexShrink: 0 }} />
          <span>{toastMessage}</span>
        </div>
      )}
    </ToastContext.Provider>
  );
};
