import { render, screen } from '@testing-library/react';
import App from './App';

test('renders student login page', () => {
  render(<App />);
  const loginHeader = screen.getByText(/Student Login/i);
  expect(loginHeader).toBeInTheDocument();
});

