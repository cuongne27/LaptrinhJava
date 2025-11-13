import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import Container from './Container';
import searchIcon from '@/public/search-icon.svg';

export default function Header() {
  return (
    <header className="bg-base w-full">
      {/* Top Bar */}
      <Container>
        <div className="flex items-center justify-between py-2">
          <div className="flex items-center">
            <Link href="/" aria-label="Về trang chủ" className="block">
              <Image
                src="/teslo.svg"
                alt="TESLO"
                width={120}
                height={40}
                className="h-8 w-auto"
                priority
              />
            </Link>
          </div>

          {/* Center: Search Bar */}
          <div className="mx-[48px] flex-1">
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                className="placeholder-muted w-full rounded-full bg-search py-2 pl-[56px] pr-[56px] text-sm text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 focus:ring-offset-[var(--color-background-base)]"
              />
              <Image
                src={searchIcon}
                alt="Search"
                width={20}
                height={20}
                className="absolute right-6 top-1/2 h-4 w-4 -translate-y-1/2 transform"
              />
            </div>
          </div>

          {/* Right: Icons */}
          <div className="flex items-center gap-10">
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
      <nav className="bg-primary w-full py-2">
        <Container>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <Link
                href="/"
                className="font-sans text-on-primary transition hover:opacity-80"
              >
                TRANG CHỦ
              </Link>
              <a
                href="#"
                className="font-sans text-on-primary transition hover:opacity-80"
              >
                XE ĐIỆN TESLO
              </a>
            </div>
          </div>
        </Container>
      </nav>
    </header>
  );
}
