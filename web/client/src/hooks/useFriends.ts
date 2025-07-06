import { useState, useEffect } from 'react';
import { ref, push, set, onValue, off } from 'firebase/database';
import { database } from '@/lib/firebase';
import { useAuth } from './useAuth';

export interface FriendRequest {
  requestId: string;
  fromUserId: string;
  fromUsername: string;
  toUserId: string;
  toUsername: string;
  timestamp: number;
  status: 'pending' | 'accepted' | 'rejected';
}

export interface Friend {
  uid: string;
  username: string;
  addedAt: number;
}

export function useFriends() {
  const { user, userProfile } = useAuth();
  const [friends, setFriends] = useState<Friend[]>([]);
  const [sentRequests, setSentRequests] = useState<FriendRequest[]>([]);
  const [receivedRequests, setReceivedRequests] = useState<FriendRequest[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }

    // Listen to friends list
    const friendsRef = ref(database, `friends/${user.uid}/list`);
    const friendsUnsubscribe = onValue(friendsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const friendsList = Object.entries(data).map(([uid, friendData]: [string, any]) => ({
          uid,
          username: friendData.username,
          addedAt: friendData.addedAt
        }));
        setFriends(friendsList);
      } else {
        setFriends([]);
      }
    });

    // Listen to sent requests
    const sentRef = ref(database, `friends/${user.uid}/sent`);
    const sentUnsubscribe = onValue(sentRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const sentList = Object.entries(data).map(([requestId, requestData]: [string, any]) => ({
          requestId,
          ...requestData
        }));
        setSentRequests(sentList);
      } else {
        setSentRequests([]);
      }
    });

    // Listen to received requests
    const receivedRef = ref(database, `friends/${user.uid}/received`);
    const receivedUnsubscribe = onValue(receivedRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const receivedList = Object.entries(data).map(([requestId, requestData]: [string, any]) => ({
          requestId,
          ...requestData
        }));
        setReceivedRequests(receivedList.filter(req => req.status === 'pending'));
      } else {
        setReceivedRequests([]);
      }
      setLoading(false);
    });

    return () => {
      off(friendsRef);
      off(sentRef);
      off(receivedRef);
    };
  }, [user]);

  const sendFriendRequest = async (targetUsername: string): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      // First, find the target user by username
      const usersRef = ref(database, 'users');
      return new Promise((resolve, reject) => {
        onValue(usersRef, (snapshot) => {
          const users = snapshot.val();
          let targetUser = null;
          
          if (users) {
            for (const [uid, userData] of Object.entries(users) as [string, any][]) {
              if (userData.username === targetUsername) {
                targetUser = { uid, ...userData };
                break;
              }
            }
          }

          if (!targetUser) {
            reject(new Error('User not found'));
            return;
          }

          if (targetUser.uid === user.uid) {
            reject(new Error('Cannot send friend request to yourself'));
            return;
          }

          // Check if already friends
          const isAlreadyFriend = friends.some(friend => friend.uid === targetUser.uid);
          if (isAlreadyFriend) {
            reject(new Error('Already friends with this user'));
            return;
          }

          // Check if request already sent
          const requestAlreadySent = sentRequests.some(req => req.toUserId === targetUser.uid && req.status === 'pending');
          if (requestAlreadySent) {
            reject(new Error('Friend request already sent'));
            return;
          }

          // Create friend request
          const requestId = push(ref(database, 'temp')).key!;
          const timestamp = Date.now();

          const requestData = {
            fromUserId: user.uid,
            fromUsername: userProfile?.username || '',
            toUserId: targetUser.uid,
            toUsername: targetUser.username,
            timestamp,
            status: 'pending'
          };

          // Save to both sent and received
          const sentRef = ref(database, `friends/${user.uid}/sent/${requestId}`);
          const receivedRef = ref(database, `friends/${targetUser.uid}/received/${requestId}`);

          Promise.all([
            set(sentRef, requestData),
            set(receivedRef, requestData)
          ]).then(() => {
            resolve(true);
          }).catch(reject);
        }, { onlyOnce: true });
      });
    } catch (error) {
      console.error('Error sending friend request:', error);
      return false;
    }
  };

  const acceptFriendRequest = async (request: FriendRequest): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      const timestamp = Date.now();

      // Add to both users' friend lists
      const myFriendRef = ref(database, `friends/${user.uid}/list/${request.fromUserId}`);
      const theirFriendRef = ref(database, `friends/${request.fromUserId}/list/${user.uid}`);

      // Update request status
      const sentRequestRef = ref(database, `friends/${request.fromUserId}/sent/${request.requestId}`);
      const receivedRequestRef = ref(database, `friends/${user.uid}/received/${request.requestId}`);

      await Promise.all([
        set(myFriendRef, {
          username: request.fromUsername,
          addedAt: timestamp
        }),
        set(theirFriendRef, {
          username: userProfile?.username || '',
          addedAt: timestamp
        }),
        set(sentRequestRef, { ...request, status: 'accepted' }),
        set(receivedRequestRef, { ...request, status: 'accepted' })
      ]);

      return true;
    } catch (error) {
      console.error('Error accepting friend request:', error);
      return false;
    }
  };

  const rejectFriendRequest = async (request: FriendRequest): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      // Update request status
      const sentRequestRef = ref(database, `friends/${request.fromUserId}/sent/${request.requestId}`);
      const receivedRequestRef = ref(database, `friends/${user.uid}/received/${request.requestId}`);

      await Promise.all([
        set(sentRequestRef, { ...request, status: 'rejected' }),
        set(receivedRequestRef, { ...request, status: 'rejected' })
      ]);

      return true;
    } catch (error) {
      console.error('Error rejecting friend request:', error);
      return false;
    }
  };

  const removeFriend = async (friendUid: string): Promise<boolean> => {
    if (!user) throw new Error('Not authenticated');

    try {
      // Remove from both users' friend lists
      const myFriendRef = ref(database, `friends/${user.uid}/list/${friendUid}`);
      const theirFriendRef = ref(database, `friends/${friendUid}/list/${user.uid}`);

      await Promise.all([
        set(myFriendRef, null),
        set(theirFriendRef, null)
      ]);

      return true;
    } catch (error) {
      console.error('Error removing friend:', error);
      return false;
    }
  };

  return {
    friends,
    sentRequests,
    receivedRequests,
    loading,
    sendFriendRequest,
    acceptFriendRequest,
    rejectFriendRequest,
    removeFriend
  };
}