// src/pages/ManageUsersPage.jsx
import React, { useState, useEffect } from 'react';
import {
    Container,
    Card,
    CardContent,
    Typography,
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Button,
    TextField,
    Snackbar,
    Alert,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions
} from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SaveIcon from '@mui/icons-material/Save';

function ManageUsersPage() {
    const [users, setUsers] = useState([]);
    const [editingUserId, setEditingUserId] = useState(null);
    const [editFormData, setEditFormData] = useState({ newUsername: '', newEmail: '' });

    // Snackbar state
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    // Dialog state for delete confirmation
    const [dialogOpen, setDialogOpen] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        if (storedUser && storedUser.role.toUpperCase() === 'ADMIN') {
            fetchUsers();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [storedUser]);

    const fetchUsers = () => {
        axiosInstance
            .get('/admin/users', { params: { currentUserId: storedUser.id } })
            .then((response) => setUsers(response.data))
            .catch((error) =>
                setSnackbar({
                    open: true,
                    message: 'Error fetching users: ' + (error.response?.data || error.message),
                    severity: 'error'
                })
            );
    };

    // Show the delete confirmation dialog
    const confirmDeleteUser = (userId) => {
        setUserToDelete(userId);
        setDialogOpen(true);
    };

    // Actually delete the user after confirming in the dialog
    const handleDeleteUser = () => {
        if (!userToDelete) return;

        axiosInstance
            .delete(`/admin/users/${userToDelete}`, { params: { currentUserId: storedUser.id } })
            .then(() => {
                setSnackbar({ open: true, message: 'User deleted successfully.', severity: 'success' });
                fetchUsers();
            })
            .catch((error) =>
                setSnackbar({
                    open: true,
                    message: 'Error deleting user: ' + (error.response?.data || error.message),
                    severity: 'error'
                })
            )
            .finally(() => {
                setDialogOpen(false);
                setUserToDelete(null);
            });
    };

    const startEditing = (user) => {
        setEditingUserId(user.id);
        setEditFormData({ newUsername: user.username, newEmail: user.email });
    };

    const cancelEditing = () => {
        setEditingUserId(null);
        setEditFormData({ newUsername: '', newEmail: '' });
    };

    const handleEditChange = (e) => {
        setEditFormData({ ...editFormData, [e.target.name]: e.target.value });
    };

    const saveUser = (userId) => {
        axiosInstance
            .put(`/users/${userId}`, null, {
                params: {
                    newUsername: editFormData.newUsername,
                    newEmail: editFormData.newEmail,
                    newPassword: '' // Not updating password here
                }
            })
            .then(() => {
                setSnackbar({ open: true, message: 'User updated successfully.', severity: 'success' });
                setEditingUserId(null);
                fetchUsers();
            })
            .catch((error) =>
                setSnackbar({
                    open: true,
                    message: 'Error updating user: ' + (error.response?.data || error.message),
                    severity: 'error'
                })
            );
    };

    // Close the Snackbar
    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') return;
        setSnackbar({ ...snackbar, open: false });
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" gutterBottom>
                        Manage Application Users
                    </Typography>
                    {users.length === 0 ? (
                        <Typography>No users found.</Typography>
                    ) : (
                        <TableContainer>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>ID</TableCell>
                                        <TableCell>Username</TableCell>
                                        <TableCell>Email</TableCell>
                                        <TableCell>Role</TableCell>
                                        <TableCell>Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {users.map((user) => (
                                        <TableRow key={user.id}>
                                            <TableCell>{user.id}</TableCell>
                                            <TableCell>
                                                {editingUserId === user.id ? (
                                                    <TextField
                                                        name="newUsername"
                                                        value={editFormData.newUsername}
                                                        onChange={handleEditChange}
                                                    />
                                                ) : (
                                                    user.username
                                                )}
                                            </TableCell>
                                            <TableCell>
                                                {editingUserId === user.id ? (
                                                    <TextField
                                                        name="newEmail"
                                                        value={editFormData.newEmail}
                                                        onChange={handleEditChange}
                                                    />
                                                ) : (
                                                    user.email
                                                )}
                                            </TableCell>
                                            <TableCell>{user.role}</TableCell>
                                            <TableCell>
                                                {editingUserId === user.id ? (
                                                    <>
                                                        <Button
                                                            variant="contained"
                                                            size="small"
                                                            onClick={() => saveUser(user.id)}
                                                            startIcon={<SaveIcon />}
                                                        >
                                                            Save
                                                        </Button>
                                                        <Button variant="outlined" size="small" onClick={cancelEditing} sx={{ ml: 1 }}>
                                                            Cancel
                                                        </Button>
                                                    </>
                                                ) : (
                                                    <>
                                                        <Button
                                                            variant="outlined"
                                                            size="small"
                                                            onClick={() => startEditing(user)}
                                                            startIcon={<EditIcon />}
                                                        >
                                                            Update
                                                        </Button>
                                                        <Button
                                                            variant="outlined"
                                                            color="error"
                                                            size="small"
                                                            onClick={() => confirmDeleteUser(user.id)}
                                                            startIcon={<DeleteIcon />}
                                                            sx={{ ml: 1 }}
                                                        >
                                                            Delete
                                                        </Button>
                                                    </>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </CardContent>
            </Card>

            {/* Delete Confirmation Dialog */}
            <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
                <DialogTitle>Confirm Delete</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete this user? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleDeleteUser} variant="contained" color="error">
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Snackbar for success/error messages */}
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

export default ManageUsersPage;
