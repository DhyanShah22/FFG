export const deepClone = (data) => JSON.parse(JSON.stringify(data));

export const genId = () => 'q_' + Math.random().toString(36).substring(2, 9);

export const getInitials = (name) => {
  if (!name) return 'A';
  return name.split(' ').map(n => n[0]).join('').toUpperCase();
};

export const truncateText = (text, maxLength = 30) => {
  if (!text) return '';
  return text.length > maxLength ? text.slice(0, maxLength) + '…' : text;
};
