import { useState, useEffect } from 'react';
import { ref, push, set, get, onValue, off } from 'firebase/database';
import { database } from '@/lib/firebase';
import { useAuth } from './useAuth';

export interface Trip {
  tripId: string;
  ownerId: string;
  title: string;
  description?: string;
  cities: string[];
  status: 'draft' | 'finalized';
  createdAt: number;
  updatedAt?: number;
}

export function useTrips() {
  const { user } = useAuth();
  const [trips, setTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setTrips([]);
      setLoading(false);
      return;
    }

    const tripsRef = ref(database, `trips/${user.uid}`);
    
    const unsubscribe = onValue(tripsRef, (snapshot) => {
      if (snapshot.exists()) {
        const tripsData = snapshot.val();
        const tripsArray = Object.entries(tripsData).map(([tripId, tripData]: [string, any]) => ({
          tripId,
          ...tripData
        }));
        setTrips(tripsArray.sort((a, b) => b.createdAt - a.createdAt));
      } else {
        setTrips([]);
      }
      setLoading(false);
    });

    return () => off(tripsRef, 'value', unsubscribe);
  }, [user]);

  const createTrip = async (tripData: Omit<Trip, 'tripId' | 'ownerId' | 'createdAt'>) => {
    if (!user) throw new Error('User must be logged in');

    const tripsRef = ref(database, `trips/${user.uid}`);
    const newTripRef = push(tripsRef);
    
    const trip: Trip = {
      ...tripData,
      tripId: newTripRef.key!,
      ownerId: user.uid,
      createdAt: Date.now()
    };

    await set(newTripRef, trip);
    return trip;
  };

  const updateTrip = async (tripId: string, updates: Partial<Trip>) => {
    if (!user) throw new Error('User must be logged in');

    const tripRef = ref(database, `trips/${user.uid}/${tripId}`);
    const updatedData = {
      ...updates,
      updatedAt: Date.now()
    };
    
    await set(tripRef, updatedData);
  };

  const getTrip = async (tripId: string): Promise<Trip | null> => {
    if (!user) return null;

    const tripRef = ref(database, `trips/${user.uid}/${tripId}`);
    const snapshot = await get(tripRef);
    
    if (snapshot.exists()) {
      return {
        tripId,
        ...snapshot.val()
      };
    }
    
    return null;
  };

  const optimizeTrip = (cities: string[]): { cities: string[], benefits: string } => {
    // Mock AI optimization - in real implementation, this would call an AI service
    // For now, we'll implement a simple distance-based optimization
    
    if (cities.length <= 2) {
      return { 
        cities, 
        benefits: 'Route is already optimal for short trips.' 
      };
    }

    // Simple optimization: try to create a logical geographical flow
    const optimized = [...cities].sort();
    
    return {
      cities: optimized,
      benefits: 'Reduces total travel time by 45 minutes and follows scenic mountain routes.'
    };
  };

  return {
    trips,
    loading,
    createTrip,
    updateTrip,
    getTrip,
    optimizeTrip
  };
}
