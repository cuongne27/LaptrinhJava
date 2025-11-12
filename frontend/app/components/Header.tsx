import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import Container from './Container';
import searchIcon from '@/public/search-icon.svg';

export default function Header() {
  return (
    <header className="bg-white w-full">
      {/* Top Bar */}
      <Container>
        <div className="flex items-center justify-between py-4">
          <div className="flex items-center">
            <Link href="/" aria-label="Về trang chủ" className="block">
              <Image
                src="/teslo.svg"
                alt="TESLO"
                width={120}
                height={40}
                className="h-10 w-auto"
                priority
              />
            </Link>
          </div>

          {/* Center: Search Bar */}
          <div className="mx-[75px] flex-1">
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                className="w-full rounded-full bg-gray-300/80 py-3 text-base text-black placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-white pl-[75px] pr-[75px]"
              />
              <Image
                src={searchIcon}
                alt="Search"
                width={20}
                height={20}
                className="absolute right-[28px] top-1/2 h-5 w-5 -translate-y-1/2 transform"
              />
            </div>
          </div>

          {/* Right: Icons */}
          <div className="flex items-center gap-[70px]">
            <button type="button" aria-label="Liên hệ" className="h-6 w-6">
              <Image
                src="/phone-icon.svg"
                alt="Liên hệ"
                width={24}
                height={24}
                className="h-6 w-6 cursor-pointer"
              />
            </button>
            <button type="button" aria-label="Thông báo" className="h-6 w-6">
              <Image
                src="/noti-icon.svg"
                alt="Thông báo"
                width={24}
                height={24}
                className="h-6 w-6 cursor-pointer"
              />
            </button>
            <button type="button" aria-label="Tài khoản" className="h-6 w-6">
              <Image
                src="/user-icon.svg"
                alt="Tài khoản"
                width={24}
                height={24}
                className="h-6 w-6 cursor-pointer"
              />
            </button>
          </div>
        </div>
      </Container>

      {/* Blue Navigation Bar */}
      <nav className="bg-blue-600 w-full py-3">
        <Container>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <Link href="/" className="text-white font-sans hover:text-gray-200 transition">
                TRANG CHỦ
              </Link>
              <a href="#" className="text-white font-sans hover:text-gray-200 transition">
                XE ĐIỆN TESLO
              </a>
            </div>
          </div>
        </Container>
      </nav>
    </header>
  );
}
