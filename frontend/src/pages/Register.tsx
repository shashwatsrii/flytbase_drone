import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  Button,
  TextField,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  Visibility,
  VisibilityOff,
  Email,
  Lock,
  Person,
  Business,
} from '@mui/icons-material';
import { RootState, AppDispatch } from '../store';
import { register, clearError } from '../store/slices/authSlice';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { loading, error, isAuthenticated } = useSelector((state: RootState) => state.auth);

  const [formData, setFormData] = useState({
    organizationName: '',
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [validationError, setValidationError] = useState('');

  const [registrationSuccess, setRegistrationSuccess] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/app/dashboard');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    return () => {
      dispatch(clearError());
    };
  }, [dispatch]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setValidationError('');
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    // Validate passwords match
    if (formData.password !== formData.confirmPassword) {
      setValidationError('Passwords do not match');
      return;
    }

    // Validate password strength
    if (formData.password.length < 6) {
      setValidationError('Password must be at least 6 characters long');
      return;
    }

    const { confirmPassword, ...registerData } = formData;
    dispatch(register(registerData)).then((result) => {
      if (register.fulfilled.match(result)) {
        setRegistrationSuccess(true);
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      }
    });
  };

  const handleClickShowPassword = () => {
    setShowPassword(!showPassword);
  };

  const handleClickShowConfirmPassword = () => {
    setShowConfirmPassword(!showConfirmPassword);
  };

  return (
    <Box sx={{ width: '100%', maxWidth: 400 }}>
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography component="h1" variant="h4" sx={{ fontWeight: 'bold', mb: 1 }}>
          Create Account
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Join us to start managing your drone surveys
        </Typography>
      </Box>

      {registrationSuccess && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Registration successful! Redirecting to login...
        </Alert>
      )}
      
      {(error || validationError) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error || validationError}
        </Alert>
      )}

      <form onSubmit={handleSubmit}>
        <TextField
          margin="normal"
          required
          fullWidth
          id="organizationName"
          label="Organization Name"
          name="organizationName"
          autoComplete="organization"
          autoFocus
          value={formData.organizationName}
          onChange={handleChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Business color="action" />
              </InputAdornment>
            ),
          }}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          id="fullName"
          label="Full Name"
          name="fullName"
          autoComplete="name"
          value={formData.fullName}
          onChange={handleChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Person color="action" />
              </InputAdornment>
            ),
          }}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          id="email"
          label="Email Address"
          name="email"
          autoComplete="email"
          value={formData.email}
          onChange={handleChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Email color="action" />
              </InputAdornment>
            ),
          }}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="password"
          label="Password"
          type={showPassword ? 'text' : 'password'}
          id="password"
          autoComplete="new-password"
          value={formData.password}
          onChange={handleChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Lock color="action" />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="toggle password visibility"
                  onClick={handleClickShowPassword}
                  edge="end"
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="confirmPassword"
          label="Confirm Password"
          type={showConfirmPassword ? 'text' : 'password'}
          id="confirmPassword"
          autoComplete="new-password"
          value={formData.confirmPassword}
          onChange={handleChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Lock color="action" />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="toggle password visibility"
                  onClick={handleClickShowConfirmPassword}
                  edge="end"
                >
                  {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        <Button
          type="submit"
          fullWidth
          variant="contained"
          sx={{ mt: 3, mb: 2, py: 1.5 }}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Sign Up'}
        </Button>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="body2">
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#1976d2', textDecoration: 'none' }}>
              Sign in
            </Link>
          </Typography>
        </Box>
      </form>
    </Box>
  );
};

export default Register;