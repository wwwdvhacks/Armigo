import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useCars } from '@/hooks/useCars';
import { useTrips } from '@/hooks/useTrips';
import { useToast } from '@/hooks/use-toast';
import { Car, MessageCircle, Plus, Users, Clock, CheckCircle, XCircle } from 'lucide-react';

export function CarsAvailable() {
  const [carModel, setCarModel] = useState('');
  const [seatsAvailable, setSeatsAvailable] = useState(4);
  const [requestMessage, setRequestMessage] = useState('');
  const [selectedCarId, setSelectedCarId] = useState('');
  const [selectedTripId, setSelectedTripId] = useState('');
  const [isAddingCar, setIsAddingCar] = useState(false);
  const [isRequestDialogOpen, setIsRequestDialogOpen] = useState(false);
  
  const { 
    cars, 
    myCarRequests, 
    receivedRequests, 
    loading,
    addCar,
    requestCar,
    respondToRequest,
    endTrip
  } = useCars();
  const { trips } = useTrips();
  const { toast } = useToast();

  const handleAddCar = async () => {
    if (!carModel.trim()) {
      toast({
        title: "Car model required",
        description: "Please enter your car model",
        variant: "destructive"
      });
      return;
    }

    setIsAddingCar(true);
    try {
      const success = await addCar(carModel, seatsAvailable);
      if (success) {
        toast({
          title: "Car added successfully",
          description: "Your car is now available for bookings"
        });
        setCarModel('');
        setSeatsAvailable(4);
      }
    } catch (error: any) {
      toast({
        title: "Failed to add car",
        description: error.message,
        variant: "destructive"
      });
    } finally {
      setIsAddingCar(false);
    }
  };

  const handleRequestCar = async () => {
    if (!requestMessage.trim()) {
      toast({
        title: "Message required",
        description: "Please add a message for the driver",
        variant: "destructive"
      });
      return;
    }

    try {
      const selectedTrip = trips.find(t => t.tripId === selectedTripId);
      const success = await requestCar(
        selectedCarId, 
        requestMessage,
        selectedTripId || undefined,
        selectedTrip?.title || undefined
      );
      
      if (success) {
        toast({
          title: "Request sent",
          description: "Your car request has been sent to the driver"
        });
        setRequestMessage('');
        setSelectedTripId('');
        setIsRequestDialogOpen(false);
      }
    } catch (error: any) {
      toast({
        title: "Failed to send request",
        description: error.message,
        variant: "destructive"
      });
    }
  };

  const handleAcceptRequest = async (request: any) => {
    // Estimate trip end time (example: 8 hours from now)
    const tripEndTime = Date.now() + (8 * 60 * 60 * 1000);
    
    try {
      const success = await respondToRequest(request, true, tripEndTime);
      if (success) {
        toast({
          title: "Request accepted",
          description: `Booking confirmed for ${request.requesterName}`
        });
      }
    } catch (error: any) {
      toast({
        title: "Failed to accept request",
        description: error.message,
        variant: "destructive"
      });
    }
  };

  const handleRejectRequest = async (request: any) => {
    try {
      const success = await respondToRequest(request, false);
      if (success) {
        toast({
          title: "Request rejected",
          description: `Request from ${request.requesterName} rejected`
        });
      }
    } catch (error: any) {
      toast({
        title: "Failed to reject request",
        description: error.message,
        variant: "destructive"
      });
    }
  };

  const handleEndTrip = async (carId: string) => {
    try {
      const success = await endTrip(carId);
      if (success) {
        toast({
          title: "Trip ended",
          description: "Car is now available for new bookings"
        });
      }
    } catch (error: any) {
      toast({
        title: "Failed to end trip",
        description: error.message,
        variant: "destructive"
      });
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-slate-600">Loading cars...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Add Car Section */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Plus className="h-5 w-5" />
            Add Your Car
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2">
              <Input
                placeholder="Car model (e.g., Toyota Prius)"
                value={carModel}
                onChange={(e) => setCarModel(e.target.value)}
              />
            </div>
            <div>
              <Input
                type="number"
                min={1}
                max={8}
                placeholder="Seats"
                value={seatsAvailable}
                onChange={(e) => setSeatsAvailable(parseInt(e.target.value) || 4)}
              />
            </div>
          </div>
          <Button 
            onClick={handleAddCar}
            disabled={isAddingCar}
            className="w-full"
          >
            {isAddingCar ? 'Adding...' : 'Add Car'}
          </Button>
        </CardContent>
      </Card>

      <Tabs defaultValue="available" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="available">Available Cars ({cars.filter(c => c.status === 'available').length})</TabsTrigger>
          <TabsTrigger value="requests">My Requests ({myCarRequests.length})</TabsTrigger>
          <TabsTrigger value="inbox">Driver Inbox ({receivedRequests.length})</TabsTrigger>
        </TabsList>

        <TabsContent value="available" className="space-y-4">
          {cars.filter(car => car.status === 'available').length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {cars.filter(car => car.status === 'available').map((car) => (
                <Card key={car.carId}>
                  <CardContent className="p-4">
                    <div className="space-y-3">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="font-semibold">{car.carModel}</h3>
                          <p className="text-sm text-slate-600">Driver: {car.driverName}</p>
                        </div>
                        <Badge variant="secondary">
                          <Users className="h-3 w-3 mr-1" />
                          {car.seatsAvailable}
                        </Badge>
                      </div>
                      
                      <Dialog open={isRequestDialogOpen && selectedCarId === car.carId} onOpenChange={setIsRequestDialogOpen}>
                        <DialogTrigger asChild>
                          <Button 
                            size="sm" 
                            className="w-full"
                            onClick={() => setSelectedCarId(car.carId)}
                          >
                            <MessageCircle className="h-4 w-4 mr-2" />
                            Request Car
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Request {car.carModel}</DialogTitle>
                          </DialogHeader>
                          <div className="space-y-4">
                            <div>
                              <label className="text-sm font-medium">Select Trip (Optional)</label>
                              <select 
                                className="w-full mt-1 p-2 border rounded-md"
                                value={selectedTripId}
                                onChange={(e) => setSelectedTripId(e.target.value)}
                              >
                                <option value="">No specific trip</option>
                                {trips.map(trip => (
                                  <option key={trip.tripId} value={trip.tripId}>
                                    {trip.title}
                                  </option>
                                ))}
                              </select>
                            </div>
                            <div>
                              <label className="text-sm font-medium">Message to Driver</label>
                              <Textarea
                                placeholder="Hi! I'd like to request your car for..."
                                value={requestMessage}
                                onChange={(e) => setRequestMessage(e.target.value)}
                                rows={3}
                              />
                            </div>
                            <div className="flex gap-2">
                              <Button onClick={handleRequestCar} className="flex-1">
                                Send Request
                              </Button>
                              <Button 
                                variant="outline" 
                                onClick={() => setIsRequestDialogOpen(false)}
                                className="flex-1"
                              >
                                Cancel
                              </Button>
                            </div>
                          </div>
                        </DialogContent>
                      </Dialog>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <Car className="h-12 w-12 text-slate-400 mx-auto mb-4" />
              <p className="text-slate-600">No cars available</p>
              <p className="text-sm text-slate-500">Add your car to start offering rides</p>
            </div>
          )}
        </TabsContent>

        <TabsContent value="requests" className="space-y-4">
          {myCarRequests.length > 0 ? (
            <div className="space-y-3">
              {myCarRequests.map((request) => (
                <Card key={request.requestId}>
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium">Request to {cars.find(c => c.carId === request.carId)?.driverName}</p>
                        <p className="text-sm text-slate-600">{request.message}</p>
                        {request.tripName && (
                          <p className="text-xs text-blue-600">Trip: {request.tripName}</p>
                        )}
                        <p className="text-xs text-slate-500">
                          {new Date(request.timestamp).toLocaleDateString()}
                        </p>
                      </div>
                      <Badge
                        variant={
                          request.status === 'accepted' ? 'default' :
                          request.status === 'rejected' ? 'destructive' : 'secondary'
                        }
                      >
                        {request.status}
                      </Badge>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <Clock className="h-12 w-12 text-slate-400 mx-auto mb-4" />
              <p className="text-slate-600">No requests sent</p>
            </div>
          )}
        </TabsContent>

        <TabsContent value="inbox" className="space-y-4">
          {receivedRequests.length > 0 ? (
            <div className="space-y-3">
              {receivedRequests.map((request) => (
                <Card key={request.requestId}>
                  <CardContent className="p-4">
                    <div className="space-y-3">
                      <div>
                        <p className="font-medium">Request from {request.requesterName}</p>
                        <p className="text-sm text-slate-600">{request.message}</p>
                        {request.tripName && (
                          <p className="text-xs text-blue-600">Trip: {request.tripName}</p>
                        )}
                        <p className="text-xs text-slate-500">
                          {new Date(request.timestamp).toLocaleDateString()}
                        </p>
                      </div>
                      
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          onClick={() => handleAcceptRequest(request)}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <CheckCircle className="h-4 w-4 mr-1" />
                          Accept
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleRejectRequest(request)}
                          className="text-red-600 border-red-200 hover:bg-red-50"
                        >
                          <XCircle className="h-4 w-4 mr-1" />
                          Reject
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <MessageCircle className="h-12 w-12 text-slate-400 mx-auto mb-4" />
              <p className="text-slate-600">No pending requests</p>
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* My Cars with Booked Status */}
      {cars.filter(car => car.status === 'booked').length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>My Booked Cars</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {cars.filter(car => car.status === 'booked').map((car) => (
                <div key={car.carId} className="flex items-center justify-between p-3 bg-orange-50 border border-orange-200 rounded-lg">
                  <div>
                    <p className="font-medium">{car.carModel}</p>
                    <p className="text-sm text-slate-600">Currently booked</p>
                    {car.availableAfter && (
                      <p className="text-xs text-orange-600">
                        Available after: {new Date(car.availableAfter).toLocaleString()}
                      </p>
                    )}
                  </div>
                  <Button
                    size="sm"
                    onClick={() => handleEndTrip(car.carId)}
                    variant="outline"
                  >
                    End Trip
                  </Button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}