import { Link } from 'react-router-dom';
import { FaMapMarker, FaBuilding, FaCalendar } from 'react-icons/fa';

const JobCard = ({ job }) => {
  // ✅ Add null check
  if (!job) {
    return null;
  }

  const { id, title, companyName, location, postedDate, match } = job;

  const getMatchColor = (percentage) => {
    if (percentage >= 70) return 'bg-green-100 text-green-800';
    if (percentage >= 40) return 'bg-yellow-100 text-yellow-800';
    return 'bg-red-100 text-red-800';
  };

  const formatDate = (date) => {
    if (!date) return 'Recently';
    try {
      return new Date(date).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return 'Recently';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6 border border-gray-100">
      <div className="flex justify-between items-start">
        <div className="flex-1">
          <Link to={`/jobs/${id}`}>
            <h3 className="text-xl font-semibold text-gray-900 hover:text-primary transition">
              {title || 'Untitled Position'}
            </h3>
          </Link>
          <div className="flex items-center text-gray-600 mt-1">
            <FaBuilding className="mr-2 text-gray-400" />
            <span>{companyName || 'Unknown Company'}</span>
          </div>
          <div className="flex items-center text-gray-600 mt-1">
            <FaMapMarker className="mr-2 text-gray-400" />
            <span>{location || 'Remote'}</span>
          </div>
          <div className="flex items-center text-gray-500 text-sm mt-2">
            <FaCalendar className="mr-2 text-gray-400" />
            <span>Posted: {formatDate(postedDate)}</span>
          </div>
        </div>

        {match && match.matchPercentage !== null && (
          <div className="ml-4 flex-shrink-0">
            <div className={`px-4 py-2 rounded-full text-sm font-semibold ${getMatchColor(match.matchPercentage)}`}>
              {match.matchPercentage}% Match
            </div>
          </div>
        )}
      </div>

      {match && match.matchingSkills && (
        <div className="mt-4 pt-4 border-t border-gray-100">
          {match.matchingSkills.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-2">
              <span className="text-sm font-medium text-gray-700">✅ Your Skills:</span>
              {match.matchingSkills.map((skill, idx) => (
                <span key={idx} className="text-sm bg-green-100 text-green-800 px-2 py-1 rounded">
                  {skill}
                </span>
              ))}
            </div>
          )}
          {match.missingSkills && match.missingSkills.length > 0 && (
            <div className="flex flex-wrap gap-2">
              <span className="text-sm font-medium text-gray-700">⚠️ Missing:</span>
              {match.missingSkills.slice(0, 5).map((skill, idx) => (
                <span key={idx} className="text-sm bg-red-100 text-red-800 px-2 py-1 rounded">
                  {skill}
                </span>
              ))}
              {match.missingSkills.length > 5 && (
                <span className="text-sm text-gray-500">+{match.missingSkills.length - 5} more</span>
              )}
            </div>
          )}
          {match.experienceStatus && match.experienceStatus !== 'Not Specified' && (
            <div className="mt-2 text-sm text-gray-600">
              {match.experienceStatus}
            </div>
          )}
        </div>
      )}

      <div className="mt-4 flex justify-end">
        <Link
          to={`/jobs/${id}`}
          className="text-primary hover:text-blue-700 font-medium text-sm"
        >
          View Details →
        </Link>
      </div>
    </div>
  );
};

export default JobCard;