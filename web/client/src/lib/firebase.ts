import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
  apiKey: "AIzaSyDjF5N0yninCw-UFsZwjxUb-4CQpBc6VeY",
  authDomain: "armigo-59057.firebaseapp.com",
  databaseURL: "https://armigo-59057-default-rtdb.firebaseio.com",
  projectId: "armigo-59057",
  storageBucket: "armigo-59057.firebasestorage.app",
  messagingSenderId: "330079094438",
  appId: "1:330079094438:web:12d171281a042b020e1667",
  measurementId: "G-ESEM2DGCNK",
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const database = getDatabase(app);

export default app;
