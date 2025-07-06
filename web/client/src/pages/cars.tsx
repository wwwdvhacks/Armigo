import { CarsAvailable } from '@/components/CarsAvailable';

export default function CarsPage() {
  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 mb-2">Cars & Rides</h1>
        <p className="text-slate-600">Find available cars or offer your own car for trips around Armenia.</p>
      </div>
      
      <CarsAvailable />
    </div>
  );
}