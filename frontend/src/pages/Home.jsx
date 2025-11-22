import { useState, useEffect } from 'react';
import api from '../services/api';
import { Link } from 'react-router-dom';
import { MagnifyingGlassIcon, AdjustmentsHorizontalIcon } from '@heroicons/react/24/outline'; // Install heroicons if not present

const Home = () => {
    const [restaurants, setRestaurants] = useState([]);
    const [loading, setLoading] = useState(true);

    // Fetch Logic (Same as yours)
    useEffect(() => {
        const fetchRestaurants = async () => {
            try {
                const response = await api.post('/restaurants/search', {}); // Ensure this endpoint exists in Backend
                setRestaurants(response.data.data.content || []); // Handle null safety
            } catch (error) {
                console.error('Error fetching restaurants:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchRestaurants();
    }, []);

    return (
        <div className="min-h-screen bg-gray-50">
            {/* 1. Hero Section (Zomato Style) */}
            <div className="bg-white py-10 px-4 shadow-sm text-center">
                <h1 className="text-4xl md:text-5xl font-extrabold text-gray-800 mb-4">
                    Order food from favourite restaurants
                </h1>
                <p className="text-gray-500 mb-8 text-lg">Taste the food that surprises you</p>

                {/* Search Bar */}
                <div className="max-w-2xl mx-auto relative flex items-center">
                    <MagnifyingGlassIcon className="h-6 w-6 text-gray-400 absolute left-4" />
                    <input
                        type="text"
                        placeholder="Search for restaurant, cuisine or a dish"
                        className="w-full pl-12 pr-4 py-4 rounded-lg border border-gray-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent text-gray-700"
                    />
                </div>
            </div>

            {/* 2. Filters & Content */}
            <div className="max-w-7xl mx-auto px-4 py-8">
                {/* Filter Pills */}
                <div className="flex gap-3 mb-8 overflow-x-auto pb-2">
                    {['Sort', 'Pure Veg', 'Fast Delivery', 'Rating 4.0+'].map((filter) => (
                        <button key={filter} className="flex items-center px-3 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 text-sm hover:bg-gray-50 font-medium shadow-sm whitespace-nowrap">
                            {filter === 'Sort' && <AdjustmentsHorizontalIcon className="h-4 w-4 mr-1" />}
                            {filter}
                        </button>
                    ))}
                </div>

                <h2 className="text-2xl font-bold text-gray-800 mb-6">Best Food in Hyderabad</h2>

                {/* 3. Restaurant Grid */}
                {loading ? (
                    <div className="text-center py-20 text-gray-500">Looking for great food...</div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                        {restaurants.map((restaurant) => (
                            <Link to={`/restaurants/${restaurant.id}`} key={restaurant.id} className="group block">
                                <div className="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-lg transition-all duration-300 border border-gray-100">
                                    <div className="relative h-56 overflow-hidden">
                                        <img
                                            src={restaurant.imageUrl || 'https://b.zmtcdn.com/data/pictures/chains/1/18412861/b047e7df773649054199d64a62587813.jpg'} // Use a generic food image if null
                                            alt={restaurant.name}
                                            className="w-full h-full object-cover transform group-hover:scale-105 transition duration-500"
                                        />
                                        {/* Delivery Time Overlay */}
                                        <div className="absolute bottom-4 right-4 bg-white/90 backdrop-blur-sm px-2 py-1 rounded text-xs font-bold shadow-sm">
                                            35 mins
                                        </div>
                                    </div>

                                    <div className="p-4">
                                        <div className="flex justify-between items-center mb-1">
                                            <h3 className="text-xl font-bold text-gray-900 truncate">{restaurant.name}</h3>
                                            <div className="bg-green-600 text-white text-sm font-bold px-2 py-0.5 rounded flex items-center gap-1">
                                                {restaurant.rating || '4.2'} <span className="text-xs">★</span>
                                            </div>
                                        </div>

                                        <div className="flex justify-between items-center text-gray-500 text-sm">
                                            <p className="truncate w-2/3">{restaurant.cuisineTypes?.join(', ')}</p>
                                            <p>₹300 for two</p>
                                        </div>

                                        <div className="mt-3 pt-3 border-t border-gray-100 flex items-center gap-2 text-xs text-gray-400 uppercase font-semibold tracking-wider">
                                            <img src="https://b.zmtcdn.com/data/o2_assets/4bf016f32f05d2648ee1cc43e242c3ba1617695354.png" className="h-4 w-4" alt=""/>
                                            {restaurant.city || 'Hyderabad'}
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Home;