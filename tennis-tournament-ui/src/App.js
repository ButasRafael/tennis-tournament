// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PlayerDashboard from './pages/PlayerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import RefereeDashboard from './pages/RefereeDashboard';
import RegistrationRequestsPage from './pages/RegistrationRequestsPage';
import FilterPlayersPage from './pages/FilterPlayersPage';
import UpdateAccountPage from './pages/UpdateAccountPage';
import TournamentRegistrationPage from './pages/TournamentRegistrationPage';
import MatchesSchedulePage from './pages/MatchesSchedulePage';
import ManageUsersPage from './pages/ManageUsersPage';
import ExportMatchesPage from './pages/ExportMatchesPage';
import OwnProgramPage from './pages/OwnProgramPage';
import ManageScorePage from './pages/ManageScorePage';
import CreateTournamentPage from './pages/CreateTournamentPage';
import CreateMatchPage from './pages/CreateMatchPage';
import LogoutPage from './pages/LogoutPage';

const pageVariants = {
  initial: { opacity: 0, y: 10 },
  in:      { opacity: 1, y: 0  },
  out:     { opacity: 0, y: -10 }
};
const pageTransition = { type: 'tween', ease: 'easeInOut', duration: 0.6 };
const PageWrapper = ({ children }) => (
    <motion.div
        initial="initial"
        animate="in"
        exit="out"
        variants={pageVariants}
        transition={pageTransition}
        style={{ height: '100%' }}
    >
      {children}
    </motion.div>
);

const AnimatedRoutes = () => {
  const location = useLocation();
  return (
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>

          {/* Public routes */}
          <Route path="/" element={<PageWrapper><HomePage /></PageWrapper>} />
          <Route path="/login" element={<PageWrapper><LoginPage /></PageWrapper>} />
          <Route path="/register" element={<PageWrapper><RegisterPage /></PageWrapper>} />

          {/* Player routes */}
          <Route
              path="/player"
              element={
                <ProtectedRoute allowedRoles={[ 'PLAYER' ]}>
                  <PageWrapper><PlayerDashboard /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/update-account"
              element={
                <ProtectedRoute allowedRoles={[ 'PLAYER', 'ADMIN', 'REFEREE' ]}>
                  <PageWrapper><UpdateAccountPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/tournament-registration"
              element={
                <ProtectedRoute allowedRoles={[ 'PLAYER' ]}>
                  <PageWrapper><TournamentRegistrationPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/matches-schedule"
              element={
                <ProtectedRoute allowedRoles={[ 'PLAYER' ]}>
                  <PageWrapper><MatchesSchedulePage /></PageWrapper>
                </ProtectedRoute>
              }
          />

          {/* Admin routes */}
          <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><AdminDashboard /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/manage-users"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><ManageUsersPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/export-matches"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><ExportMatchesPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/create-tournament"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><CreateTournamentPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/create-match"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><CreateMatchPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/admin/requests"
              element={
                <ProtectedRoute allowedRoles={[ 'ADMIN' ]}>
                  <PageWrapper><RegistrationRequestsPage /></PageWrapper>
                </ProtectedRoute>
              }
          />

          {/* Referee routes */}
          <Route
              path="/referee"
              element={
                <ProtectedRoute allowedRoles={[ 'REFEREE' ]}>
                  <PageWrapper><RefereeDashboard /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/referee/filter-players"
              element={
                <ProtectedRoute allowedRoles={[ 'REFEREE' ]}>
                  <PageWrapper><FilterPlayersPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/own-program"
              element={
                <ProtectedRoute allowedRoles={[ 'REFEREE' ]}>
                  <PageWrapper><OwnProgramPage /></PageWrapper>
                </ProtectedRoute>
              }
          />
          <Route
              path="/manage-score"
              element={
                <ProtectedRoute allowedRoles={[ 'REFEREE' ]}>
                  <PageWrapper><ManageScorePage /></PageWrapper>
                </ProtectedRoute>
              }
          />

          {/* Logout (any authenticated) */}
          <Route
              path="/logout"
              element={
                <ProtectedRoute allowedRoles={[ 'PLAYER', 'ADMIN', 'REFEREE' ]}>
                  <PageWrapper><LogoutPage /></PageWrapper>
                </ProtectedRoute>
              }
          />

          {/* Fallback route could be added here */}
        </Routes>
      </AnimatePresence>
  );
};

function App({ mode, setMode }) {
  return (
      <Router>
        <Layout mode={mode} setMode={setMode}>
          <AnimatedRoutes />
        </Layout>
      </Router>
  );
}

export default App;
