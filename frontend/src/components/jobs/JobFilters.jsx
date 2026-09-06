import { useState, useEffect } from 'react';
import { FaSearch } from 'react-icons/fa';
import { jobService } from '../../services/jobService';
import './JobFilters.css';

const JobFilters = ({ filters, onFilterChange, onSearch }) => {
  const [localFilters, setLocalFilters] = useState(filters);
  const [filterOptions, setFilterOptions] = useState({
    locations: [],
    companies: [],
    experienceLevels: [],
  });
  const [loading, setLoading] = useState(false);

  const fallbackExperienceLevels = ['Fresher', '1-4 years', '4-10 years', '10+ years'];

  useEffect(() => {
    const fetchFilters = async () => {
      setLoading(true);
      try {
        const response = await jobService.getFilters();
        const data = response.data.data;
        setFilterOptions({
          locations: data.locations || [],
          companies: data.companies || [],
          experienceLevels: data.experienceLevels || [],
        });
      } catch (error) {
        console.error('Failed to fetch filter options:', error);
        setFilterOptions({
          locations: ['Remote', 'Bangalore', 'Mumbai', 'Delhi', 'Hyderabad', 'Pune', 'Chennai'],
          companies: ['TechCorp', 'StartupX', 'InnovateLabs', 'DataFlow', 'CloudMasters'],
          experienceLevels: fallbackExperienceLevels,
        });
      } finally {
        setLoading(false);
      }
    };
    fetchFilters();
  }, []);

  const handleFilterChange = (key, value) => {
    const newFilters = { ...localFilters, [key]: value };
    setLocalFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const cleanedFilters = {};
    Object.keys(localFilters).forEach(key => {
      if (localFilters[key] && localFilters[key].trim() !== '') {
        cleanedFilters[key] = localFilters[key];
      }
    });
    onSearch(cleanedFilters);
  };

  return (
    <div className="job-filters">
      <form onSubmit={handleSubmit} className="job-filters-form">
        <div className="job-filters-group">
          <label>Search</label>
          <div className="search-wrapper">
            <FaSearch className="icon" />
            <input
              type="text"
              placeholder="Job title, skills..."
              className="search-input"
              value={localFilters.title || ''}
              onChange={(e) => handleFilterChange('title', e.target.value)}
            />
          </div>
        </div>

        <div className="job-filters-group">
          <label>Location</label>
          <select
            value={localFilters.location || ''}
            onChange={(e) => handleFilterChange('location', e.target.value)}
          >
            <option value="">All Locations</option>
            {filterOptions.locations.map((loc) => (
              <option key={loc} value={loc}>{loc}</option>
            ))}
          </select>
        </div>

        <div className="job-filters-group">
          <label>Experience</label>
          <select
            value={localFilters.experience || ''}
            onChange={(e) => handleFilterChange('experience', e.target.value)}
          >
            <option value="">All Levels</option>
            {filterOptions.experienceLevels.map((experienceLevel) => (
              <option key={experienceLevel} value={experienceLevel}>{experienceLevel}</option>
            ))}
          </select>
        </div>

        <div className="job-filters-group job-filters-action">
          <button type="submit" className="job-filters-btn">
            <FaSearch /> Search Jobs
          </button>
        </div>
      </form>
    </div>
  );
};

export default JobFilters;