import { useState, useEffect } from 'react';
import { ref, push, set, onValue, off } from 'firebase/database';
import { database } from '@/lib/firebase';
import { useAuth } from './useAuth';

export interface GroupChatMessage {
  messageId: string;
  senderId: string;
  senderUsername: string;
  text: string;
  timestamp: number;
}

export interface GroupChat {
  groupId: string;
  groupName: string;
  members: string[];
  memberDetails: { uid: string; username: string }[];
  createdAt: number;
  lastMessage?: GroupChatMessage;
  messages: GroupChatMessage[];
}

export function useGroupChat() {
  const { user, userProfile } = useAuth();
  const [groupChats, setGroupChats] = useState<GroupChat[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }

    // Listen to all group chats
    const groupChatsRef = ref(database, 'groupChats');
    const unsubscribe = onValue(groupChatsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const userGroupChats: GroupChat[] = [];
        
        Object.entries(data).forEach(([groupId, groupData]: [string, any]) => {
          // Only include groups where user is a member
          if (groupData.members && groupData.members.includes(user.uid)) {
            const messages = groupData.messages ? 
              Object.entries(groupData.messages).map(([messageId, messageData]: [string, any]) => ({
                messageId,
                ...messageData
              })).sort((a, b) => a.timestamp - b.timestamp) : [];

            const lastMessage = messages.length > 0 ? messages[messages.length - 1] : undefined;

            userGroupChats.push({
              groupId,
              groupName: groupData.groupName,
              members: groupData.members || [],
              memberDetails: groupData.memberDetails || [],
              createdAt: groupData.createdAt,
              lastMessage,
              messages
            });
          }
        });

        setGroupChats(userGroupChats.sort((a, b) => 
          (b.lastMessage?.timestamp || b.createdAt) - (a.lastMessage?.timestamp || a.createdAt)
        ));
      } else {
        setGroupChats([]);
      }
      setLoading(false);
    });

    return () => off(groupChatsRef);
  }, [user]);

  const createGroupChat = async (groupName: string, selectedFriends: { uid: string; username: string }[]): Promise<string | null> => {
    if (!user || !userProfile) throw new Error('Not authenticated');

    try {
      const groupId = push(ref(database, 'groupChats')).key!;
      const timestamp = Date.now();

      // Include current user in members
      const allMembers = [user.uid, ...selectedFriends.map(f => f.uid)];
      const allMemberDetails = [
        { uid: user.uid, username: userProfile.username },
        ...selectedFriends
      ];

      const groupData = {
        groupName,
        members: allMembers,
        memberDetails: allMemberDetails,
        createdAt: timestamp
      };

      const groupRef = ref(database, `groupChats/${groupId}`);
      await set(groupRef, groupData);

      return groupId;
    } catch (error) {
      console.error('Error creating group chat:', error);
      return null;
    }
  };

  const sendGroupMessage = async (groupId: string, text: string): Promise<boolean> => {
    if (!user || !userProfile) throw new Error('Not authenticated');

    try {
      const messageId = push(ref(database, `groupChats/${groupId}/messages`)).key!;
      const timestamp = Date.now();

      const messageData = {
        senderId: user.uid,
        senderUsername: userProfile.username,
        text,
        timestamp
      };

      const messageRef = ref(database, `groupChats/${groupId}/messages/${messageId}`);
      await set(messageRef, messageData);

      return true;
    } catch (error) {
      console.error('Error sending group message:', error);
      return false;
    }
  };

  const leaveGroupChat = async (groupId: string): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      const group = groupChats.find(g => g.groupId === groupId);
      if (!group) return false;

      // Remove user from members array
      const updatedMembers = group.members.filter(uid => uid !== user.uid);
      const updatedMemberDetails = group.memberDetails.filter(member => member.uid !== user.uid);

      const groupRef = ref(database, `groupChats/${groupId}`);
      await set(groupRef, {
        ...group,
        members: updatedMembers,
        memberDetails: updatedMemberDetails
      });

      return true;
    } catch (error) {
      console.error('Error leaving group chat:', error);
      return false;
    }
  };

  return {
    groupChats,
    loading,
    createGroupChat,
    sendGroupMessage,
    leaveGroupChat
  };
}