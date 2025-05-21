// src/pages/FilterPlayersPage.jsx
import React, { useState } from 'react';
import {
    Container, Card, CardContent, Typography, TextField,
    Button, Table, TableHead, TableRow, TableCell,
    TableBody, Snackbar, Alert, Grid
} from '@mui/material';
import axios from '../api/axiosConfig';

export default function FilterPlayersPage() {
    const [username, setUsername] = useState('');
    const [tournamentId, setTournamentId] = useState('');
    const [players, setPlayers] = useState([]);
    const [snack, setSnack] = useState({ open:false, msg:'', sev:'success' });

    const handleSearch = () =>
        axios.get('/referee/players', { params:{ username, tournamentId: tournamentId||undefined }})
            .then(r => setPlayers(r.data))
            .catch(e => setSnack({ open:true, msg:e.response?.data||e.message, sev:'error' }));

    return (
        <Container sx={{ mt:4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5">Filter Players</Typography>
                    <Grid container spacing={2} sx={{ mb:2 }}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Username contains"
                                fullWidth
                                value={username}
                                onChange={e=>setUsername(e.target.value)}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Tournament ID"
                                type="number"
                                fullWidth
                                value={tournamentId}
                                onChange={e=>setTournamentId(e.target.value)}
                            />
                        </Grid>
                    </Grid>
                    <Button variant="contained" onClick={handleSearch}>Search</Button>
                    {players.length > 0 && (
                        <Table sx={{ mt:2 }}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>ID</TableCell><TableCell>Username</TableCell><TableCell>Email</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {players.map(u=>(
                                    <TableRow key={u.id}>
                                        <TableCell>{u.id}</TableCell>
                                        <TableCell>{u.username}</TableCell>
                                        <TableCell>{u.email}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>

            <Snackbar
                open={snack.open}
                autoHideDuration={4000}
                onClose={()=>setSnack({...snack,open:false})}
                anchorOrigin={{ vertical:'top', horizontal:'center' }}
            >
                <Alert severity={snack.sev}>{snack.msg}</Alert>
            </Snackbar>
        </Container>
    );
}
