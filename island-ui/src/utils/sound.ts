type SoundType = 'success' | 'error' | 'info' | 'warning';

class SoundService {
  private ctx: AudioContext | null = null;

  private init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
  }

  private createOscillator(freq: number, type: OscillatorType, startTime: number, duration: number, volume: number) {
    if (!this.ctx) return;
    
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = type;
    osc.frequency.setValueAtTime(freq, startTime);
    
    gain.gain.setValueAtTime(volume, startTime);
    gain.gain.exponentialRampToValueAtTime(0.01, startTime + duration);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start(startTime);
    osc.stop(startTime + duration);
  }

  play(type: SoundType) {
    try {
      this.init();
      if (!this.ctx) return;
      
      const now = this.ctx.currentTime;

      switch (type) {
        case 'success':
          // Two rising notes
          this.createOscillator(523.25, 'sine', now, 0.1, 0.1); // C5
          this.createOscillator(659.25, 'sine', now + 0.08, 0.15, 0.1); // E5
          break;
          
        case 'error':
          // Low buzzing sound
          this.createOscillator(150, 'sawtooth', now, 0.2, 0.1);
          this.createOscillator(110, 'sawtooth', now + 0.05, 0.2, 0.1);
          break;
          
        case 'warning':
          // Neutral alert
          this.createOscillator(440, 'triangle', now, 0.15, 0.1);
          break;
          
        case 'info':
          // Soft click/pop
          this.createOscillator(880, 'sine', now, 0.05, 0.05);
          break;
      }
    } catch (e) {
      console.warn('Audio feedback blocked by browser policy or not supported');
    }
  }
}

export const soundService = new SoundService();
