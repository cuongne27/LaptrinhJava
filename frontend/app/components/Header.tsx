import React from 'react';
import Container from './Container';

export default function Header() {
  return (
    <header className="bg-white w-full">
      {/* Top Bar */}
      <Container>
        <div className="flex items-center justify-between py-4">
          {/* Left: Logo */}
          <div className="flex items-center gap-2">
            <img 
              src="/teslo-logo.svg" 
              alt="TESLO Logo" 
              className="h-8 w-auto"
            />
            <img 
              src="/teslo.svg" 
              alt="TESLO" 
              className="h-6 w-auto"
            />
          </div>

          {/* Center: Search Bar */}
          <div className="flex-1 max-w-md mx-8">
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                className="w-full bg-gray-200 rounded py-2 px-4 pl-10 pr-10 text-black placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <svg
                className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
          </div>

          {/* Right: Icons */}
          <div className="flex items-center gap-4">
            <svg
              className="w-6 h-6 text-gray-700 cursor-pointer"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
              />
            </svg>
            <svg
              className="w-6 h-6 text-gray-700 cursor-pointer"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            <svg
              className="w-6 h-6 text-gray-700 cursor-pointer"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
          </div>
        </div>
      </Container>

      {/* Blue Navigation Bar */}
      <nav className="bg-blue-600 w-full py-3">
        <Container>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <a href="#" className="text-white font-sans hover:text-gray-200 transition">
                TRANG CHỦ
              </a>
              <a href="#" className="text-white font-sans hover:text-gray-200 transition">
                XE ĐIỆN TESLO
              </a>
            </div>
            <a href="#" className="text-white font-sans hover:text-gray-200 transition">
              HỖ TRỢ
            </a>
          </div>
        </Container>
      </nav>
    </header>
  );
}
