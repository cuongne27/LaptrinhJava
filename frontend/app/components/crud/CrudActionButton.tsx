"use client";

import { useState } from "react";

interface CrudActionButtonProps {
  onClick: () => void;
  className?: string;
}

export default function CrudActionButton({ 
  onClick, 
  className = "" 
}: CrudActionButtonProps) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <button
      type="button"
      onClick={onClick}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      className={`
        fixed bottom-8 right-8 z-50
        flex items-center justify-center
        w-14 h-14 rounded-full
        bg-gradient-to-r from-rose-500 to-pink-500
        text-white shadow-lg
        transition-all duration-300 ease-out
        hover:scale-110 hover:shadow-xl
        active:scale-95
        ${className}
      `}
      aria-label="Quản lý dữ liệu"
    >
      <svg
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={`transition-transform duration-300 ${isHovered ? 'rotate-90' : ''}`}
      >
        <path
          d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M18.5 2.50001C18.8978 2.10219 19.4374 1.87869 20 1.87869C20.5626 1.87869 21.1022 2.10219 21.5 2.50001C21.8978 2.89784 22.1213 3.4374 22.1213 4.00001C22.1213 4.56262 21.8978 5.10219 21.5 5.50001L12 15L8 16L9 12L18.5 2.50001Z"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      
      {/* Tooltip */}
      <div className={`
        absolute bottom-full right-0 mb-2 px-3 py-1
        bg-gray-800 text-white text-sm rounded-md
        whitespace-nowrap opacity-0 pointer-events-none
        transition-opacity duration-200
        ${isHovered ? 'opacity-100' : ''}
      `}>
        Quản lý dữ liệu
        <div className="absolute top-full right-4 -mt-1">
          <div className="border-4 border-transparent border-t-gray-800"></div>
        </div>
      </div>
    </button>
  );
}
