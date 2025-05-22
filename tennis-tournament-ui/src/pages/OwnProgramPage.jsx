// src/pages/OwnProgramPage.jsx
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
    TableBody
} from '@mui/material';
import axiosInstance from '../api/axiosConfig';

export default function OwnProgramPage() {
    const [matches, setMatches] = useState([]);
    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');

    useEffect(() => {
        if (storedUser?.role === 'REFEREE') {
            axiosInstance
                .get(`/matches/referee/${storedUser.id}`)
                .then(res => setMatches(res.data))
                .catch(err =>
                    alert(
                        'Error fetching your program: ' +
                        (err.response?.data || err.message)
                    )
                );
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
                                    {matches.map(m => (
                                        <TableRow key={m.id}>
                                            <TableCell>{m.id}</TableCell>
                                            <TableCell>{m.tournamentName}</TableCell>
                                            <TableCell>{m.player1Username}</TableCell>
                                            <TableCell>{m.player2Username}</TableCell>
                                            <TableCell>{m.score || 'N/A'}</TableCell>
                                            <TableCell>{m.startTime}</TableCell>
                                            <TableCell>{m.endTime}</TableCell>
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
