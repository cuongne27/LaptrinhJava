import React from "react";
import Image from "next/image";

export default function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-200 font-sans">
      <div className="w-full max-w-md p-8 bg-gray-200 rounded-lg">
        <h2 className="text-3xl font-bold text-center mb-6 text-black">
          ĐĂNG NHẬP
        </h2>
        <form>
          <div className="mb-4">
            <label
              htmlFor="email"
              className="block text-gray-700 text-sm font-bold mb-2"
            >
              Email
            </label>
            <input
              type="email"
              id="email"
              className="appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline bg-white border-gray-400"
              placeholder="abc@gmail.com"
            />
          </div>
          <div className="mb-6">
            <label
              htmlFor="password"
              className="block text-gray-700 text-sm font-bold mb-2"
            >
              Mật Khẩu
            </label>
            <input
              type="password"
              id="password"
              className="appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline bg-white border-gray-400"
              placeholder="************"
            />
          </div>
          <div className="flex items-center justify-between mb-6">
            <label className="flex items-center text-gray-700 text-sm">
              <input type="checkbox" className="mr-2 leading-tight" />
              Nhớ tôi
            </label>
            <a
              href="#"
              className="inline-block align-baseline font-bold text-sm text-blue-600 hover:text-blue-800"
            >
              Quên mật khẩu?
            </a>
          </div>
          <button
            type="submit"
            className="bg-blue-700 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded w-full focus:outline-none focus:shadow-outline"
          >
            ĐĂNG NHẬP
          </button>
          <p className="text-center text-gray-700 text-sm mt-4">
            Không có tài khoản?{" "}
            <a
              href="#"
              className="font-bold text-blue-600 hover:text-blue-800"
            >
              Đăng ký Ngay.
            </a>
          </p>
        </form>
      </div>
    </div>
  );
}
