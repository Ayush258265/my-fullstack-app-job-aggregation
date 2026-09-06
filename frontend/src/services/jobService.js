import api from './api';

export const jobService = {
  // Search jobs with filters
  searchJobs: (params) => {
    // Remove empty params
    const cleanParams = {};
    Object.keys(params).forEach(key => {
      if (params[key] && params[key].trim && params[key].trim() !== '') {
        cleanParams[key] = params[key];
      } else if (params[key] && typeof params[key] !== 'string') {
        cleanParams[key] = params[key];
      }
    });
    return api.get('/jobs', { params: cleanParams });
  },
  
  // Get job by ID
  getJobById: (id) => api.get(`/jobs/${id}`),
  
  // Get filter options
  getFilters: () => api.get('/jobs/filters'),
  
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