import { useState, useEffect } from 'react';
import { FaVolumeUp, FaVolumeMute } from 'react-icons/fa';

const VoiceToggle = ({ isPlaying, onToggle, isEnabled }) => {
  return (
    <button
      onClick={onToggle}
      className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-all ${
        isEnabled
          ? 'bg-green-100 text-green-700 hover:bg-green-200'
          : 'bg-gray-100 text-gray-400 hover:bg-gray-200'
      }`}
      title={isEnabled ? 'Voice is ON' : 'Voice is OFF'}
    >
      {isEnabled ? (
        <>
          <FaVolumeUp className={isPlaying ? 'animate-pulse' : ''} />
          <span className="text-sm font-medium">
            {isPlaying ? '🔊 Playing...' : 'Voice ON'}
          </span>
        </>
      ) : (
        <>
          <FaVolumeMute />
          <span className="text-sm font-medium">Voice OFF</span>
        </>
      )}
    </button>
  );
};

export default VoiceToggle;