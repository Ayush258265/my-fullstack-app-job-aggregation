import api from './api';
// import { jobService } from '../services/jobService';

export const jobService = {
  searchJobs: (params) => {
    console.log('🔍 jobService.searchJobs called with:', params);
    const cleanParams = {};
    Object.keys(params).forEach(key => {
      if (params[key] && params[key].trim && params[key].trim() !== '') {
        cleanParams[key] = params[key];
      } else if (params[key] && typeof params[key] !== 'string') {
        cleanParams[key] = params[key];
      }
    });
    console.log('📤 Making API call to /jobs with:', cleanParams);
    return api.get('/jobs', { params: cleanParams });  // ✅ Uses api from api.js
  },

  // Get job by ID
  getJobById: (id) => api.get(`/jobs/${id}`),

  // Get filter options
  getFilters: () => {
    console.log('🔍 Fetching filter options'); // ✅ ADD THIS
    return api.get('/jobs/filters');
  },

  // Track apply click
  trackApply: (data) => api.post('/user/apply', data),

  // Get applied jobs
  getAppliedJobs: (params) => api.get('/user/applied', { params }),

  // Get not-applied jobs
  getNotAppliedJobs: (params) => api.get('/user/not-applied', { params }),

  // Check if job is tracked
  isJobTracked: (jobId) => api.get(`/user/tracked/${jobId}`),

  // Get applied count
  getAppliedCount: () => api.get('/user/applied/count'),
};