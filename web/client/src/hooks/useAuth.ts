import { useState, useEffect } from 'react';
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged,
  User
} from 'firebase/auth';
import { ref, set, get } from 'firebase/database';
import { auth, database } from '@/lib/firebase';

export interface UserProfile {
  uid: string;
  username: string;
  email: string;
  createdAt: number;
}

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      setUser(user);
      
      if (user) {
        // Fetch user profile from database
        const profileRef = ref(database, `users/${user.uid}`);
        const snapshot = await get(profileRef);
        if (snapshot.exists()) {
          setUserProfile(snapshot.val());
        }
      } else {
        setUserProfile(null);
      }
      
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const register = async (email: string, password: string, username: string) => {
    // Check username uniqueness
    const usernameRef = ref(database, 'usernames');
    const usernameSnapshot = await get(usernameRef);
    
    if (usernameSnapshot.exists()) {
      const usernames = usernameSnapshot.val();
      if (Object.values(usernames).includes(username)) {
        throw new Error('Username already exists');
      }
    }

    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    
    // Save profile to database
    const profile: UserProfile = {
      uid: user.uid,
      username,
      email,
      createdAt: Date.now()
    };
    
    await set(ref(database, `users/${user.uid}`), profile);
    await set(ref(database, `usernames/${user.uid}`), username);
    
    setUserProfile(profile);
    return user;
  };

  const login = async (email: string, password: string) => {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    return userCredential.user;
  };

  const logout = async () => {
    await signOut(auth);
  };

  return {
    user,
    userProfile,
    loading,
    register,
    login,
    logout
  };
}
