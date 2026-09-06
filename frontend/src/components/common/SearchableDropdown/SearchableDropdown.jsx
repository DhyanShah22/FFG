import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Search, X } from 'lucide-react';

export const SearchableDropdown = ({
  options = [],
  value = '',
  onChange,
  placeholder = 'Select an option...',
  label = '',
  required = false,
  error = ''
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const containerRef = useRef(null);

  const selectedOption = options.find(
    opt => (typeof opt === 'object' ? opt.value : opt) === value
  );

  const getOptionLabel = (opt) => {
    if (!opt) return '';
    return typeof opt === 'object' ? opt.label : opt;
  };

  const getOptionValue = (opt) => {
    if (!opt) return '';
    return typeof opt === 'object' ? opt.value : opt;
  };

  const filteredOptions = options.filter(opt =>
    getOptionLabel(opt).toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (opt) => {
    onChange(getOptionValue(opt));
    setIsOpen(false);
    setSearchQuery('');
  };

  return (
    <div className="form-group" ref={containerRef} style={{ position: 'relative' }}>
      {label && (
        <label className="form-label">
          {label} {required && <span style={{ color: 'var(--color-error)' }}>*</span>}
        </label>
      )}

      <div
        className={`form-input custom-dropdown-trigger ${error ? 'has-error' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          cursor: 'pointer',
          userSelect: 'none',
          backgroundColor: '#ffffff'
        }}
      >
        <span style={{ color: value ? 'var(--color-dark-text)' : 'var(--color-text-subtle)' }}>
          {selectedOption ? getOptionLabel(selectedOption) : placeholder}
        </span>
        <ChevronDown size={18} color="var(--color-text-muted)" style={{ transform: isOpen ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />
      </div>

      {isOpen && (
        <div
          className="slide-up"
          style={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            zIndex: 100,
            marginTop: '6px',
            backgroundColor: '#ffffff',
            borderRadius: '12px',
            border: '1.5px solid var(--color-border-light)',
            boxShadow: 'var(--shadow-card)',
            maxHeight: '240px',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden'
          }}
        >
          <div style={{ padding: '8px 12px', borderBottom: '1px solid var(--color-border-light)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Search size={14} color="var(--color-text-subtle)" />
            <input
              type="text"
              placeholder="Search..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onClick={e => e.stopPropagation()}
              autoFocus
              style={{
                border: 'none',
                outline: 'none',
                width: '100%',
                fontSize: '13px',
                fontFamily: 'var(--font-family)'
              }}
            />
            {searchQuery && (
              <X size={14} color="var(--color-text-subtle)" style={{ cursor: 'pointer' }} onClick={() => setSearchQuery('')} />
            )}
          </div>

          <div style={{ overflowY: 'auto', flex: 1, padding: '4px 0' }}>
            {filteredOptions.length === 0 ? (
              <div style={{ padding: '12px', fontSize: '13px', color: 'var(--color-text-muted)', textAlign: 'center' }}>
                No options found
              </div>
            ) : (
              filteredOptions.map((opt, idx) => {
                const optVal = getOptionValue(opt);
                const optLabel = getOptionLabel(opt);
                const isSelected = optVal === value;

                return (
                  <div
                    key={optVal || idx}
                    onClick={() => handleSelect(opt)}
                    style={{
                      padding: '10px 16px',
                      fontSize: '14px',
                      cursor: 'pointer',
                      backgroundColor: isSelected ? 'rgba(80, 167, 113, 0.1)' : 'transparent',
                      color: isSelected ? 'var(--color-primary-green)' : 'var(--color-dark-text)',
                      fontWeight: isSelected ? 600 : 400,
                      transition: 'background-color 0.15s ease'
                    }}
                    onMouseEnter={e => { if (!isSelected) e.currentTarget.style.backgroundColor = '#f7faf8'; }}
                    onMouseLeave={e => { if (!isSelected) e.currentTarget.style.backgroundColor = 'transparent'; }}
                  >
                    {optLabel}
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}

      {error && <span className="field-error-text" style={{ color: 'var(--color-error)', fontSize: '12px', marginTop: '4px', display: 'block' }}>{error}</span>}
    </div>
  );
};
