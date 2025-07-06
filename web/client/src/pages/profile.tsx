import { useAuth } from '@/hooks/useAuth';
import { Navigation } from '@/components/Navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useTrips } from '@/hooks/useTrips';
import { useLocation } from 'wouter';
import { useEffect } from 'react';

export default function Profile() {
  const { user, userProfile, loading, logout } = useAuth();
  const { trips } = useTrips();
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

  if (!user || !userProfile) {
    return null; // Will redirect in useEffect
  }

  const draftTrips = trips.filter(trip => trip.status === 'draft').length;
  const finalizedTrips = trips.filter(trip => trip.status === 'finalized').length;

  return (
    <div className="min-h-screen bg-slate-50 mb-16 md:mb-0">
      <Navigation />
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-slate-900 mb-2">Profile</h2>
          <p className="text-slate-600">Manage your account and view your Armenia exploration progress.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Profile Info */}
          <div className="lg:col-span-1">
            <Card>
              <CardHeader>
                <CardTitle>Profile Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col items-center space-y-4">
                  <Avatar className="h-20 w-20">
                    <AvatarFallback className="text-2xl">
                      {userProfile.username.slice(0, 2).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <div className="text-center">
                    <h3 className="text-lg font-semibold">{userProfile.username}</h3>
                    <p className="text-slate-600">{userProfile.email}</p>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Member since:</span>
                    <span className="font-medium">
                      {new Date(userProfile.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Total trips:</span>
                    <span className="font-medium">{trips.length}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Draft trips:</span>
                    <span className="font-medium">{draftTrips}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Finalized trips:</span>
                    <span className="font-medium">{finalizedTrips}</span>
                  </div>
                </div>

                <Button
                  onClick={logout}
                  variant="outline"
                  className="w-full text-red-600 hover:text-red-700 hover:bg-red-50"
                >
                  <i className="fas fa-sign-out-alt mr-2"></i>
                  Sign Out
                </Button>
              </CardContent>
            </Card>
          </div>

          {/* Statistics */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Your Armenia Explorer Stats</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                  <div className="text-center">
                    <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                      <i className="fas fa-route text-blue-600 text-xl"></i>
                    </div>
                    <div className="text-2xl font-bold text-slate-900">{trips.length}</div>
                    <div className="text-sm text-slate-600">Total Trips</div>
                  </div>

                  <div className="text-center">
                    <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                      <i className="fas fa-check-circle text-emerald-600 text-xl"></i>
                    </div>
                    <div className="text-2xl font-bold text-slate-900">{finalizedTrips}</div>
                    <div className="text-sm text-slate-600">Completed</div>
                  </div>

                  <div className="text-center">
                    <div className="w-12 h-12 bg-amber-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                      <i className="fas fa-edit text-amber-600 text-xl"></i>
                    </div>
                    <div className="text-2xl font-bold text-slate-900">{draftTrips}</div>
                    <div className="text-sm text-slate-600">In Planning</div>
                  </div>

                  <div className="text-center">
                    <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                      <i className="fas fa-map-marker-alt text-purple-600 text-xl"></i>
                    </div>
                    <div className="text-2xl font-bold text-slate-900">
                      {[...new Set(trips.flatMap(trip => trip.cities))].length}
                    </div>
                    <div className="text-sm text-slate-600">Cities Visited</div>
                  </div>
                </div>

                {trips.length > 0 && (
                  <div className="mt-8">
                    <h4 className="text-lg font-semibold mb-4">Recent Activity</h4>
                    <div className="space-y-3">
                      {trips.slice(0, 3).map((trip) => (
                        <div key={trip.tripId} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                          <div>
                            <div className="font-medium text-slate-900">{trip.title}</div>
                            <div className="text-sm text-slate-600">
                              {trip.cities.length} cities • {trip.status}
                            </div>
                          </div>
                          <div className="text-sm text-slate-500">
                            {new Date(trip.createdAt).toLocaleDateString()}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
