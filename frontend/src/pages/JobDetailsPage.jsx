import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import JobDetails from '../components/jobs/JobDetails';
import { jobService } from '../services/jobService';
import { FaArrowLeft } from 'react-icons/fa';
import toast from 'react-hot-toast';
import './JobDetailsPage.css';

const JobDetailsPage = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchJob = async () => {
      setLoading(true);
      try {
        const response = await jobService.getJobById(id);
        setJob(response.data.data);
      } catch (error) {
        toast.error('Failed to load job details');
      } finally {
        setLoading(false);
      }
    };
    fetchJob();
  }, [id]);

  return (
    <div className="job-details-page">
      <Link to="/jobs" className="job-details-back">
        <FaArrowLeft /> Back to Jobs
      </Link>

      <JobDetails job={job} loading={loading} />
    </div>
  );
};

export default JobDetailsPage;