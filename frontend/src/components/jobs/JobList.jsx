import JobCard from './JobCard';
import LoadingSpinner from '../common/LoadingSpinner';

const JobList = ({ jobs, loading, onLoadMore, hasMore }) => {
  // ✅ Check if jobs exists
  if (loading && (!jobs || jobs.length === 0)) {
    return <LoadingSpinner />;
  }

  if (!jobs || jobs.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-12 text-center text-gray-500">
        <h3 className="text-xl font-semibold text-gray-400">No jobs found</h3>
        <p className="mt-2">Try adjusting your search filters.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="text-sm text-gray-500 mb-4">
        Found {jobs.length} jobs
      </div>
      {jobs.map((job) => (
        <JobCard key={job.id} job={job} />
      ))}
      {loading && <LoadingSpinner />}
      {hasMore && !loading && (
        <div className="text-center mt-6">
          <button
            onClick={onLoadMore}
            className="text-primary hover:text-blue-700 font-medium"
          >
            Load More Jobs
          </button>
        </div>
      )}
    </div>
  );
};

export default JobList;