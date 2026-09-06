import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { FaUser, FaEnvelope, FaTools, FaCalendar, FaMapMarker } from 'react-icons/fa';
import './Profile.css';

const Profile = () => {
  const { user, updateUser } = useAuth();
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    primarySkills: user?.primarySkills || '',
    yearsOfExperience: user?.yearsOfExperience || 0,
    preferredLocation: user?.preferredLocation || '',
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEditing(false);
  };

  if (!user) return null;

  return (
    <div className="profile-page">
      <div className="profile-card">
        <div className="profile-header">
          <h1 className="profile-title">Profile</h1>
          <button
            type="button"
            onClick={() => setEditing(!editing)}
            className="profile-edit-btn"
          >
            {editing ? 'Cancel' : 'Edit Profile'}
          </button>
        </div>

        {editing ? (
          <form onSubmit={handleSubmit} className="profile-form">
            <div className="profile-form-row">
              <div className="profile-form-group">
                <label>First Name</label>
                <input
                  type="text"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>
              <div className="profile-form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                />
              </div>
            </div>
            <div className="profile-form-group">
              <label>Skills (comma separated)</label>
              <input
                type="text"
                placeholder="Java, React, SQL"
                value={formData.primarySkills}
                onChange={(e) => setFormData({ ...formData, primarySkills: e.target.value })}
              />
            </div>
            <div className="profile-form-group">
              <label>Years of Experience</label>
              <input
                type="number"
                value={formData.yearsOfExperience}
                onChange={(e) => setFormData({ ...formData, yearsOfExperience: parseInt(e.target.value) })}
              />
            </div>
            <div className="profile-form-group">
              <label>Preferred Location</label>
              <input
                type="text"
                placeholder="Remote, Bangalore, etc."
                value={formData.preferredLocation}
                onChange={(e) => setFormData({ ...formData, preferredLocation: e.target.value })}
              />
            </div>
            <button type="submit" className="profile-save-btn">
              Save Changes
            </button>
          </form>
        ) : (
          <div>
            <div className="profile-field">
              <FaUser className="icon" />
              <span className="label">{user.firstName} {user.lastName}</span>
            </div>
            <div className="profile-field">
              <FaEnvelope className="icon" />
              <span className="label">{user.email}</span>
            </div>
            <div className="profile-field">
              <FaTools className="icon" />
              <span className="label">{user.primarySkills || 'No skills added'}</span>
            </div>
            <div className="profile-field">
              <FaCalendar className="icon" />
              <span className="label">{user.yearsOfExperience || 0} years</span>
            </div>
            <div className="profile-field">
              <FaMapMarker className="icon" />
              <span className="label">{user.preferredLocation || 'No location set'}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;
