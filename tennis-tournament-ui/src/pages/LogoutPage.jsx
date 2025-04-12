// src/pages/LogoutPage.jsx
import React, { useEffect } from 'react';
import { Container, Card, CardContent, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosConfig';

function LogoutPage() {
    const navigate = useNavigate();

    useEffect(() => {
        // Call the backend logout endpoint
        axiosInstance.post('/users/logout')
            .then(() => {
                // On successful logout, clear local storage and redirect to login
                localStorage.removeItem('user');
                navigate('/login');
            })
            .catch((error) => {
                // In case of error, still clear local storage and redirect to login
                console.error('Logout error:', error);
                localStorage.removeItem('user');
                navigate('/login');
            });
    }, [navigate]);

    return (
        <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
            <Card sx={{ width: '100%', maxWidth: 400 }}>
                <CardContent>
                    <Typography variant="h5" align="center">
                        Logging out...
                    </Typography>
                </CardContent>
            </Card>
        </Container>
    );
}

export default LogoutPage;
