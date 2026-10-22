import { render, screen } from '@testing-library/react';
import App from './App';

test('renders welcome heading on home page', () => {
  render(<App />);
  const headingElement = screen.getByText(/Shop for the new trends at never seen prices/i);
  expect(headingElement).toBeInTheDocument();
});
