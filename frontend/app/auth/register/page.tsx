import React from 'react';
import Header from '../../components/layout/Header';
import Container from '../../components/layout/Container';
import RegisterForm from '../../components/auth/RegisterForm';

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-page font-sans text-primary">
      <Header />
      <Container>
        <div className="flex items-center justify-center py-12">
          <RegisterForm />
        </div>
      </Container>
    </div>
  );
}
