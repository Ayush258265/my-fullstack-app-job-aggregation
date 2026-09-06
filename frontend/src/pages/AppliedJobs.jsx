import { useState, useEffect } from 'react';
import LoadingSpinner from '../components/common/LoadingSpinner';
import './AppliedJobs.css';

const AppliedJobs = () => {
  const [appliedJobs, setAppliedJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(false);
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="applied-jobs-page">
      <h1 className="applied-jobs-title">Applied Jobs</h1>

      {appliedJobs.length === 0 ? (
        <div className="applied-jobs-empty">
          <p>You haven't applied to any jobs yet.</p>
          <p>Start browsing jobs and track your applications!</p>
        </div>
      ) : (
        <div>
          {appliedJobs.map((job) => (
            <div key={job.id} className="applied-job-item">
              <h3 className="applied-job-title">{job.title}</h3>
              <p className="applied-job-company">{job.companyName}</p>
              <p className="applied-job-date">
                Applied on: {new Date(job.clickedAt).toLocaleDateString()}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AppliedJobs;
