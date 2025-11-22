import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import { PlusIcon, MinusIcon } from '@heroicons/react/24/solid';
import toast from 'react-hot-toast';

const RestaurantDetails = () => {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menu, setMenu] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [restRes, menuRes, cartRes] = await Promise.all([
          api.get(`/restaurants/${id}`),
          api.get(`/restaurants/${id}/menu-items/full-menu`),
          api.get('/cart').catch(() => ({ data: { data: null } })) // Handle 404 for empty cart
        ]);

        setRestaurant(restRes.data.data);
        setMenu(menuRes.data.data);
        setCart(cartRes.data.data);
      } catch (error) {
        console.error('Error fetching data:', error);
        toast.error('Failed to load restaurant details');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const addToCart = async (item) => {
    try {
      const response = await api.post('/cart/items', {
        restaurantId: id,
        menuItemId: item.id,
        itemName: item.name,
        price: item.price,
        quantity: 1,
        imageUrl: item.imageUrl
      });

      setCart(response.data.data);
      toast.success(`Added ${item.name} to cart`);
    } catch (error) {
      console.error('Error adding to cart:', error);
      toast.error(error.response?.data?.message || 'Failed to add item');
    }
  };

  if (loading) return <div className="text-center py-10">Loading menu...</div>;
  if (!restaurant) return <div className="text-center py-10">Restaurant not found</div>;

  return (
    <div>
      {/* Restaurant Header */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div className="flex flex-col md:flex-row gap-6">
          <img
            src={restaurant.imageUrl || 'https://via.placeholder.com/150'}
            alt={restaurant.name}
            className="w-full md:w-48 h-32 object-cover rounded-lg"
          />
          <div>
             <h1 className="text-3xl font-bold text-gray-900">{restaurant.name}</h1>
             <p className="text-gray-500 mt-1">{restaurant.cuisineTypes?.join(', ')}</p>
             <p className="text-gray-400 text-sm mt-1">{restaurant.address?.street}, {restaurant.address?.city}</p>

             <div className="flex items-center gap-4 mt-4">
               <span className="bg-green-100 text-green-800 font-bold px-2 py-1 rounded">
                 ★ {restaurant.rating}
               </span>
               <span className="text-gray-500">
                 {restaurant.deliveryTime || '30-40'} min • ₹{restaurant.deliveryFee} Delivery Fee
               </span>
             </div>
          </div>
        </div>
      </div>

      {/* Menu Sections */}
      <div className="space-y-8">
        {menu?.categories?.map((category) => (
          <div key={category.categoryId} id={`category-${category.categoryId}`}>
            <h2 className="text-2xl font-bold mb-4 text-gray-800">{category.categoryName}</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {category.items?.map((item) => (
                <div key={item.id} className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                        {item.isBestseller && <span className="text-xs font-bold text-yellow-600 bg-yellow-100 px-1 rounded">Bestseller</span>}
                        <span className={`h-3 w-3 rounded-full ${item.dietaryType === 'VEGETARIAN' ? 'bg-green-500' : 'bg-red-500'}`}></span>
                    </div>
                    <h3 className="font-bold text-lg text-gray-900">{item.name}</h3>
                    <p className="text-gray-900 font-medium mt-1">₹{item.price}</p>
                    <p className="text-gray-500 text-sm mt-2 line-clamp-2">{item.description}</p>
                  </div>

                  <div className="flex flex-col items-center justify-center relative w-32">
                    <img
                      src={item.imageUrl || 'https://via.placeholder.com/100'}
                      alt={item.name}
                      className="w-28 h-24 object-cover rounded-lg mb-2"
                    />
                    <button
                      onClick={() => addToCart(item)}
                      className="absolute -bottom-2 bg-white text-green-600 font-bold px-6 py-2 rounded shadow hover:shadow-md border border-gray-200"
                    >
                      ADD
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RestaurantDetails;
