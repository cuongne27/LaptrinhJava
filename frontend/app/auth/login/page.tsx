import React from "react";
import Header from "../../components/layout/Header";
import Container from "../../components/layout/Container";
import LoginForm from "../../components/auth/LoginForm";

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-page font-sans text-primary">
      <Header />
      <Container>
        <div className="flex items-center justify-center py-12">
          <LoginForm />
        </div>
      </Container>
    </div>
  );
}
