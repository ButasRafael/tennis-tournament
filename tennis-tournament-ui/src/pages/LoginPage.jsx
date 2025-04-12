// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import {
    Container,
    TextField,
    Button,
    Card,
    CardContent,
    Typography,
    Link,
    Snackbar,
    Alert
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosConfig';
import LoginIcon from '@mui/icons-material/Login';

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            // Use your axiosInstance so that (if needed) interceptors work for errors.
            const response = await axiosInstance.post('/users/login', null, {
                params: { username, password }
            });
            // The backend response should contain accessToken, refreshToken and the user role (or other user info)
            // Ensure that your backend includes "role" as part of the user object in the token response.
            localStorage.setItem('user', JSON.stringify(response.data));
            // Here we assume role is returned as part of response.data; adjust if needed.
            const role = response.data.role && response.data.role.toUpperCase();
            setSnackbar({ open: true, message: 'Login successful!', severity: 'success' });
            // Redirect based on role (you can change routes accordingly)
            if (role === 'PLAYER') navigate('/player');
            else if (role === 'ADMIN') navigate('/admin');
            else if (role === 'REFEREE') navigate('/referee');
            else navigate('/');
        } catch (error) {
            setSnackbar({
                open: true,
                message: 'Login failed: ' + (error.response?.data || error.message),
                severity: 'error'
            });
        }
    };

    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') return;
        setSnackbar({ ...snackbar, open: false });
    };

    return (
        <Container
            maxWidth="sm"
            sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '80vh'
            }}
        >
            <Card sx={{ width: '100%' }}>
                <CardContent>
                    <Typography variant="h5" align="center" gutterBottom>
                        Login
                    </Typography>
                    <form onSubmit={handleLogin}>
                        <TextField
                            label="Username"
                            variant="outlined"
                            fullWidth
                            margin="normal"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                        <TextField
                            label="Password"
                            type="password"
                            variant="outlined"
                            fullWidth
                            margin="normal"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <Button
                            type="submit"
                            variant="contained"
                            color="primary"
                            fullWidth
                            startIcon={<LoginIcon />}
                            sx={{ mt: 2 }}
                        >
                            Login
                        </Button>
                    </form>
                    <Typography variant="body2" align="center" sx={{ mt: 2 }}>
                        Don't have an account?{' '}
                        <Link href="/register" underline="hover">
                            Register here
                        </Link>
                    </Typography>
                </CardContent>
            </Card>
            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
}

export default LoginPage;
