import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { CitySelector } from './CitySelector';
import { useTrips } from '@/hooks/useTrips';
import { useToast } from '@/hooks/use-toast';
import { Loader2, MapPin, Save } from 'lucide-react';

const tripSchema = z.object({
  title: z.string().min(1, 'Trip title is required'),
  description: z.string().optional(),
  status: z.enum(['draft', 'finalized']).default('draft')
});

type TripForm = z.infer<typeof tripSchema>;

export function TripCreate() {
  const [selectedCities, setSelectedCities] = useState<string[]>([]);
  const { createTrip } = useTrips();
  const { toast } = useToast();

  const form = useForm<TripForm>({
    resolver: zodResolver(tripSchema),
    defaultValues: {
      title: '',
      description: '',
      status: 'draft'
    }
  });

  const handleSaveTrip = async (data: TripForm) => {
    if (selectedCities.length === 0) {
      toast({
        title: "No cities selected",
        description: "Please select at least one city for your trip",
        variant: "destructive"
      });
      return;
    }

    try {
      await createTrip({
        ...data,
        cities: selectedCities
      });
      
      toast({
        title: "Trip saved successfully",
        description: `Your trip "${data.title}" has been saved`
      });
      
      // Reset form
      form.reset();
      setSelectedCities([]);
    } catch (error) {
      toast({
        title: "Failed to save trip",
        description: "Please try again",
        variant: "destructive"
      });
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <MapPin className="h-5 w-5" />
            Create New Trip
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleSaveTrip)} className="space-y-4">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Trip Title</FormLabel>
                    <FormControl>
                      <Input placeholder="My Armenia Adventure" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description (Optional)</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="Describe your trip plans..."
                        className="resize-none"
                        rows={3}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="status"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Trip Status</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select status" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="draft">Draft</SelectItem>
                        <SelectItem value="finalized">Finalized</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="pt-4">
                <Button 
                  type="submit" 
                  disabled={form.formState.isSubmitting || selectedCities.length === 0}
                  className="w-full"
                >
                  {form.formState.isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Saving Trip...
                    </>
                  ) : (
                    <>
                      <Save className="mr-2 h-4 w-4" />
                      Save Trip ({selectedCities.length} cities)
                    </>
                  )}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>

      <CitySelector 
        selectedCities={selectedCities}
        onCitiesChange={setSelectedCities}
      />
    </div>
  );
}