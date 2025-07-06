import { useAuth } from '@/hooks/useAuth';
import { Navigation } from '@/components/Navigation';
import { Chat } from '@/components/Chat';
import { useLocation } from 'wouter';
import { useEffect } from 'react';

export default function ChatPage() {
  const { user, loading } = useAuth();
  const [, setLocation] = useLocation();

  useEffect(() => {
    if (!loading && !user) {
      setLocation('/login');
    }
  }, [user, loading, setLocation]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!user) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className="min-h-screen bg-slate-50 mb-16 md:mb-0">
      <Navigation />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-slate-900 mb-2">Chat</h2>
          <p className="text-slate-600">Connect with fellow Armenia explorers and share your experiences.</p>
        </div>
        <Chat />
      </div>
    </div>
  );
}
