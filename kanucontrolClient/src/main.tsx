import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App' // Update this if your App component is in a different location
import './index.css'; // Update the CSS import if necessary

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
