// src/pages/OwnProgramPage.jsx
import React, { useState, useEffect } from 'react';
import { Container, Card, CardContent, Typography, TableContainer, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import axiosInstance from '../api/axiosConfig';

function OwnProgramPage() {
    const [matches, setMatches] = useState([]);
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        if (storedUser && storedUser.role.toUpperCase() === 'REFEREE') {
            axiosInstance.get(`/matches/referee/${storedUser.id}`)
                .then(response => setMatches(response.data))
                .catch(error => alert('Error fetching your program: ' + (error.response?.data || error.message)));
        }
    }, [storedUser]);

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" gutterBottom>
                        Your Referee Program
                    </Typography>
                    {matches.length === 0 ? (
                        <Typography>No matches assigned to you.</Typography>
                    ) : (
                        <TableContainer>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Match ID</TableCell>
                                        <TableCell>Tournament</TableCell>
                                        <TableCell>Player 1</TableCell>
                                        <TableCell>Player 2</TableCell>
                                        <TableCell>Score</TableCell>
                                        <TableCell>Start Time</TableCell>
                                        <TableCell>End Time</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {matches.map(match => (
                                        <TableRow key={match.id}>
                                            <TableCell>{match.id}</TableCell>
                                            <TableCell>{match.tournament ? match.tournament.name : 'N/A'}</TableCell>
                                            <TableCell>{match.player1 ? match.player1.username : 'N/A'}</TableCell>
                                            <TableCell>{match.player2 ? match.player2.username : 'N/A'}</TableCell>
                                            <TableCell>{match.score || 'N/A'}</TableCell>
                                            <TableCell>{match.startTime}</TableCell>
                                            <TableCell>{match.endTime}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </CardContent>
            </Card>
        </Container>
    );
}

export default OwnProgramPage;
