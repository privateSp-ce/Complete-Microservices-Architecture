import { Link } from 'react-router-dom';
import { ShoppingBagIcon, UserCircleIcon } from '@heroicons/react/24/outline';

const Navbar = () => {
  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="text-2xl font-bold text-primary">
            FoodExpress
          </Link>

          <div className="flex items-center space-x-6">
            <Link to="/cart" className="relative p-2 hover:bg-gray-100 rounded-full transition">
              <ShoppingBagIcon className="h-6 w-6 text-gray-600" />
              {/* Badge placeholder */}
              <span className="absolute top-0 right-0 bg-primary text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                0
              </span>
            </Link>

            <Link to="/login" className="flex items-center space-x-2 text-gray-600 hover:text-primary transition">
              <UserCircleIcon className="h-6 w-6" />
              <span className="font-medium">Login</span>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
