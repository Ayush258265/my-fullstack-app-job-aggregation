import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import JobList from '../components/jobs/JobList';
import JobFilters from '../components/jobs/JobFilters';
import { jobService } from '../services/jobService';
import toast from 'react-hot-toast';
import './Jobs.css';

const Jobs = () => {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    title: searchParams.get('q') || '',
    location: searchParams.get('location') || '',
    experience: searchParams.get('experience') || '',
  });

  const fetchJobs = async (filterParams) => {
    // ✅ ADD THIS DEBUG LOG
    console.log('🔍 Fetching jobs with params:', filterParams);
    
    setLoading(true);
    try {
      const params = {
        page: 0,
        size: 20,
        ...filterParams,
      };
      
      // Remove empty values
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key];
        }
      });
      
      console.log('📤 Sending request to:', '/jobs', params); // ✅ ADD THIS
      
      const response = await jobService.searchJobs(params);
      
      console.log('📥 Response received:', response); // ✅ ADD THIS
      
      const jobsData = response?.data?.data?.content || [];
      setJobs(jobsData);
      
      // Update URL params
      const urlParams = {};
      if (filterParams.title) urlParams.q = filterParams.title;
      if (filterParams.location) urlParams.location = filterParams.location;
      if (filterParams.experience) urlParams.experience = filterParams.experience;
      setSearchParams(urlParams);
      
    } catch (error) {
      console.error('❌ Failed to fetch jobs:', error); // ✅ ADD THIS
      toast.error('Failed to fetch jobs');
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log('🔄 Jobs component mounted, fetching initial jobs'); // ✅ ADD THIS
    fetchJobs(filters);
  }, []);

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
  };

  const handleSearch = (searchFilters) => {
    fetchJobs(searchFilters);
  };

  return (
    <div className="jobs-page">
      <div className="jobs-header">
        <h1 className="jobs-title">Find Your Dream Job</h1>
        {user && (
          <span className="jobs-welcome">
            👋 Welcome, {user.firstName || user.email}
          </span>
        )}
      </div>

      <JobFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onSearch={handleSearch}
      />

      <JobList jobs={jobs} loading={loading} />
    </div>
  );
};

export default Jobs;