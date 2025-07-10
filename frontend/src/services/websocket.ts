import { io, Socket } from 'socket.io-client';

class WebSocketService {
  private socket: Socket | null = null;
  private listeners: Map<string, Function[]> = new Map();

  connect(token: string): void {
    if (this.socket?.connected) {
      return;
    }

    const wsUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8080';
    
    this.socket = io(wsUrl, {
      auth: {
        token: token,
      },
      transports: ['websocket'],
      path: '/ws',
    });

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
    });

    this.socket.on('disconnect', () => {
      console.log('WebSocket disconnected');
    });

    this.socket.on('error', (error) => {
      console.error('WebSocket error:', error);
    });
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
    this.listeners.clear();
  }

  subscribeTo<T>(event: string, callback: (data: T) => void): () => void {
    if (!this.socket) {
      console.error('WebSocket not connected');
      return () => {};
    }

    // Add listener
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);

    // Subscribe to the event
    this.socket.on(event, callback);

    // Return unsubscribe function
    return () => {
      if (this.socket) {
        this.socket.off(event, callback);
      }
      const listeners = this.listeners.get(event);
      if (listeners) {
        const index = listeners.indexOf(callback);
        if (index > -1) {
          listeners.splice(index, 1);
        }
      }
    };
  }

  subscribeToMission(missionId: string, callback: (data: any) => void): () => void {
    return this.subscribeTo(`/topic/missions/${missionId}`, callback);
  }

  subscribeToTelemetry(missionId: string, callback: (data: any) => void): () => void {
    return this.subscribeTo(`/topic/missions/${missionId}/telemetry`, callback);
  }

  sendMissionProgress(missionId: string, progress: any): void {
    if (!this.socket) {
      console.error('WebSocket not connected');
      return;
    }
    this.socket.emit(`/app/missions/${missionId}/progress`, progress);
  }

  sendTelemetryData(missionId: string, telemetry: any): void {
    if (!this.socket) {
      console.error('WebSocket not connected');
      return;
    }
    this.socket.emit(`/app/missions/${missionId}/telemetry`, telemetry);
  }

  isConnected(): boolean {
    return this.socket?.connected || false;
  }
}

const webSocketService = new WebSocketService();

export const connectWebSocket = (token: string) => webSocketService.connect(token);
export const disconnectWebSocket = () => webSocketService.disconnect();
export const subscribeToMission = (missionId: string, callback: (data: any) => void) => 
  webSocketService.subscribeToMission(missionId, callback);
export const subscribeToTelemetry = (missionId: string, callback: (data: any) => void) => 
  webSocketService.subscribeToTelemetry(missionId, callback);
export const sendMissionProgress = (missionId: string, progress: any) => 
  webSocketService.sendMissionProgress(missionId, progress);
export const sendTelemetryData = (missionId: string, telemetry: any) => 
  webSocketService.sendTelemetryData(missionId, telemetry);

export default webSocketService;