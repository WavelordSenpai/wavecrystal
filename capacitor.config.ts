import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.wavecrystal.app',
  appName: 'Wave Crystal',
  webDir: '.',
  server: {
    androidScheme: 'https'
  }
};

export default config;
