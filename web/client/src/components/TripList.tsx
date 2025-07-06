import { Link } from 'wouter';
import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useTrips } from '@/hooks/useTrips';

export function TripList() {
  const { trips, loading } = useTrips();
  const [statusFilter, setStatusFilter] = useState<string>('all');

  const filteredTrips = trips.filter(trip => {
    if (statusFilter === 'all') return true;
    return trip.status === statusFilter;
  });

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[...Array(6)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <div className="h-32 bg-slate-200"></div>
            <CardContent className="p-4">
              <div className="h-4 bg-slate-200 rounded mb-2"></div>
              <div className="h-3 bg-slate-200 rounded w-1/2 mb-3"></div>
              <div className="flex space-x-1 mb-3">
                <div className="h-6 bg-slate-200 rounded w-16"></div>
                <div className="h-6 bg-slate-200 rounded w-16"></div>
              </div>
              <div className="flex justify-between">
                <div className="h-3 bg-slate-200 rounded w-20"></div>
                <div className="h-3 bg-slate-200 rounded w-16"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div>
      {/* Filter Controls */}
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-2xl font-bold text-slate-900">My Trips</h3>
        <div className="flex items-center space-x-3">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Trips</SelectItem>
              <SelectItem value="draft">Draft</SelectItem>
              <SelectItem value="finalized">Finalized</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Trips Grid */}
      {filteredTrips.length === 0 ? (
        <Card>
          <CardContent className="pt-6 text-center">
            <i className="fas fa-route text-4xl text-slate-400 mb-4"></i>
            <h4 className="text-lg font-semibold text-slate-900 mb-2">
              {statusFilter === 'all' ? 'No trips yet' : `No ${statusFilter} trips`}
            </h4>
            <p className="text-slate-600 mb-4">
              {statusFilter === 'all' 
                ? 'Start planning your first Armenia adventure!'
                : `You don't have any ${statusFilter} trips yet.`
              }
            </p>
            <Link href="/create-trip">
              <Button>
                <i className="fas fa-plus mr-2"></i>Create Your First Trip
              </Button>
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredTrips.map((trip) => (
            <Card key={trip.tripId} className="overflow-hidden hover:shadow-md transition-shadow">
              <div className="relative h-32 armenia-gradient">
                <div className="absolute inset-0 bg-black bg-opacity-20"></div>
                <div className="absolute top-3 right-3">
                  <Badge variant={trip.status === 'finalized' ? 'default' : 'secondary'}>
                    {trip.status}
                  </Badge>
                </div>
                <div className="absolute bottom-3 left-3 text-white">
                  <h4 className="font-semibold text-lg">{trip.title}</h4>
                </div>
              </div>
              
              <CardContent className="p-4">
                <p className="text-sm text-slate-600 mb-3">
                  {trip.cities.length} cities • {trip.status === 'finalized' ? 'Ready to go' : 'In planning'}
                </p>
                
                {trip.description && (
                  <p className="text-sm text-slate-700 mb-3 line-clamp-2">{trip.description}</p>
                )}
                
                <div className="flex flex-wrap gap-1 mb-3">
                  {trip.cities.slice(0, 3).map((city, index) => (
                    <Badge key={`${city}-${index}`} variant="outline" className="text-xs">
                      {city}
                    </Badge>
                  ))}
                  {trip.cities.length > 3 && (
                    <Badge variant="outline" className="text-xs">
                      +{trip.cities.length - 3}
                    </Badge>
                  )}
                </div>
                
                <div className="flex items-center justify-between">
                  <span className="text-xs text-slate-500">
                    Created {new Date(trip.createdAt).toLocaleDateString()}
                  </span>
                  <Link href={`/trip/${trip.tripId}`}>
                    <Button size="sm" variant="outline">
                      {trip.status === 'draft' ? 'Continue' : 'View Trip'}
                      <i className={`ml-1 fas ${trip.status === 'draft' ? 'fa-edit' : 'fa-arrow-right'}`}></i>
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
