import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  FaSearch,
  FaUser,
  FaBriefcase,
  FaSignOutAlt,
  FaHome,
  FaMicrophone
} from 'react-icons/fa';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-inner">
          <div className="navbar-brand">
            <Link to="/" className="navbar-logo">
              <span className="navbar-logo-text">JobFinder</span>
              <span className="navbar-logo-ai">AI</span>
            </Link>
          </div>

          <div className="navbar-links">
            <Link to="/" className="navbar-link">
              <FaHome /> <span>Home</span>
            </Link>
            <Link to="/jobs" className="navbar-link">
              <FaSearch /> <span>Jobs</span>
            </Link>

            {user ? (
              <>
                <Link to="/interview/setup" className="navbar-link">
                  <FaMicrophone /> <span>Interview</span>
                </Link>
                <Link to="/applied" className="navbar-link">
                  <FaBriefcase /> <span>Applied</span>
                </Link>
                <Link to="/profile" className="navbar-link">
                  <FaUser /> <span>Profile</span>
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="navbar-logout"
                >
                  <FaSignOutAlt /> <span>Logout</span>
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="navbar-link">
                  Login
                </Link>
                <Link to="/register" className="navbar-link-btn">
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
