import React from 'react';
import Image from 'next/image';

export default function Header() {
  return (
    <header className="bg-white shadow-md p-4 flex items-center justify-between">
      <div className="flex items-center">
        <Image src="/tesla-logo.svg" alt="Tesla Logo" width={100} height={24} />
        <nav className="ml-10">
          <a href="#" className="text-gray-700 hover:text-blue-600 mx-2">TRANG CHỦ</a>
          <a href="#" className="text-gray-700 hover:text-blue-600 mx-2">XE ĐIỆN TESLO</a>
        </nav>
      </div>
      <div className="flex items-center">
        <div className="relative mr-4">
          <input type="text" placeholder="Search..." className="border rounded-full py-2 px-4 pl-10" />
          <Image src="/search-icon.svg" alt="Search" width={20} height={20} className="absolute left-3 top-1/2 transform -translate-y-1/2" />
        </div>
        <Image src="/phone-icon.svg" alt="Phone" width={24} height={24} className="mx-2" />
        <Image src="/bell-icon.svg" alt="Notifications" width={24} height={24} className="mx-2" />
        <Image src="/user-icon.svg" alt="User" width={24} height={24} className="mx-2" />
        <a href="#" className="text-gray-700 hover:text-blue-600 ml-4">HỖ TRỢ</a>
      </div>
    </header>
  );
}
