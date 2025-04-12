// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PlayerDashboard from './pages/PlayerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import RefereeDashboard from './pages/RefereeDashboard';
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
  initial: {
    opacity: 0,
    y: 10,
  },
  in: {
    opacity: 1,
    y: 0,
  },
  out: {
    opacity: 0,
    y: -10,
  },
};

const pageTransition = {
  type: "tween",
  ease: "easeInOut",
  duration: 0.6,
};

const PageWrapper = ({ children }) => (
    <motion.div
        initial="initial"
        animate="in"
        exit="out"
        variants={pageVariants}
        transition={pageTransition}
        style={{ height: "100%" }}
    >
      {children}
    </motion.div>
);

const AnimatedRoutes = () => {
  const location = useLocation();
  return (
      <AnimatePresence exitBeforeEnter>
        <Routes location={location} key={location.pathname}>
          <Route path="/" element={<PageWrapper><HomePage /></PageWrapper>} />
          <Route path="/login" element={<PageWrapper><LoginPage /></PageWrapper>} />
          <Route path="/register" element={<PageWrapper><RegisterPage /></PageWrapper>} />
          <Route path="/player" element={<PageWrapper><PlayerDashboard /></PageWrapper>} />
          <Route path="/admin" element={<PageWrapper><AdminDashboard /></PageWrapper>} />
          <Route path="/referee" element={<PageWrapper><RefereeDashboard /></PageWrapper>} />
          <Route path="/update-account" element={<PageWrapper><UpdateAccountPage /></PageWrapper>} />
          <Route path="/tournament-registration" element={<PageWrapper><TournamentRegistrationPage /></PageWrapper>} />
          <Route path="/matches-schedule" element={<PageWrapper><MatchesSchedulePage /></PageWrapper>} />
          <Route path="/manage-users" element={<PageWrapper><ManageUsersPage /></PageWrapper>} />
          <Route path="/export-matches" element={<PageWrapper><ExportMatchesPage /></PageWrapper>} />
          <Route path="/own-program" element={<PageWrapper><OwnProgramPage /></PageWrapper>} />
          <Route path="/manage-score" element={<PageWrapper><ManageScorePage /></PageWrapper>} />
          <Route path="/create-tournament" element={<PageWrapper><CreateTournamentPage /></PageWrapper>} />
          <Route path="/create-match" element={<PageWrapper><CreateMatchPage /></PageWrapper>} />
          <Route path="/logout" element={<PageWrapper><LogoutPage /></PageWrapper>} />
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
