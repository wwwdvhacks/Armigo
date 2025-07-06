import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useFriends } from '@/hooks/useFriends';
import { useToast } from '@/hooks/use-toast';
import { Users, UserPlus, Search, Check, X, Clock, UserMinus } from 'lucide-react';

export function Friends() {
  const [searchUsername, setSearchUsername] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const { 
    friends, 
    sentRequests, 
    receivedRequests, 
    loading,
    sendFriendRequest,
    acceptFriendRequest,
    rejectFriendRequest,
    removeFriend
  } = useFriends();
  const { toast } = useToast();

  const handleSendRequest = async () => {
    if (!searchUsername.trim()) {
      toast({
        title: "Username required",
        description: "Please enter a username to search",
        variant: "destructive"
      });
      return;
    }

    setIsSearching(true);
    try {
      const success = await sendFriendRequest(searchUsername.trim());
      if (success) {
        toast({
          title: "Friend request sent",
          description: `Request sent to ${searchUsername}`
        });
        setSearchUsername('');
      }
    } catch (error: any) {
      toast({
        title: "Failed to send request",
        description: error.message,
        variant: "destructive"
      });
    } finally {
      setIsSearching(false);
    }
  };

  const handleAcceptRequest = async (request: any) => {
    try {
      const success = await acceptFriendRequest(request);
      if (success) {
        toast({
          title: "Friend request accepted",
          description: `You are now friends with ${request.fromUsername}`
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
      const success = await rejectFriendRequest(request);
      if (success) {
        toast({
          title: "Friend request rejected",
          description: `Request from ${request.fromUsername} rejected`
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

  const handleRemoveFriend = async (friend: any) => {
    try {
      const success = await removeFriend(friend.uid);
      if (success) {
        toast({
          title: "Friend removed",
          description: `${friend.username} removed from friends`
        });
      }
    } catch (error: any) {
      toast({
        title: "Failed to remove friend",
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
          <p className="text-slate-600">Loading friends...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <UserPlus className="h-5 w-5" />
            Add Friend
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <Input
              placeholder="Enter username..."
              value={searchUsername}
              onChange={(e) => setSearchUsername(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSendRequest()}
              disabled={isSearching}
            />
            <Button 
              onClick={handleSendRequest}
              disabled={isSearching}
            >
              {isSearching ? (
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              ) : (
                <Search className="h-4 w-4" />
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Tabs defaultValue="friends" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="friends" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Friends ({friends.length})
          </TabsTrigger>
          <TabsTrigger value="received" className="flex items-center gap-2">
            <Clock className="h-4 w-4" />
            Requests ({receivedRequests.length})
          </TabsTrigger>
          <TabsTrigger value="sent" className="flex items-center gap-2">
            <UserPlus className="h-4 w-4" />
            Sent ({sentRequests.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="friends" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Your Friends</CardTitle>
            </CardHeader>
            <CardContent>
              {friends.length > 0 ? (
                <div className="space-y-3">
                  {friends.map((friend) => (
                    <div key={friend.uid} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                      <div className="flex items-center gap-3">
                        <Avatar>
                          <AvatarFallback>
                            {friend.username.charAt(0).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-medium">{friend.username}</p>
                          <p className="text-xs text-slate-500">
                            Added {new Date(friend.addedAt).toLocaleDateString()}
                          </p>
                        </div>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleRemoveFriend(friend)}
                        className="text-red-500 hover:text-red-700 hover:bg-red-50"
                      >
                        <UserMinus className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Users className="h-12 w-12 text-slate-400 mx-auto mb-4" />
                  <p className="text-slate-600">No friends yet</p>
                  <p className="text-sm text-slate-500">Start by adding friends using their username</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="received" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Friend Requests</CardTitle>
            </CardHeader>
            <CardContent>
              {receivedRequests.length > 0 ? (
                <div className="space-y-3">
                  {receivedRequests.map((request) => (
                    <div key={request.requestId} className="flex items-center justify-between p-3 bg-blue-50 border border-blue-200 rounded-lg">
                      <div className="flex items-center gap-3">
                        <Avatar>
                          <AvatarFallback>
                            {request.fromUsername.charAt(0).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-medium">{request.fromUsername}</p>
                          <p className="text-xs text-slate-500">
                            {new Date(request.timestamp).toLocaleDateString()}
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          onClick={() => handleAcceptRequest(request)}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <Check className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleRejectRequest(request)}
                          className="text-red-600 border-red-200 hover:bg-red-50"
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Clock className="h-12 w-12 text-slate-400 mx-auto mb-4" />
                  <p className="text-slate-600">No pending requests</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="sent" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Sent Requests</CardTitle>
            </CardHeader>
            <CardContent>
              {sentRequests.length > 0 ? (
                <div className="space-y-3">
                  {sentRequests.map((request) => (
                    <div key={request.requestId} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                      <div className="flex items-center gap-3">
                        <Avatar>
                          <AvatarFallback>
                            {request.toUsername.charAt(0).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-medium">{request.toUsername}</p>
                          <p className="text-xs text-slate-500">
                            {new Date(request.timestamp).toLocaleDateString()}
                          </p>
                        </div>
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
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <UserPlus className="h-12 w-12 text-slate-400 mx-auto mb-4" />
                  <p className="text-slate-600">No sent requests</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}