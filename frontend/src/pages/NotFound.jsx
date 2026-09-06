import { Link } from 'react-router-dom';
import './NotFound.css';

const NotFound = () => {
  return (
    <div className="not-found">
      <h1 className="not-found-number">404</h1>
      <h2 className="not-found-title">Page Not Found</h2>
      <p className="not-found-text">The page you're looking for doesn't exist.</p>
      <Link to="/" className="not-found-btn">
        Go Home
      </Link>
    </div>
  );
};

export default NotFound;
