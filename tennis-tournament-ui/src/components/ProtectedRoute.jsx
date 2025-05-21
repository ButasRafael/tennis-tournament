// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';

/**
 * Restricts access to routes based on allowedRoles.
 * Usage: wrap a Route's element prop:
 * <Route path="/admin" element={
 *   <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
 *     <AdminDashboard />
 *   </ProtectedRoute>
 * } />
 */
export default function ProtectedRoute({ allowedRoles, children }) {
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (!user) {
        // not logged in
        return <Navigate to="/login" replace />;
    }
    if (!allowedRoles.includes(user.role)) {
        // logged in but unauthorized
        return <Navigate to="/" replace />;
    }
    return children;
}