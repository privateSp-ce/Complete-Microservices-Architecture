import { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import RestaurantDetails from './pages/RestaurantDetails';
import Cart from './pages/Cart';
import { Toaster } from 'react-hot-toast';

function App() {

  useEffect(() => {
      // Set default mock user ID if not present
      if (!localStorage.getItem('userId')) {
          localStorage.setItem('userId', '1');
      }
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/restaurants/:id" element={<RestaurantDetails />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
      <Toaster position="bottom-right" />
    </div>
  );
}

export default App;
