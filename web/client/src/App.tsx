import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { useAuth } from "@/hooks/useAuth";

// Pages
import Login from "@/pages/login";
import Chat from "@/pages/chat";
import Trips from "@/pages/trips";
import CreateTrip from "@/pages/create-trip";
import TripViewPage from "@/pages/trip-view";
import Profile from "@/pages/profile";
import Friends from "@/pages/friends";
import Cars from "@/pages/cars";
import NotFound from "@/pages/not-found";

function Router() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="text-center">
          <div className="w-12 h-12 armenia-gradient rounded-lg flex items-center justify-center mx-auto mb-4">
            <i className="fas fa-mountain text-white text-xl"></i>
          </div>
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
          <p className="text-slate-600">Loading Armenia Explorer...</p>
        </div>
      </div>
    );
  }

  return (
    <Switch>
      <Route path="/" component={() => user ? <Trips /> : <Login />} />
      <Route path="/login" component={Login} />
      <Route path="/chat" component={Chat} />
      <Route path="/trips" component={Trips} />
      <Route path="/create-trip" component={CreateTrip} />
      <Route path="/trip/:tripId" component={TripViewPage} />
      <Route path="/friends" component={Friends} />
      <Route path="/cars" component={Cars} />
      <Route path="/profile" component={Profile} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Toaster />
        <Router />
      </TooltipProvider>
    </QueryClientProvider>
  );
}

export default App;
