import { useAuth } from '@/hooks/useAuth';
import { Navigation } from '@/components/Navigation';
import { TripView } from '@/components/TripView';
import { useLocation, useParams } from 'wouter';
import { useEffect } from 'react';

export default function TripViewPage() {
  const { user, loading } = useAuth();
  const [, setLocation] = useLocation();
  const params = useParams();
  const tripId = params.tripId;

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

  if (!tripId) {
    setLocation('/trips');
    return null;
  }

  return (
    <div className="min-h-screen bg-slate-50 mb-16 md:mb-0">
      <Navigation />
      <TripView tripId={tripId} />
    </div>
  );
}
