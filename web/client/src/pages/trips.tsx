import { useAuth } from '@/hooks/useAuth';
import { Navigation } from '@/components/Navigation';
import { TripList } from '@/components/TripList';
import { Button } from '@/components/ui/button';
import { Link, useLocation } from 'wouter';
import { useEffect } from 'react';

export default function Trips() {
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
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Plan Your Armenia Adventure</h2>
              <p className="text-slate-600 max-w-2xl">Discover the beauty of Armenia by creating custom trips through its historic regions and vibrant cities.</p>
            </div>
            <div className="mt-4 sm:mt-0">
              <Link href="/create-trip">
                <Button className="bg-blue-600 hover:bg-blue-700">
                  <i className="fas fa-plus mr-2"></i>Create New Trip
                </Button>
              </Link>
            </div>
          </div>
        </div>
        <TripList />
      </div>
    </div>
  );
}
