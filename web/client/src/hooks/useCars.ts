import { useState, useEffect } from 'react';
import { ref, push, set, onValue, off } from 'firebase/database';
import { database } from '@/lib/firebase';
import { useAuth } from './useAuth';

export interface Car {
  carId: string;
  driverId: string;
  driverName: string;
  carModel: string;
  seatsAvailable: number;
  status: 'available' | 'booked';
  currentTripId: string | null;
  availableAfter: number | null;
}

export interface CarRequest {
  requestId: string;
  carId: string;
  requesterId: string;
  requesterName: string;
  driverId: string;
  message: string;
  tripId?: string;
  tripName?: string;
  status: 'pending' | 'accepted' | 'rejected';
  timestamp: number;
}

export function useCars() {
  const { user, userProfile } = useAuth();
  const [cars, setCars] = useState<Car[]>([]);
  const [myCarRequests, setMyCarRequests] = useState<CarRequest[]>([]);
  const [receivedRequests, setReceivedRequests] = useState<CarRequest[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }

    // Listen to all cars
    const carsRef = ref(database, 'cars');
    const carsUnsubscribe = onValue(carsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const carsList = Object.entries(data).map(([carId, carData]: [string, any]) => ({
          carId,
          ...carData
        }));

        // Filter available cars or show all if user is a driver
        const now = Date.now();
        const availableCars = carsList.filter(car => 
          car.status === 'available' || 
          (car.availableAfter && car.availableAfter <= now) ||
          car.driverId === user.uid
        );

        setCars(availableCars);
      } else {
        setCars([]);
      }
    });

    // Listen to car requests sent by user
    const myRequestsRef = ref(database, `carRequests`);
    const myRequestsUnsubscribe = onValue(myRequestsRef, (snapshot) => {
      const data = snapshot.val();
      const userRequests: CarRequest[] = [];
      
      if (data) {
        Object.entries(data).forEach(([driverId, driverRequests]: [string, any]) => {
          if (driverRequests) {
            Object.entries(driverRequests).forEach(([requestId, requestData]: [string, any]) => {
              if (requestData.requesterId === user.uid) {
                userRequests.push({
                  requestId,
                  ...requestData
                });
              }
            });
          }
        });
      }
      
      setMyCarRequests(userRequests);
    });

    // Listen to requests received as driver
    const receivedRequestsRef = ref(database, `carRequests/${user.uid}`);
    const receivedUnsubscribe = onValue(receivedRequestsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const requestsList = Object.entries(data).map(([requestId, requestData]: [string, any]) => ({
          requestId,
          ...requestData
        }));
        setReceivedRequests(requestsList.filter(req => req.status === 'pending'));
      } else {
        setReceivedRequests([]);
      }
      setLoading(false);
    });

    return () => {
      off(carsRef);
      off(myRequestsRef);
      off(receivedRequestsRef);
    };
  }, [user]);

  const addCar = async (carModel: string, seatsAvailable: number): Promise<boolean> => {
    if (!user || !userProfile) throw new Error('Not authenticated');

    try {
      const carId = push(ref(database, 'cars')).key!;
      
      const carData: Omit<Car, 'carId'> = {
        driverId: user.uid,
        driverName: userProfile.username,
        carModel,
        seatsAvailable,
        status: 'available',
        currentTripId: null,
        availableAfter: null
      };

      const carRef = ref(database, `cars/${carId}`);
      await set(carRef, carData);

      return true;
    } catch (error) {
      console.error('Error adding car:', error);
      return false;
    }
  };

  const requestCar = async (carId: string, message: string, tripId?: string, tripName?: string): Promise<boolean> => {
    if (!user || !userProfile) throw new Error('Not authenticated');

    try {
      const car = cars.find(c => c.carId === carId);
      if (!car) throw new Error('Car not found');

      const requestId = push(ref(database, `carRequests/${car.driverId}`)).key!;
      const timestamp = Date.now();

      const requestData: Omit<CarRequest, 'requestId'> = {
        carId,
        requesterId: user.uid,
        requesterName: userProfile.username,
        driverId: car.driverId,
        message,
        tripId,
        tripName,
        status: 'pending',
        timestamp
      };

      const requestRef = ref(database, `carRequests/${car.driverId}/${requestId}`);
      await set(requestRef, requestData);

      return true;
    } catch (error) {
      console.error('Error requesting car:', error);
      return false;
    }
  };

  const respondToRequest = async (request: CarRequest, accept: boolean, tripEndTime?: number): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      const status = accept ? 'accepted' : 'rejected';
      
      // Update request status
      const requestRef = ref(database, `carRequests/${user.uid}/${request.requestId}`);
      await set(requestRef, { ...request, status });

      // If accepted, update car status
      if (accept) {
        const carRef = ref(database, `cars/${request.carId}`);
        const carUpdate = {
          status: 'booked' as const,
          currentTripId: request.tripId || null,
          availableAfter: tripEndTime || null
        };
        
        await set(carRef, carUpdate);
      }

      return true;
    } catch (error) {
      console.error('Error responding to request:', error);
      return false;
    }
  };

  const endTrip = async (carId: string): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      const carRef = ref(database, `cars/${carId}`);
      const carUpdate = {
        status: 'available' as const,
        currentTripId: null,
        availableAfter: null
      };
      
      await set(carRef, carUpdate);
      return true;
    } catch (error) {
      console.error('Error ending trip:', error);
      return false;
    }
  };

  return {
    cars,
    myCarRequests,
    receivedRequests,
    loading,
    addCar,
    requestCar,
    respondToRequest,
    endTrip
  };
}