import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { X, Plus, Search } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

const POPULAR_CITIES = [
  'Yerevan',
  'Gyumri', 
  'Vanadzor',
  'Dilijan',
  'Goris',
  'Stepanavan'
];

const ALL_CITIES = [
  'Yerevan', 'Gyumri', 'Vanadzor', 'Dilijan', 'Goris', 'Stepanavan',
  'Alaverdi', 'Artik', 'Maralik', 'Hrazdan', 'Charentsavan', 'Abovyan',
  'Nor Hachn', 'Gavar', 'Sevan', 'Vardenis', 'Martuni', 'Ijevan',
  'Berd', 'Noyemberyan', 'Artashat', 'Ararat', 'Masis', 'Echmiadzin',
  'Armavir', 'Vagharshapat', 'Metsamor', 'Yeghegnadzor', 'Jermuk',
  'Areni', 'Vayk', 'Kapan', 'Sisian', 'Meghri', 'Kajaran',
  'Ashtarak', 'Aparan', 'Talin', 'Byureghavan', 'Spitak', 'Tumanyan'
];

interface CitySelectorProps {
  selectedCities: string[];
  onCitiesChange: (cities: string[]) => void;
}

export function CitySelector({ selectedCities, onCitiesChange }: CitySelectorProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const { toast } = useToast();

  const addCity = (city: string) => {
    if (selectedCities.includes(city)) {
      toast({
        title: "City already added",
        description: `${city} is already in your trip`,
        variant: "destructive"
      });
      return;
    }
    
    onCitiesChange([...selectedCities, city]);
    toast({
      title: "City added",
      description: `${city} added to your trip`
    });
  };

  const removeCity = (city: string) => {
    onCitiesChange(selectedCities.filter(c => c !== city));
  };

  const handleSearch = () => {
    const city = searchTerm.trim();
    if (!city) return;

    if (ALL_CITIES.includes(city)) {
      addCity(city);
      setSearchTerm('');
    } else {
      toast({
        title: "City not found",
        description: `"${city}" is not in our list of Armenian cities`,
        variant: "destructive"
      });
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Plus className="h-5 w-5" />
            Select Cities for Your Trip
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Popular Cities */}
          <div>
            <h3 className="text-sm font-medium mb-3 text-slate-600">Popular Cities</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
              {POPULAR_CITIES.map(city => (
                <Button
                  key={city}
                  variant="outline"
                  size="sm"
                  onClick={() => addCity(city)}
                  disabled={selectedCities.includes(city)}
                  className="justify-start"
                >
                  <Plus className="h-3 w-3 mr-1" />
                  {city}
                </Button>
              ))}
            </div>
          </div>

          {/* Search for Other Cities */}
          <div>
            <h3 className="text-sm font-medium mb-3 text-slate-600">Search for Other Cities</h3>
            <div className="flex gap-2">
              <Input
                placeholder="Enter city name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <Button onClick={handleSearch} size="sm">
                <Search className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Selected Cities */}
      {selectedCities.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Selected Cities ({selectedCities.length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {selectedCities.map((city, index) => (
                <Badge key={city} variant="secondary" className="flex items-center gap-1">
                  <span className="text-xs font-normal">{index + 1}.</span>
                  {city}
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-4 w-4 p-0 hover:bg-destructive hover:text-destructive-foreground"
                    onClick={() => removeCity(city)}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}