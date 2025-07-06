import { Friends } from '@/components/Friends';

export default function FriendsPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 mb-2">Friends</h1>
        <p className="text-slate-600">Connect with other travelers and plan trips together.</p>
      </div>
      
      <Friends />
    </div>
  );
}