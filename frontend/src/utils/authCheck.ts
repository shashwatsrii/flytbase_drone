import api from '../services/api';

export const verifyAuthToken = async () => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    return false;
  }
  
  try {
    // Try to fetch user data with the token
    // You'll need to replace this with your actual user endpoint
    await api.get('/auth/me');
    return true;
  } catch (error: any) {
    // If the token is invalid, clear it
    if (error.response?.status === 401 || error.response?.status === 404) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return false;
    }
    // For other errors, we might want to keep the token
    // (e.g., network errors)
    return true;
  }
};