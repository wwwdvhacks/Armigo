import { useState, useEffect } from 'react';
import { useLocation } from 'wouter';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { useTrips, Trip } from '@/hooks/useTrips';
import { Map } from './Map';

interface TripViewProps {
  tripId: string;
}

export function TripView({ tripId }: TripViewProps) {
  const [, setLocation] = useLocation();
  const [trip, setTrip] = useState<Trip | null>(null);
  const [loading, setLoading] = useState(true);
  const { getTrip, updateTrip, optimizeTrip } = useTrips();
  const { toast } = useToast();

  useEffect(() => {
    const loadTrip = async () => {
      setLoading(true);
      try {
        const tripData = await getTrip(tripId);
        setTrip(tripData);
      } catch (error: any) {
        toast({
          title: 'Error',
          description: 'Failed to load trip',
          variant: 'destructive'
        });
      } finally {
        setLoading(false);
      }
    };

    loadTrip();
  }, [tripId, getTrip, toast]);

  const handleOptimizeRoute = async () => {
    if (!trip) return;

    const result = optimizeTrip(trip.cities);
    
    try {
      await updateTrip(trip.tripId, { cities: result.cities });
      setTrip(prev => prev ? { ...prev, cities: result.cities } : null);
      
      toast({
        title: 'Route Optimized',
        description: result.benefits
      });
    } catch (error: any) {
      toast({
        title: 'Error',
        description: 'Failed to optimize route',
        variant: 'destructive'
      });
    }
  };

  const handleFinalizeRoute = async () => {
    if (!trip) return;

    try {
      await updateTrip(trip.tripId, { status: 'finalized' });
      setTrip(prev => prev ? { ...prev, status: 'finalized' } : null);
      
      toast({
        title: 'Route Finalized',
        description: 'Your trip route has been finalized!'
      });
    } catch (error: any) {
      toast({
        title: 'Error',
        description: 'Failed to finalize route',
        variant: 'destructive'
      });
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-slate-200 rounded w-1/3 mb-4"></div>
          <div className="h-4 bg-slate-200 rounded w-1/2 mb-8"></div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-1">
              <div className="h-64 bg-slate-200 rounded-lg"></div>
            </div>
            <div className="lg:col-span-2">
              <div className="h-96 bg-slate-200 rounded-lg"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!trip) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Card>
          <CardContent className="pt-6 text-center">
            <i className="fas fa-exclamation-triangle text-4xl text-amber-500 mb-4"></i>
            <h3 className="text-lg font-semibold mb-2">Trip Not Found</h3>
            <p className="text-slate-600 mb-4">The requested trip could not be found.</p>
            <Button onClick={() => setLocation('/trips')}>
              <i className="fas fa-arrow-left mr-2"></i>Back to Trips
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center space-x-2 mb-4">
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => setLocation('/trips')}
          >
            <i className="fas fa-arrow-left mr-2"></i>Back to Trips
          </Button>
        </div>
        
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-3xl font-bold text-slate-900 mb-2">{trip.title}</h2>
            <div className="flex items-center space-x-4 text-slate-600">
              <span>{trip.cities.length} cities</span>
              <span>•</span>
              <span>Created {new Date(trip.createdAt).toLocaleDateString()}</span>
              <Badge variant={trip.status === 'finalized' ? 'default' : 'secondary'}>
                {trip.status}
              </Badge>
            </div>
            {trip.description && (
              <p className="text-slate-600 mt-2">{trip.description}</p>
            )}
          </div>
          
          <div className="mt-4 sm:mt-0 flex space-x-3">
            {trip.status === 'draft' && (
              <>
                <Button
                  onClick={handleOptimizeRoute}
                  className="bg-amber-500 hover:bg-amber-600"
                  disabled={trip.cities.length < 2}
                >
                  <i className="fas fa-magic mr-2"></i>Optimize Route
                </Button>
                <Button
                  onClick={handleFinalizeRoute}
                  className="bg-emerald-600 hover:bg-emerald-700"
                >
                  <i className="fas fa-check mr-2"></i>Finalize Route
                </Button>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Trip Info Panel */}
        <div className="lg:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle>Trip Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <h4 className="text-sm font-medium text-slate-700 mb-2">Cities in Order</h4>
                <div className="space-y-2">
                  {trip.cities.map((city, index) => (
                    <div key={`${city}-${index}`} className="flex items-center space-x-3 p-2 bg-slate-50 rounded">
                      <div className="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-medium">
                        {index + 1}
                      </div>
                      <span className="font-medium text-slate-900">{city}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h4 className="text-sm font-medium text-slate-700 mb-2">Trip Statistics</h4>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-slate-600">Total Cities:</span>
                    <span className="font-medium">{trip.cities.length}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600">Status:</span>
                    <Badge variant={trip.status === 'finalized' ? 'default' : 'secondary'} className="text-xs">
                      {trip.status}
                    </Badge>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600">Created:</span>
                    <span className="font-medium">{new Date(trip.createdAt).toLocaleDateString()}</span>
                  </div>
                  {trip.updatedAt && (
                    <div className="flex justify-between">
                      <span className="text-slate-600">Updated:</span>
                      <span className="font-medium">{new Date(trip.updatedAt).toLocaleDateString()}</span>
                    </div>
                  )}
                </div>
              </div>

              {trip.status === 'finalized' && (
                <div className="p-3 bg-emerald-50 rounded-lg border border-emerald-200">
                  <div className="flex items-center space-x-2">
                    <i className="fas fa-check-circle text-emerald-600"></i>
                    <span className="text-sm font-medium text-emerald-800">Route Finalized</span>
                  </div>
                  <p className="text-xs text-emerald-700 mt-1">
                    Your trip route is complete and optimized for travel.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Map View */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Trip Route Map</CardTitle>
            </CardHeader>
            <CardContent>
              <Map
                selectedCities={trip.cities}
                onCityClick={() => {}} // View-only mode
                showRoute={trip.status === 'finalized'}
                className="h-96"
              />
              
              {/* Map Legend */}
              <div className="mt-4 flex flex-wrap items-center justify-between text-sm">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <div className="w-3 h-3 bg-blue-600 rounded-full"></div>
                    <span className="text-slate-600">Trip Cities</span>
                  </div>
                  {trip.status === 'finalized' && (
                    <div className="flex items-center space-x-2">
                      <div className="w-3 h-0.5 bg-blue-600"></div>
                      <span className="text-slate-600">Route Path</span>
                    </div>
                  )}
                </div>
                <div className="text-slate-500">
                  {trip.status === 'finalized' ? 'Route optimized and finalized' : 'Route in draft mode'}
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
