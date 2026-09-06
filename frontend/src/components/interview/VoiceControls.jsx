import { useState, useEffect } from 'react';
import { FaMicrophone, FaStop, FaMicrophoneSlash } from 'react-icons/fa';

// Speech Recognition
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

const VoiceControls = ({ onTranscript, isListening: parentListening }) => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [recognition, setRecognition] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!SpeechRecognition) {
      setError('Speech recognition not supported in this browser.');
      return;
    }

    const recognitionInstance = new SpeechRecognition();
    recognitionInstance.continuous = true;
    recognitionInstance.interimResults = true;
    recognitionInstance.lang = 'en-US';

    recognitionInstance.onresult = (event) => {
      let finalTranscript = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalTranscript += transcript;
        }
      }
      if (finalTranscript) {
        setTranscript(finalTranscript);
        onTranscript(finalTranscript);
      }
    };

    recognitionInstance.onerror = (event) => {
      console.error('Speech recognition error:', event.error);
      if (event.error !== 'network') {
        setError(event.error);
      } else {
        setError('Check your internet connection');
      }
      setIsListening(false);
    };

    recognitionInstance.onend = () => {
      setIsListening(false);
    };

    setRecognition(recognitionInstance);

    return () => {
      if (recognitionInstance) {
        recognitionInstance.stop();
      }
    };
  }, [onTranscript]);

  const startListening = () => {
    if (!recognition) {
      setError('Speech recognition not initialized.');
      return;
    }
    setError(null);
    setTranscript('');
    setIsListening(true);
    recognition.start();
  };

  const stopListening = () => {
    if (recognition) {
      recognition.stop();
    }
    setIsListening(false);
  };

  const toggleListening = () => {
    if (isListening) {
      stopListening();
    } else {
      startListening();
    }
  };

  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        onClick={toggleListening}
        disabled={!!error}
        className={`flex items-center gap-2 px-3 py-2 rounded-lg font-semibold transition-all ${
          isListening
            ? 'bg-red-500 text-white animate-pulse'
            : error
            ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
            : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
        }`}
      >
        {isListening ? (
          <>
            <FaMicrophoneSlash /> Stop
          </>
        ) : (
          <>
            <FaMicrophone /> Speak
          </>
        )}
      </button>
      {error && (
        <span className="text-xs text-red-500">{error}</span>
      )}
      {isListening && (
        <span className="text-xs text-green-500 animate-pulse font-semibold">
          🔴 Listening...
        </span>
      )}
      {transcript && !isListening && (
        <span className="text-xs text-gray-400 truncate max-w-[100px]">
          {transcript}
        </span>
      )}
    </div>
  );
};

export default VoiceControls;