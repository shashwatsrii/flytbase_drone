// Utility function to completely clear authentication data
export const clearAuthData = () => {
  // Clear localStorage
  localStorage.removeItem('token');
  
  // Clear any other stored data if needed
  localStorage.clear();
  
  // Force reload to reset Redux state
  window.location.href = '/login';
};

// For development - expose to window object
if (process.env.NODE_ENV === 'development') {
  (window as any).clearAuthData = clearAuthData;
}