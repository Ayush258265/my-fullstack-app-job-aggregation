import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { jobService } from '../../services/jobService';
import { FaCheck, FaTimes, FaExclamationCircle } from 'react-icons/fa';
import toast from 'react-hot-toast';
import './ApplyPopup.css';

const ApplyPopup = ({ jobId, jobTitle, companyName, onClose }) => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);

  const handleTrack = async (applied) => {
    if (!user) {
      toast.error('Please login to track your applications');
      onClose();
      return;
    }

    setLoading(true);
    try {
      await jobService.trackApply({ jobId, applied });
      toast.success(applied ? '✅ Application tracked as APPLIED' : '📌 Application tracked as NOT APPLIED');
      onClose();
    } catch (error) {
      toast.error('Failed to track application. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="popup-overlay">
      <div className="popup-box">
        <button
          onClick={onClose}
          className="popup-close"
        >
          <FaTimes size={20} />
        </button>

        <div className="popup-content">
          <FaExclamationCircle className="popup-icon" />
          <h3 className="popup-title">Did you apply?</h3>
          <p className="popup-text">
            For <strong>{jobTitle}</strong> at <strong>{companyName}</strong>
          </p>
          <p className="popup-subtext">
            This helps you track your job applications.
          </p>
        </div>

        <div className="popup-actions">
          <button
            onClick={() => handleTrack(true)}
            disabled={loading}
            className="popup-btn-yes"
          >
            <FaCheck /> Yes, I applied
          </button>
          <button
            onClick={() => handleTrack(false)}
            disabled={loading}
            className="popup-btn-no"
          >
            <FaTimes /> No, not yet
          </button>
        </div>

        {!user && (
          <p className="popup-login-warning">
            You need to login to track applications.
          </p>
        )}
      </div>
    </div>
  );
};

export default ApplyPopup;