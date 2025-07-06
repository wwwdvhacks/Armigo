import { useEffect, useRef } from 'react';
import L from 'leaflet';
import { armeniaCenter, armeniaBounds, citiesCoordinates, armeniaRegions } from '@/lib/armeniaData';

interface MapProps {
  selectedCities: string[];
  onCityClick: (city: string) => void;
  onRegionClick?: (regionId: string) => void;
  showRoute?: boolean;
  className?: string;
}

export function Map({ selectedCities, onCityClick, onRegionClick, showRoute = false, className = "" }: MapProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const markersRef = useRef<L.Marker[]>([]);
  const routeLineRef = useRef<L.Polyline | null>(null);

  useEffect(() => {
    if (!mapRef.current || mapInstanceRef.current) return;

    // Initialize map
    const map = L.map(mapRef.current, {
      center: armeniaCenter,
      zoom: 8,
      maxBounds: armeniaBounds,
      maxBoundsViscosity: 1.0
    });

    // Add tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 18,
      minZoom: 6
    }).addTo(map);

    // Add region image overlays
    armeniaRegions.forEach(region => {
      const bounds = L.latLngBounds(region.bounds);
      
      // Create image overlay for the region
      const imageOverlay = L.imageOverlay(region.imageUrl, bounds, {
        opacity: 0.7,
        className: 'region-overlay cursor-pointer hover:opacity-90 transition-opacity'
      }).addTo(map);

      // Add click handler to the image overlay
      imageOverlay.on('click', () => {
        if (onRegionClick) {
          onRegionClick(region.id);
        }
      });

      // Add invisible clickable rectangle for better interaction
      const clickableArea = L.rectangle(bounds, {
        color: 'transparent',
        weight: 0,
        opacity: 0,
        fillOpacity: 0,
        className: 'region-clickable cursor-pointer'
      }).addTo(map);

      clickableArea.on('click', () => {
        if (onRegionClick) {
          onRegionClick(region.id);
        }
      });

      // Add hover effects
      clickableArea.on('mouseover', () => {
        imageOverlay.setOpacity(0.9);
      });
      
      clickableArea.on('mouseout', () => {
        imageOverlay.setOpacity(0.7);
      });

      // Add region label
      L.marker(region.center, {
        icon: L.divIcon({
          html: `<div class="bg-white/90 backdrop-blur px-3 py-1 rounded-lg shadow-lg text-xs font-medium text-slate-700 pointer-events-none border border-slate-200">${region.name}</div>`,
          className: 'region-label',
          iconSize: [120, 24],
          iconAnchor: [60, 12]
        })
      }).addTo(map);
    });

    mapInstanceRef.current = map;

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, [onRegionClick]);

  // Update city markers
  useEffect(() => {
    if (!mapInstanceRef.current) return;

    // Clear existing markers
    markersRef.current.forEach(marker => {
      mapInstanceRef.current?.removeLayer(marker);
    });
    markersRef.current = [];

    // Add city markers
    Object.entries(citiesCoordinates).forEach(([city, coordinates]) => {
      const isSelected = selectedCities.includes(city);
      const selectedIndex = selectedCities.indexOf(city);
      
      const marker = L.marker(coordinates, {
        icon: L.divIcon({
          html: `
            <div class="flex flex-col items-center">
              <div class="w-4 h-4 rounded-full border-2 border-white shadow-lg cursor-pointer ${
                isSelected ? 'bg-blue-600' : 'bg-slate-400 hover:bg-slate-500'
              }">
                ${isSelected && selectedIndex >= 0 ? 
                  `<div class="w-full h-full rounded-full flex items-center justify-center text-white text-xs font-bold">${selectedIndex + 1}</div>` : 
                  ''
                }
              </div>
              <div class="mt-1 text-xs font-medium text-slate-900 bg-white px-2 py-1 rounded shadow-lg whitespace-nowrap ${
                isSelected ? 'bg-blue-50 text-blue-900' : ''
              }">${city}</div>
            </div>
          `,
          className: 'city-marker',
          iconSize: [80, 40],
          iconAnchor: [40, 20]
        })
      });

      marker.on('click', () => {
        onCityClick(city);
      });

      marker.addTo(mapInstanceRef.current!);
      markersRef.current.push(marker);
    });
  }, [selectedCities, onCityClick]);

  // Update route line
  useEffect(() => {
    if (!mapInstanceRef.current) return;

    // Remove existing route
    if (routeLineRef.current) {
      mapInstanceRef.current.removeLayer(routeLineRef.current);
      routeLineRef.current = null;
    }

    // Add route line if showing route and have multiple cities
    if (showRoute && selectedCities.length > 1) {
      const routeCoordinates = selectedCities
        .map(city => citiesCoordinates[city])
        .filter(coord => coord);

      if (routeCoordinates.length > 1) {
        const routeLine = L.polyline(routeCoordinates, {
          color: '#3b82f6',
          weight: 3,
          opacity: 0.7,
          dashArray: '10, 5'
        });

        routeLine.addTo(mapInstanceRef.current);
        routeLineRef.current = routeLine;
      }
    }
  }, [selectedCities, showRoute]);

  return (
    <div 
      ref={mapRef} 
      className={`w-full h-96 rounded-lg border border-slate-200 ${className}`}
    />
  );
}
