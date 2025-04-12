// src/pages/UpdateAccountPage.jsx
import React, { useState } from 'react';
import { Container, Card, CardContent, TextField, Button, Typography, Snackbar, Alert } from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import SaveIcon from '@mui/icons-material/Save';

function UpdateAccountPage() {
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    const [newUsername, setNewUsername] = useState(storedUser ? storedUser.username : '');
    const [newEmail, setNewEmail] = useState(storedUser ? storedUser.email : '');
    const [newPassword, setNewPassword] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    const handleUpdate = () => {
        axiosInstance.put(`/users/${storedUser.id}`, null, {
            params: { newUsername, newEmail, newPassword }
        })
            .then(response => {
                setSnackbar({ open: true, message: 'Account updated successfully!', severity: 'success' });
                localStorage.setItem('user', JSON.stringify(response.data));
            })
            .catch(error => {
                setSnackbar({ open: true, message: 'Error updating account: ' + (error.response?.data || error.message), severity: 'error' });
            });
    };

    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') return;
        setSnackbar({ ...snackbar, open: false });
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" gutterBottom>
                        Update Account Information
                    </Typography>
                    <TextField
                        label="New Username"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={newUsername}
                        onChange={(e) => setNewUsername(e.target.value)}
                    />
                    <TextField
                        label="New Email"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={newEmail}
                        onChange={(e) => setNewEmail(e.target.value)}
                    />
                    <TextField
                        label="New Password"
                        type="password"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                    />
                    <Button variant="contained" color="primary" onClick={handleUpdate} startIcon={<SaveIcon />} sx={{ mt: 2 }}>
                        Update Account
                    </Button>
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

export default UpdateAccountPage;
