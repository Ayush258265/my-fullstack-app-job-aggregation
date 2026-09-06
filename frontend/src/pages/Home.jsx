import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { FaSearch, FaRobot, FaBriefcase } from 'react-icons/fa';
import './Home.css';

const Home = () => {
  const { user } = useAuth();

  return (
    <div className="home-container">
      <div className="home-hero">
        <h1 className="home-hero-title">
          Find Your Dream Job with{' '}
          <span className="highlight">AI</span>
        </h1>
        <p className="home-hero-subtitle">
          Search jobs, track applications, and practice interviews with AI-powered coaching.
        </p>
        <div className="home-hero-actions">
          <Link to="/jobs" className="home-hero-btn home-hero-btn-primary">
            <FaSearch /> Search Jobs
          </Link>
          {!user && (
            <Link to="/register" className="home-hero-btn home-hero-btn-secondary">
              Get Started
            </Link>
          )}
        </div>
      </div>

      <div className="home-features">
        <div className="home-feature-card">
          <FaBriefcase className="home-feature-icon" />
          <h3 className="home-feature-title">Job Aggregator</h3>
          <p className="home-feature-text">
            Browse jobs from multiple sources in one place. Smart matching shows your fit percentage.
          </p>
        </div>
        <div className="home-feature-card">
          <FaRobot className="home-feature-icon" />
          <h3 className="home-feature-title">AI Interview Coach</h3>
          <p className="home-feature-text">
            Practice interviews with AI. Get real-time feedback on your answers.
          </p>
        </div>
        <div className="home-feature-card">
          <FaSearch className="home-feature-icon" />
          <h3 className="home-feature-title">Smart Tracking</h3>
          <p className="home-feature-text">
            Track your applications. Know what you've applied to and what's pending.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Home;
