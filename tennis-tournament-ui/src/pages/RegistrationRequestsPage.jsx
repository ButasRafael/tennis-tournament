// src/pages/RegistrationRequestsPage.jsx
import React, { useState, useEffect } from 'react';
import {
    Container, Card, CardContent, Typography, Table, TableHead,
    TableRow, TableCell, TableBody, Button, Snackbar, Alert
} from '@mui/material';
import axios from '../api/axiosConfig';

export default function RegistrationRequestsPage() {
    const [reqs, setReqs] = useState([]);
    const [snack, setSnack] = useState({ open:false, msg:'', sev:'success' });

    const fetchPending = () =>
        axios.get('/admin/registration-requests', { params:{ status:'PENDING' }})
            .then(r => setReqs(r.data))
            .catch(e => setSnack({ open:true, msg:e.response?.data||e.message, sev:'error' }));

    useEffect(() => {
        fetchPending();
    }, []);

    const handle = (id, approve) =>
        axios.post(`/admin/registration-requests/${id}/${approve?'approve':'deny'}`)
            .then(()=> {
                setSnack({ open:true, msg:`${approve?'Approved':'Denied'}!`, sev:'success' });
                fetchPending();
            })
            .catch(e => setSnack({ open:true, msg:e.response?.data||e.message, sev:'error' }));

    return (
        <Container sx={{ mt:4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5">Registration Requests</Typography>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>ID</TableCell><TableCell>Player</TableCell>
                                <TableCell>Tournament</TableCell><TableCell>Requested At</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {reqs.map(r => (
                                <TableRow key={r.id}>
                                    <TableCell>{r.id}</TableCell>
                                    <TableCell>{r.playerUsername}</TableCell>
                                    <TableCell>{r.tournamentName}</TableCell>
                                    <TableCell>{new Date(r.createdAt).toLocaleString()}</TableCell>
                                    <TableCell>
                                        <Button size="small" onClick={()=>handle(r.id,true)}>Approve</Button>
                                        <Button size="small" color="error" onClick={()=>handle(r.id,false)}>Deny</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
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
