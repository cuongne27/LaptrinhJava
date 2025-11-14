import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import Container from './Container';
import VehicleSearchBar from '../search/VehicleSearchBar';

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
            <VehicleSearchBar
              placeholder="Search..."
              onSearch={() => {}}
              inputClassName="py-2"
            />
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

