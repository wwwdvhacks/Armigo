import { useState, useEffect } from 'react';
import { ref, push, onValue, off, get } from 'firebase/database';
import { database } from '@/lib/firebase';
import { useAuth } from './useAuth';

export interface Message {
  messageId: string;
  senderId: string;
  text: string;
  timestamp: number;
}

export interface Chat {
  chatId: string;
  participants: string[];
  lastMessage?: Message;
  messages: Message[];
}

export function useChat() {
  const { user } = useAuth();
  const [chats, setChats] = useState<Record<string, Chat>>({});
  const [loading, setLoading] = useState(true);

  const generateChatId = (uid1: string, uid2: string): string => {
    return [uid1, uid2].sort().join('_');
  };

  const startChat = async (targetUserId: string) => {
    if (!user) throw new Error('User must be logged in');
    
    const chatId = generateChatId(user.uid, targetUserId);
    const chatRef = ref(database, `chats/${chatId}`);
    
    // Check if chat already exists
    const snapshot = await get(chatRef);
    if (!snapshot.exists()) {
      // Initialize empty chat
      await push(ref(database, `chats/${chatId}/messages`), {
        senderId: 'system',
        text: 'Chat started',
        timestamp: Date.now()
      });
    }
    
    return chatId;
  };

  const sendMessage = async (chatId: string, text: string) => {
    if (!user) throw new Error('User must be logged in');
    
    const messagesRef = ref(database, `chats/${chatId}/messages`);
    const message: Omit<Message, 'messageId'> = {
      senderId: user.uid,
      text,
      timestamp: Date.now()
    };
    
    await push(messagesRef, message);
  };

  const loadMessages = (chatId: string, callback: (messages: Message[]) => void) => {
    const messagesRef = ref(database, `chats/${chatId}/messages`);
    
    const unsubscribe = onValue(messagesRef, (snapshot) => {
      if (snapshot.exists()) {
        const messagesData = snapshot.val();
        const messages = Object.entries(messagesData).map(([messageId, messageData]: [string, any]) => ({
          messageId,
          ...messageData
        })).sort((a, b) => a.timestamp - b.timestamp);
        
        callback(messages);
      } else {
        callback([]);
      }
    });

    return () => off(messagesRef, 'value', unsubscribe);
  };

  // Get list of users for friend list
  const getUsers = async () => {
    const usersRef = ref(database, 'users');
    const snapshot = await get(usersRef);
    
    if (snapshot.exists()) {
      const usersData = snapshot.val();
      return Object.values(usersData).filter((userData: any) => userData.uid !== user?.uid);
    }
    
    return [];
  };

  return {
    chats,
    loading,
    generateChatId,
    startChat,
    sendMessage,
    loadMessages,
    getUsers
  };
}
