import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/hooks/useAuth';

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters')
});

const registerSchema = loginSchema.extend({
  username: z.string().min(3, 'Username must be at least 3 characters').max(20, 'Username must be less than 20 characters'),
  confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword']
});

type LoginForm = z.infer<typeof loginSchema>;
type RegisterForm = z.infer<typeof registerSchema>;

export function AuthForm() {
  const [isRegister, setIsRegister] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const { toast } = useToast();

  const loginForm = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: ''
    }
  });

  const registerForm = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: '',
      password: '',
      username: '',
      confirmPassword: ''
    }
  });

  const handleLogin = async (data: LoginForm) => {
    setLoading(true);
    try {
      await login(data.email, data.password);
      toast({
        title: 'Success',
        description: 'Logged in successfully!'
      });
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.message || 'Failed to login',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (data: RegisterForm) => {
    setLoading(true);
    try {
      await register(data.email, data.password, data.username);
      toast({
        title: 'Success',
        description: 'Account created successfully!'
      });
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create account',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="w-12 h-12 armenia-gradient rounded-lg flex items-center justify-center mx-auto mb-4">
            <i className="fas fa-mountain text-white text-xl"></i>
          </div>
          <CardTitle className="text-2xl">
            {isRegister ? 'Create Account' : 'Welcome Back'}
          </CardTitle>
          <CardDescription>
            {isRegister 
              ? 'Join Armenia Explorer to plan your adventure'
              : 'Sign in to your Armenia Explorer account'
            }
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isRegister ? (
            <form onSubmit={registerForm.handleSubmit(handleRegister)} className="space-y-4">
              <div>
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  {...registerForm.register('username')}
                  placeholder="Enter your username"
                />
                {registerForm.formState.errors.username && (
                  <p className="text-sm text-red-600 mt-1">
                    {registerForm.formState.errors.username.message}
                  </p>
                )}
              </div>
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  {...registerForm.register('email')}
                  placeholder="Enter your email"
                />
                {registerForm.formState.errors.email && (
                  <p className="text-sm text-red-600 mt-1">
                    {registerForm.formState.errors.email.message}
                  </p>
                )}
              </div>
              <div>
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  {...registerForm.register('password')}
                  placeholder="Create a password"
                />
                {registerForm.formState.errors.password && (
                  <p className="text-sm text-red-600 mt-1">
                    {registerForm.formState.errors.password.message}
                  </p>
                )}
              </div>
              <div>
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  {...registerForm.register('confirmPassword')}
                  placeholder="Confirm your password"
                />
                {registerForm.formState.errors.confirmPassword && (
                  <p className="text-sm text-red-600 mt-1">
                    {registerForm.formState.errors.confirmPassword.message}
                  </p>
                )}
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Creating Account...' : 'Create Account'}
              </Button>
            </form>
          ) : (
            <form onSubmit={loginForm.handleSubmit(handleLogin)} className="space-y-4">
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  {...loginForm.register('email')}
                  placeholder="Enter your email"
                />
                {loginForm.formState.errors.email && (
                  <p className="text-sm text-red-600 mt-1">
                    {loginForm.formState.errors.email.message}
                  </p>
                )}
              </div>
              <div>
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  {...loginForm.register('password')}
                  placeholder="Enter your password"
                />
                {loginForm.formState.errors.password && (
                  <p className="text-sm text-red-600 mt-1">
                    {loginForm.formState.errors.password.message}
                  </p>
                )}
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Signing In...' : 'Sign In'}
              </Button>
            </form>
          )}
          
          <div className="mt-6 text-center">
            <button
              type="button"
              onClick={() => setIsRegister(!isRegister)}
              className="text-blue-600 hover:text-blue-700 text-sm"
            >
              {isRegister 
                ? 'Already have an account? Sign in'
                : "Don't have an account? Create one"
              }
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
