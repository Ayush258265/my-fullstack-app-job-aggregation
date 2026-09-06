// Speech Synthesis (Text-to-Speech) Service
class SpeechService {
  constructor() {
    this.synthesis = window.speechSynthesis;
    this.utterance = null;
    this.isSpeaking = false;
    this.isPaused = false;
    this.voicesLoaded = false;
    this.englishVoice = null;
    this.voiceLoadAttempts = 0;
    this.maxVoiceLoadAttempts = 10;
    this.voiceLoadInterval = null;

    // ✅ Load voices when available
    if (this.isSupported()) {
      this.loadVoices();
      // Some browsers need this event
      if (this.synthesis.onvoiceschanged !== undefined) {
        this.synthesis.onvoiceschanged = () => {
          this.loadVoices();
        };
      }
      // Fallback: keep trying to load voices
      this.voiceLoadInterval = setInterval(() => {
        if (!this.voicesLoaded && this.voiceLoadAttempts < this.maxVoiceLoadAttempts) {
          this.loadVoices();
        } else if (this.voicesLoaded) {
          clearInterval(this.voiceLoadInterval);
        }
      }, 1000);
    }
  }

  /**
   * ✅ Load and cache English voices
   */
  loadVoices() {
    if (!this.isSupported()) return;
    
    const voices = this.synthesis.getVoices();
    this.voiceLoadAttempts++;
    
    if (voices.length === 0) {
      console.log(`⏳ Loading voices... attempt ${this.voiceLoadAttempts}`);
      return;
    }
    
    console.log('🔊 Available voices:', voices.length);
    
    // ✅ Find best English voice
    this.englishVoice = this.findBestEnglishVoice(voices);
    if (this.englishVoice) {
      console.log('✅ Selected English voice:', this.englishVoice.name);
      this.voicesLoaded = true;
      clearInterval(this.voiceLoadInterval);
    } else {
      console.warn('⚠️ No English voice found, using default');
    }
  }

  /**
   * ✅ Find the best English voice
   */
  findBestEnglishVoice(voices) {
    // Priority order:
    // 1. Google UK/US English (most natural)
    // 2. Any English voice with 'Google' in name
    // 3. Microsoft/Amazon voices
    // 4. Any English voice
    // 5. Default voice
    
    const priorityList = [
      // Google voices (most natural)
      (v) => v.lang.startsWith('en') && v.name.includes('Google'),
      // Microsoft voices
      (v) => v.lang.startsWith('en') && (v.name.includes('Microsoft') || v.name.includes('David') || v.name.includes('Zira')),
      // Amazon voices
      (v) => v.lang.startsWith('en') && v.name.includes('Amazon'),
      // Any English voice
      (v) => v.lang.startsWith('en'),
    ];

    for (const condition of priorityList) {
      const found = voices.find(condition);
      if (found) return found;
    }

    return null;
  }

  /**
   * Check if speech synthesis is supported
   */
  isSupported() {
    return 'speechSynthesis' in window && window.speechSynthesis !== null;
  }

  /**
   * Get available voices
   */
  getVoices() {
    if (!this.isSupported()) return [];
    return this.synthesis.getVoices();
  }

  /**
   * Speak text using Text-to-Speech
   */
  speak(text, options = {}) {
    // Stop any ongoing speech
    this.stop();

    // Check if speech synthesis is available
    if (!this.isSupported()) {
      console.warn('Speech synthesis not supported in this browser');
      if (options.onError) options.onError('Speech synthesis not supported');
      return;
    }

    // ✅ Ensure voices are loaded
    if (!this.voicesLoaded) {
      this.loadVoices();
    }

    // Check if text is valid
    if (!text || text.trim().length === 0) {
      console.warn('No text to speak');
      if (options.onError) options.onError('No text to speak');
      return;
    }

    // Clean the text
    const cleanText = text
      .replace(/[#*_`]/g, '')
      .replace(/\s+/g, ' ')
      .trim();

    if (cleanText.length === 0) {
      console.warn('Text became empty after cleaning');
      if (options.onError) options.onError('Text became empty');
      return;
    }

    // Create utterance
    this.utterance = new SpeechSynthesisUtterance(cleanText);
    
    // ✅ Set language to English (US)
    this.utterance.lang = options.lang || 'en-US';
    this.utterance.rate = options.rate || 0.9;  // Slightly slower for clarity
    this.utterance.pitch = options.pitch || 1.0;
    this.utterance.volume = options.volume || 1.0;

    // ✅ Use the best English voice if available
    if (options.voice) {
      this.utterance.voice = options.voice;
    } else if (this.englishVoice) {
      this.utterance.voice = this.englishVoice;
      console.log('🔊 Using voice:', this.englishVoice.name);
    } else {
      // ✅ Fallback: find any English voice
      const voices = this.getVoices();
      const fallbackVoice = voices.find(v => v.lang.startsWith('en'));
      if (fallbackVoice) {
        this.utterance.voice = fallbackVoice;
        console.log('🔊 Using fallback English voice:', fallbackVoice.name);
      } else {
        console.warn('⚠️ No English voice found, using default system voice');
      }
    }

    // Event listeners
    this.utterance.onstart = () => {
      this.isSpeaking = true;
      this.isPaused = false;
      console.log('🔊 Speaking...');
      if (options.onStart) options.onStart();
    };

    this.utterance.onend = () => {
      this.isSpeaking = false;
      this.isPaused = false;
      console.log('🔊 Speech ended');
      if (options.onEnd) options.onEnd();
    };

    this.utterance.onpause = () => {
      this.isPaused = true;
      console.log('⏸️ Speech paused');
      if (options.onPause) options.onPause();
    };

    this.utterance.onresume = () => {
      this.isPaused = false;
      console.log('▶️ Speech resumed');
      if (options.onResume) options.onResume();
    };

    this.utterance.onerror = (event) => {
      this.isSpeaking = false;
      this.isPaused = false;
      console.error('Speech error:', event);
      if (options.onError) options.onError(event);
    };

    // ✅ Use a small delay to ensure browser is ready
    setTimeout(() => {
      if (this.synthesis) {
        this.synthesis.speak(this.utterance);
      }
    }, 100);
  }

  /**
   * Stop speaking
   */
  stop() {
    if (this.synthesis) {
      try {
        this.synthesis.cancel();
      } catch (e) {
        console.warn('Error stopping speech:', e);
      }
    }
    this.isSpeaking = false;
    this.isPaused = false;
  }

  /**
   * Pause speaking
   */
  pause() {
    if (this.synthesis && this.isSpeaking) {
      try {
        this.synthesis.pause();
      } catch (e) {
        console.warn('Error pausing speech:', e);
      }
    }
  }

  /**
   * Resume speaking
   */
  resume() {
    if (this.synthesis && this.isPaused) {
      try {
        this.synthesis.resume();
      } catch (e) {
        console.warn('Error resuming speech:', e);
      }
    }
  }

  /**
   * Check if currently speaking
   */
  isSpeakingNow() {
    return this.isSpeaking;
  }

  /**
   * Check if paused
   */
  isPausedNow() {
    return this.isPaused;
  }

  /**
   * ✅ Get available English voices (for debugging)
   */
  getEnglishVoices() {
    if (!this.isSupported()) return [];
    const voices = this.getVoices();
    return voices.filter(v => v.lang && v.lang.startsWith('en'));
  }

  /**
   * ✅ Set a specific voice by name
   */
  setVoiceByName(voiceName) {
    if (!this.isSupported()) return false;
    const voices = this.getVoices();
    const found = voices.find(v => v.name === voiceName);
    if (found) {
      this.englishVoice = found;
      this.voicesLoaded = true;
      console.log('✅ Voice set to:', found.name);
      return true;
    }
    console.warn('⚠️ Voice not found:', voiceName);
    return false;
  }

  /**
   * ✅ List all available voices (for debugging)
   */
  listAllVoices() {
    if (!this.isSupported()) {
      console.warn('Speech synthesis not supported');
      return [];
    }
    const voices = this.getVoices();
    console.log('📢 ALL AVAILABLE VOICES:');
    voices.forEach((voice, index) => {
      console.log(`${index + 1}. ${voice.name} (${voice.lang}) - ${voice.default ? 'DEFAULT' : ''}`);
    });
    return voices;
  }

  /**
   * ✅ Check if voices are loaded
   */
  isVoicesLoaded() {
    return this.voicesLoaded;
  }

  /**
   * ✅ Get current voice
   */
  getCurrentVoice() {
    return this.englishVoice;
  }

  /**
   * ✅ Speak with retry if voice not loaded
   */
  speakWithRetry(text, options = {}, maxRetries = 3) {
    let attempts = 0;
    
    const trySpeak = () => {
      attempts++;
      if (this.voicesLoaded || attempts > maxRetries) {
        this.speak(text, options);
      } else {
        console.log(`⏳ Waiting for voices to load... attempt ${attempts}`);
        setTimeout(() => {
          this.loadVoices();
          trySpeak();
        }, 500);
      }
    };
    
    trySpeak();
  }
}

// Singleton instance
const speechService = new SpeechService();

// ✅ Expose to window for debugging
if (typeof window !== 'undefined') {
  window.speechService = speechService;
}

export default speechService;