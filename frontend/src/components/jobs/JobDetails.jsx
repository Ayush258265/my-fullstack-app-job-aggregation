import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { FaBuilding, FaMapMarker, FaCalendar, FaExternalLinkAlt } from 'react-icons/fa';
import ApplyPopup from './ApplyPopup';

const JobDetails = ({ job, loading }) => {
  const { user } = useAuth();
  const [showApplyPopup, setShowApplyPopup] = useState(false);

  // ✅ Add loading check
  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  // ✅ Add null check
  if (!job) {
    return (
      <div className="text-center py-12 text-gray-500">
        <h3 className="text-xl font-semibold">Job not found</h3>
      </div>
    );
  }

  const { id, title, companyName, location, description, postedDate, applyUrl, match, skills, experienceRequired } = job;

  const formatDate = (date) => {
    if (!date) return 'Recently';
    try {
      return new Date(date).toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return 'Recently';
    }
  };

  const handleApply = () => {
    if (applyUrl) {
      window.open(applyUrl, '_blank');
    }
    setShowApplyPopup(true);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      {/* Header */}
      <div className="border-b border-gray-200 pb-4 mb-4">
        <h1 className="text-2xl font-bold text-gray-900">{title || 'Untitled Position'}</h1>
        <div className="flex flex-wrap items-center gap-4 mt-2 text-gray-600">
          <span className="flex items-center">
            <FaBuilding className="mr-2" />
            {companyName || 'Unknown Company'}
          </span>
          <span className="flex items-center">
            <FaMapMarker className="mr-2" />
            {location || 'Remote'}
          </span>
          <span className="flex items-center">
            <FaCalendar className="mr-2" />
            Posted: {formatDate(postedDate)}
          </span>
        </div>

        {match && match.matchPercentage !== null && (
          <div className="mt-3">
            <span className={`px-4 py-2 rounded-full text-sm font-semibold inline-block ${match.matchPercentage >= 70 ? 'bg-green-100 text-green-800' :
                match.matchPercentage >= 40 ? 'bg-yellow-100 text-yellow-800' :
                  'bg-red-100 text-red-800'
              }`}>
              {match.matchPercentage}% Match
            </span>
          </div>
        )}
      </div>

      {/* Skills Comparison */}
      {match && match.matchingSkills && (
        <div className="bg-gray-50 rounded-lg p-4 mb-4">
          <h4 className="font-semibold mb-2">Your Match Breakdown</h4>
          {match.matchingSkills.length > 0 && (
            <div className="mb-2">
              <span className="text-sm font-medium text-gray-700">✅ Matching Skills:</span>
              <div className="flex flex-wrap gap-2 mt-1">
                {match.matchingSkills.map((skill, idx) => (
                  <span key={idx} className="text-sm bg-green-100 text-green-800 px-2 py-1 rounded">
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}
          {match.missingSkills && match.missingSkills.length > 0 && (
            <div>
              <span className="text-sm font-medium text-gray-700">⚠️ Skills to Learn:</span>
              <div className="flex flex-wrap gap-2 mt-1">
                {match.missingSkills.map((skill, idx) => (
                  <span key={idx} className="text-sm bg-red-100 text-red-800 px-2 py-1 rounded">
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}
          {match.experienceStatus && (
            <div className="mt-2 text-sm text-gray-600">
              {match.experienceStatus}
            </div>
          )}
          {match.recommendations && match.recommendations.length > 0 && (
            <div className="mt-3 pt-3 border-t border-gray-200">
              <span className="text-sm font-medium text-gray-700">💡 Recommendations:</span>
              <ul className="mt-1 text-sm text-gray-600 list-disc list-inside">
                {match.recommendations.slice(0, 3).map((rec, idx) => (
                  <li key={idx}>{rec}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Description */}
      <div className="mb-6">
        <h4 className="font-semibold mb-2">Job Description</h4>
        <div className="text-gray-700 whitespace-pre-wrap">
          {description || 'No description provided.'}
        </div>
      </div>

      {/* Skills Required */}
      {skills && (
        <div className="mb-6">
          <h4 className="font-semibold mb-2">Skills Required</h4>
          <div className="flex flex-wrap gap-2">
            {skills.split(',').map((skill, idx) => (
              <span key={idx} className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm">
                {skill.trim()}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Experience Required */}
      {experienceRequired && (
        <div className="mb-6">
          <h4 className="font-semibold mb-2">Experience Required</h4>
          <p className="text-gray-700">{experienceRequired}</p>
        </div>
      )}

      {/* Apply Button */}
      <div className="flex gap-4">
        <button
          onClick={handleApply}
          className="flex-1 bg-primary text-white py-3 px-6 rounded-md hover:bg-blue-700 transition flex items-center justify-center gap-2"
        >
          <FaExternalLinkAlt /> Apply Now
        </button>
      </div>

      {/* Apply Popup */}
      {showApplyPopup && (
        <ApplyPopup
          jobId={id}
          jobTitle={title}
          companyName={companyName}
          onClose={() => setShowApplyPopup(false)}
        />
      )}
    </div>
  );
};

export default JobDetails;