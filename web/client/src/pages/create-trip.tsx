import { useAuth } from '@/hooks/useAuth';
import { Navigation } from '@/components/Navigation';
import { TripCreate } from '@/components/TripCreate';
import { useLocation } from 'wouter';
import { useEffect } from 'react';

export default function CreateTrip() {
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
      <TripCreate />
    </div>
  );
}
