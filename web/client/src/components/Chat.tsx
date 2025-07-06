import { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useToast } from '@/hooks/use-toast';
import { useChat, Message } from '@/hooks/useChat';
import { useAuth } from '@/hooks/useAuth';

interface ChatProps {
  targetUserId?: string;
  targetUsername?: string;
}

export function Chat({ targetUserId, targetUsername }: ChatProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [chatId, setChatId] = useState<string>('');
  const [users, setUsers] = useState<any[]>([]);
  const [selectedUser, setSelectedUser] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  
  const { user } = useAuth();
  const { startChat, sendMessage, loadMessages, getUsers } = useChat();
  const { toast } = useToast();

  // Load users for friend list
  useEffect(() => {
    const loadUsers = async () => {
      try {
        const usersList = await getUsers();
        setUsers(usersList);
      } catch (error: any) {
        toast({
          title: 'Error',
          description: 'Failed to load users',
          variant: 'destructive'
        });
      }
    };

    loadUsers();
  }, [getUsers, toast]);

  // Auto-scroll to bottom of messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Start chat with target user if provided
  useEffect(() => {
    if (targetUserId && user) {
      handleStartChat(targetUserId, targetUsername || 'Unknown User');
    }
  }, [targetUserId, targetUsername, user]);

  const handleStartChat = async (userId: string, username: string) => {
    if (!user) return;

    setLoading(true);
    try {
      const newChatId = await startChat(userId);
      setChatId(newChatId);
      setSelectedUser({ uid: userId, username });

      // Load messages for this chat
      const unsubscribe = loadMessages(newChatId, (loadedMessages) => {
        setMessages(loadedMessages.filter(msg => msg.senderId !== 'system'));
      });

      // Store unsubscribe function for cleanup
      return unsubscribe;
    } catch (error: any) {
      toast({
        title: 'Error',
        description: 'Failed to start chat',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newMessage.trim() || !chatId || !user) return;

    try {
      await sendMessage(chatId, newMessage.trim());
      setNewMessage('');
    } catch (error: any) {
      toast({
        title: 'Error',
        description: 'Failed to send message',
        variant: 'destructive'
      });
    }
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString([], { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 h-[calc(100vh-12rem)]">
      {/* Friends List */}
      <Card className="lg:col-span-1">
        <CardHeader>
          <CardTitle className="text-lg">Friends</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="max-h-96 overflow-y-auto">
            {users.length === 0 ? (
              <div className="p-4 text-center text-slate-500">
                <i className="fas fa-users text-2xl mb-2"></i>
                <p className="text-sm">No other users found</p>
              </div>
            ) : (
              users.map((chatUser) => (
                <button
                  key={chatUser.uid}
                  onClick={() => handleStartChat(chatUser.uid, chatUser.username)}
                  className={`w-full p-3 text-left hover:bg-slate-50 border-b border-slate-100 transition-colors ${
                    selectedUser?.uid === chatUser.uid ? 'bg-blue-50' : ''
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <Avatar className="h-8 w-8">
                      <AvatarFallback>
                        {chatUser.username?.slice(0, 2).toUpperCase() || 'U'}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="font-medium text-sm">{chatUser.username}</div>
                      <div className="text-xs text-slate-500">Click to chat</div>
                    </div>
                  </div>
                </button>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* Chat Area */}
      <Card className="lg:col-span-3 flex flex-col">
        <CardHeader className="border-b">
          <CardTitle className="text-lg">
            {selectedUser ? (
              <div className="flex items-center space-x-3">
                <Avatar className="h-8 w-8">
                  <AvatarFallback>
                    {selectedUser.username?.slice(0, 2).toUpperCase() || 'U'}
                  </AvatarFallback>
                </Avatar>
                <span>Chat with {selectedUser.username}</span>
              </div>
            ) : (
              'Select a friend to start chatting'
            )}
          </CardTitle>
        </CardHeader>
        
        {selectedUser ? (
          <>
            {/* Messages */}
            <CardContent className="flex-1 overflow-y-auto p-4 space-y-4">
              {loading ? (
                <div className="flex justify-center">
                  <div className="animate-pulse text-slate-500">Loading chat...</div>
                </div>
              ) : messages.length === 0 ? (
                <div className="text-center text-slate-500">
                  <i className="fas fa-comments text-3xl mb-2"></i>
                  <p>No messages yet. Start the conversation!</p>
                </div>
              ) : (
                messages.map((message) => (
                  <div
                    key={message.messageId}
                    className={`flex ${
                      message.senderId === user?.uid ? 'justify-end' : 'justify-start'
                    }`}
                  >
                    <div
                      className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                        message.senderId === user?.uid
                          ? 'bg-blue-600 text-white'
                          : 'bg-slate-100 text-slate-900'
                      }`}
                    >
                      <p className="text-sm">{message.text}</p>
                      <p
                        className={`text-xs mt-1 ${
                          message.senderId === user?.uid
                            ? 'text-blue-100'
                            : 'text-slate-500'
                        }`}
                      >
                        {formatTime(message.timestamp)}
                      </p>
                    </div>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </CardContent>

            {/* Message Input */}
            <div className="border-t p-4">
              <form onSubmit={handleSendMessage} className="flex space-x-2">
                <Input
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Type your message..."
                  disabled={loading}
                  className="flex-1"
                />
                <Button type="submit" disabled={!newMessage.trim() || loading}>
                  <i className="fas fa-paper-plane"></i>
                </Button>
              </form>
            </div>
          </>
        ) : (
          <CardContent className="flex-1 flex items-center justify-center">
            <div className="text-center text-slate-500">
              <i className="fas fa-comments text-4xl mb-4"></i>
              <h3 className="text-lg font-semibold mb-2">Start a conversation</h3>
              <p>Select a friend from the list to begin chatting</p>
            </div>
          </CardContent>
        )}
      </Card>
    </div>
  );
}
