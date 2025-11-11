import React from 'react';
import Header from '../../components/Header';
import Container from '../../components/Container';

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-white font-sans">
      <Header />

      <Container>
        <div className="flex items-center justify-center py-12">
          <div className="w-full max-w-lg bg-gray-200 rounded-lg p-8 md:p-12">
            {/* Title */}
            <h1 className="text-4xl font-bold text-black mb-8 text-center uppercase">
              ĐĂNG KÝ
            </h1>

            <form>
              {/* Email */}
              <div className="mb-6">
                <label
                  htmlFor="email"
                  className="block text-black text-sm font-medium mb-2"
                >
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  className="w-full py-3 px-4 bg-gray-100 rounded text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition"
                  placeholder="abc@gmail.com"
                />
              </div>

              {/* Password */}
              <div className="mb-6">
                <label
                  htmlFor="password"
                  className="block text-black text-sm font-medium mb-2"
                >
                  Mật Khẩu
                </label>
                <input
                  type="password"
                  id="password"
                  className="w-full py-3 px-4 bg-gray-100 rounded text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition"
                  placeholder="......................"
                />
              </div>

              {/* Confirm Password */}
              <div className="mb-6">
                <label
                  htmlFor="confirmPassword"
                  className="block text-black text-sm font-medium mb-2"
                >
                  Xác nhận Mật Khẩu
                </label>
                <input
                  type="password"
                  id="confirmPassword"
                  className="w-full py-3 px-4 bg-gray-100 rounded text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition"
                  placeholder="......................"
                />
              </div>

              {/* Remember */}
              <label className="flex items-center text-black text-sm mb-8 cursor-pointer">
                <input
                  type="checkbox"
                  className="w-4 h-4 mr-2 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                Nhớ tôi
              </label>

              {/* Submit */}
              <button
                type="submit"
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded uppercase transition duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                ĐĂNG KÝ
              </button>

            </form>
          </div>
        </div>
      </Container>
    </div>
  );
}
