import { Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Layout from './components/common/Layout';
import ProtectedRoute from './components/protected/ProtectedRoute';
import Home from './pages/Home';
import Jobs from './pages/Jobs';
import JobDetailsPage from './pages/JobDetailsPage';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Profile from './pages/Profile';
import AppliedJobs from './pages/AppliedJobs';
import NotFound from './pages/NotFound';
import { AuthProvider } from './context/AuthContext';

// ✅ IMPORT INTERVIEW PAGES
import InterviewSetup from './pages/InterviewSetup';
import InterviewSession from './pages/InterviewSession';
import InterviewSummary from './pages/InterviewSummary';
import InterviewHistory from './pages/InterviewHistory';

function App() {
  return (
    <AuthProvider>
      <Toaster position="top-right" />
      <Routes>
        {/* Public Routes - No Layout */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Public Routes - With Layout */}
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="jobs" element={<Jobs />} />
          <Route path="jobs/:id" element={<JobDetailsPage />} />
        </Route>

        {/* ✅ INTERVIEW ROUTES - Public (but require login via ProtectedRoute) */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            {/* Interview Routes */}
            <Route path="interview/setup" element={<InterviewSetup />} />
            <Route path="interview/session/:sessionId" element={<InterviewSession />} />
            <Route path="interview/summary/:sessionId" element={<InterviewSummary />} />
            <Route path="interview/history" element={<InterviewHistory />} />
            
            {/* Existing Protected Routes */}
            <Route path="profile" element={<Profile />} />
            <Route path="applied" element={<AppliedJobs />} />
          </Route>
        </Route>
        
        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;