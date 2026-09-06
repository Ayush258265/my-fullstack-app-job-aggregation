import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { FaUser, FaEnvelope, FaLock, FaTools, FaCalendar, FaMapMarker } from 'react-icons/fa';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    primarySkills: '',
    yearsOfExperience: 0,
    preferredLocation: '',
  });
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const result = await register(formData);
    setLoading(false);
    if (result.success) {
      navigate('/');
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <div>
          <h2 className="register-title">Create Account</h2>
          <p className="register-subtitle">Join JobFinder AI today</p>
        </div>

        <form onSubmit={handleSubmit} className="register-form">
          <div className="register-row">
            <div className="register-input-group">
              <label>First Name</label>
              <div className="input-wrapper">
                <FaUser className="icon" />
                <input
                  type="text"
                  required
                  placeholder="John"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>
            </div>
            <div className="register-input-group">
              <label>Last Name</label>
              <div className="input-wrapper">
                <FaUser className="icon" />
                <input
                  type="text"
                  required
                  placeholder="Doe"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                />
              </div>
            </div>
          </div>

          <div className="register-input-group">
            <label>Email</label>
            <div className="input-wrapper">
              <FaEnvelope className="icon" />
              <input
                type="email"
                required
                placeholder="you@example.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>
          </div>

          <div className="register-input-group">
            <label>Password</label>
            <div className="input-wrapper">
              <FaLock className="icon" />
              <input
                type="password"
                required
                minLength="6"
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
            </div>
          </div>

          <div className="register-input-group">
            <label>Skills (comma separated)</label>
            <div className="input-wrapper">
              <FaTools className="icon" />
              <input
                type="text"
                placeholder="Java, React, SQL"
                value={formData.primarySkills}
                onChange={(e) => setFormData({ ...formData, primarySkills: e.target.value })}
              />
            </div>
          </div>

          <div className="register-input-group">
            <label>Years of Experience</label>
            <div className="input-wrapper">
              <FaCalendar className="icon" />
              <input
                type="number"
                placeholder="0"
                value={formData.yearsOfExperience}
                onChange={(e) => setFormData({ ...formData, yearsOfExperience: parseInt(e.target.value) || 0 })}
              />
            </div>
          </div>

          <div className="register-input-group">
            <label>Preferred Location</label>
            <div className="input-wrapper">
              <FaMapMarker className="icon" />
              <input
                type="text"
                placeholder="Remote, Bangalore, etc."
                value={formData.preferredLocation}
                onChange={(e) => setFormData({ ...formData, preferredLocation: e.target.value })}
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className="register-btn">
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="register-footer">
          Already have an account?{' '}
          <Link to="/login">Sign In</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
