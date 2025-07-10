import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { store } from './store';
import './index.css';

// Preload commonly used Ant Design components to avoid chunk loading errors
import 'antd/es/form';
import 'antd/es/input';
import 'antd/es/input-number';
import 'antd/es/select';
import 'antd/es/date-picker';

// Import clearAuth utility for development
if (process.env.NODE_ENV === 'development') {
  import('./utils/clearAuth');
}

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Provider>
  </React.StrictMode>
);