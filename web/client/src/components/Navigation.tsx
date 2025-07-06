import { Link, useLocation } from "wouter";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

export function Navigation() {
  const [location] = useLocation();
  const { userProfile, logout } = useAuth();

  const navItems = [
    { path: "/trips", label: "Trips", icon: "fas fa-route" },
    { path: "/create-trip", label: "Plan", icon: "fas fa-plus" },
    { path: "/friends", label: "Friends", icon: "fas fa-users" },
    { path: "/chat", label: "Chat", icon: "fas fa-comments" },
    { path: "/cars", label: "Cars", icon: "fas fa-car" },
  ];

  return (
    <>
      {/* Desktop Navigation */}
      <nav className="bg-white shadow-sm border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <Link href="/trips" className="flex items-center space-x-2">
                <img
                  src="https://img.playbook.com/ZaEM0JXSIrlzLvwyku0FZq41HLGuqrqc-EcF5vsOzIs/w:500/Z3M6Ly9icmFuZGlm/eS11c2VyY29udGVu/dC1kZXYvcHJvZC9w/cmV2aWV3cy8wYWU4/NzllNS1jOGE4LTQx/NjctOTcyYi1hYjNl/YjUzMGI2MDY.webp"
                  alt="Armigo Logo"
                  className="w-8 h-8 rounded-lg"
                />
                <h1 className="text-xl font-bold text-slate-900">Armigo</h1>
              </Link>
              <div className="hidden md:flex space-x-6">
                {navItems.map((item) => (
                  <Link
                    key={item.path}
                    href={item.path}
                    className={`font-medium transition-colors ${
                      location === item.path
                        ? "text-blue-600 border-b-2 border-blue-600"
                        : "text-slate-600 hover:text-blue-600"
                    }`}
                  >
                    <i className={`${item.icon} mr-2`}></i>
                    {item.label}
                  </Link>
                ))}
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Avatar className="h-8 w-8">
                  <AvatarFallback>
                    {userProfile?.username?.slice(0, 2).toUpperCase() || "U"}
                  </AvatarFallback>
                </Avatar>
                <span className="text-sm text-slate-600 hidden sm:block">
                  {userProfile?.username}
                </span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={logout}
                className="text-slate-600 hover:text-slate-900"
              >
                <i className="fas fa-sign-out-alt mr-1"></i>
                Logout
              </Button>
            </div>
          </div>
        </div>
      </nav>

      {/* Mobile Navigation */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 px-4 py-2 z-50">
        <div className="flex justify-around">
          {navItems.map((item) => (
            <Link
              key={item.path}
              href={item.path}
              className={`flex flex-col items-center space-y-1 transition-colors ${
                location === item.path
                  ? "text-blue-600"
                  : "text-slate-600 hover:text-blue-600"
              }`}
            >
              <i className={`${item.icon} text-lg`}></i>
              <span className="text-xs">{item.label}</span>
            </Link>
          ))}
        </div>
      </div>
    </>
  );
}
