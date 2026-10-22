import { render, screen } from '@testing-library/react';
import App from './App';

test('renders brand title', () => {
  render(<App />);
  const titleElement = screen.getByText(/Have the flamboyance in you have no bounds/i);
  expect(titleElement).toBeInTheDocument();
});
