import { useState, useEffect } from 'react';
import api from '../services/api';
import { Link } from 'react-router-dom';

const Home = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRestaurants = async () => {
      try {
        // Mock search request
        const response = await api.post('/restaurants/search', {});
        setRestaurants(response.data.data.content);
      } catch (error) {
        console.error('Error fetching restaurants:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchRestaurants();
  }, []);

  if (loading) return <div className="text-center py-10">Loading restaurants...</div>;

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8 text-gray-800">Best Food in Town</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {restaurants.map((restaurant) => (
          <Link to={`/restaurants/${restaurant.id}`} key={restaurant.id} className="block group">
            <div className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-xl transition duration-300">
              <div className="relative h-48 overflow-hidden">
                <img
                  src={restaurant.imageUrl || 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'}
                  alt={restaurant.name}
                  className="w-full h-full object-cover transform group-hover:scale-105 transition duration-500"
                />
                {restaurant.promoted && (
                    <span className="absolute top-4 left-4 bg-gray-800 text-white text-xs px-2 py-1 rounded uppercase tracking-wide">Promoted</span>
                )}
              </div>

              <div className="p-4">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-xl font-bold text-gray-900 group-hover:text-primary transition">{restaurant.name}</h3>
                  <span className="bg-green-100 text-green-800 text-xs font-bold px-2 py-1 rounded flex items-center">
                    ★ {restaurant.rating}
                  </span>
                </div>

                <p className="text-gray-500 text-sm mb-3 truncate">
                  {restaurant.cuisineTypes.join(', ')}
                </p>

                <div className="flex justify-between items-center text-sm text-gray-500 border-t pt-3">
                    <span>{restaurant.city}</span>
                    <span>20-30 min</span>
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Home;
