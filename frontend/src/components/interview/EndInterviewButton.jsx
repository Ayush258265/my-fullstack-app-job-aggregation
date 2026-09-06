import { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import './EndInterviewButton.css';

const EndInterviewButton = ({ onEnd }) => {
  const [showConfirm, setShowConfirm] = useState(false);
  const [isEnding, setIsEnding] = useState(false);

  const handleClick = () => {
    setShowConfirm(true);
  };

  const handleConfirm = async () => {
    setIsEnding(true);
    setShowConfirm(false);
    try {
      await onEnd();
    } catch (error) {
      console.error('Error ending interview:', error);
    } finally {
      setIsEnding(false);
    }
  };

  const handleCancel = () => {
    setShowConfirm(false);
  };

  return (
    <>
      <button
        onClick={handleClick}
        disabled={isEnding}
        className="flex items-center gap-1.5 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-all text-sm font-semibold disabled:opacity-50"
      >
        <FaTimes size={14} />
        {isEnding ? 'Ending...' : 'End Interview'}
      </button>

      {showConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-2">End Interview?</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to end this interview? 
              Your progress will be saved and you'll see a summary of answered questions.
            </p>
            <div className="flex gap-3">
              <button
                onClick={handleConfirm}
                disabled={isEnding}
                className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 font-semibold transition-all disabled:opacity-50"
              >
                {isEnding ? 'Ending...' : 'Yes, End'}
              </button>
              <button
                onClick={handleCancel}
                className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-semibold transition-all"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default EndInterviewButton;