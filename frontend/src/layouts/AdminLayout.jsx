import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/layout/Header/Header';
import { Footer } from '../components/layout/Footer/Footer';

export const AdminLayout = () => {
  return (
    <div className="app-container">
      <Header />
      <div style={{ flex: 1 }}>
        <Outlet />
      </div>
      <Footer />
    </div>
  );
};
