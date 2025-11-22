import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchCart = async () => {
    try {
      const response = await api.get('/cart');
      setCart(response.data.data);
    } catch (error) {
      if (error.response?.status === 404) {
          setCart(null);
      } else {
          console.error('Error fetching cart:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateQuantity = async (itemId, newQuantity) => {
    if (newQuantity < 1) return;
    try {
      const response = await api.put(`/cart/items/${itemId}`, { quantity: newQuantity });
      setCart(response.data.data);
    } catch (error) {
      toast.error('Failed to update quantity');
    }
  };

  const removeItem = async (itemId) => {
    try {
      const response = await api.delete(`/cart/items/${itemId}`);
      setCart(response.data.data);
      toast.success('Item removed');
    } catch (error) {
      toast.error('Failed to remove item');
    }
  };

  const clearCart = async () => {
    try {
       await api.delete('/cart');
       setCart(null);
       toast.success('Cart cleared');
    } catch (error) {
       toast.error('Failed to clear cart');
    }
  };

  if (loading) return <div className="text-center py-10">Loading cart...</div>;

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="text-center py-20 bg-white rounded-xl shadow-sm">
        <img src="https://cdn-icons-png.flaticon.com/512/11329/11329060.png" alt="Empty Cart" className="w-32 h-32 mx-auto mb-4 opacity-50" />
        <h2 className="text-2xl font-bold text-gray-800 mb-2">Your cart is empty</h2>
        <p className="text-gray-500 mb-6">Good food is always cooking! Go ahead and order some yummy items from the menu.</p>
        <Link to="/" className="bg-primary text-white px-6 py-3 rounded-lg font-bold hover:bg-red-600 transition">
          See Restaurants
        </Link>
      </div>
    );
  }

    const handleCheckout = async () => {
        try {
            // 1. Create Order
            const orderResponse = await api.post('/orders/place', {
                paymentMethod: "UPI", // Hardcode for now
                deliveryAddress: "Flat 402, Tech Park, Hyderabad"
            });

            toast.success("Order Placed Successfully! 🎉");

            // 2. Redirect to Order Success Page (Create a simple page later)
            // navigate('/orders/' + orderResponse.data.data.orderTrackingNumber);

            // 3. Clear local cart state
            setCart(null);

        } catch (error) {
            toast.error("Order Failed: " + error.message);
        }
    };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
      {/* Cart Items */}
      <div className="lg:col-span-2 space-y-4">
        <div className="flex justify-between items-center mb-4">
            <h1 className="text-2xl font-bold">Cart ({cart.totalItems} items)</h1>
            <button onClick={clearCart} className="text-red-500 text-sm font-bold hover:underline">Clear Cart</button>
        </div>

        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            <div className="p-4 border-b bg-gray-50 flex justify-between items-center">
                <div className="flex items-center gap-3">
                    <img
                        src="https://via.placeholder.com/40"
                        className="w-10 h-10 rounded bg-gray-200"
                        alt="Restaurant"
                    />
                    <div>
                        <h3 className="font-bold text-gray-900">{cart.restaurantName}</h3>
                        <p className="text-xs text-gray-500">Home Delivery</p>
                    </div>
                </div>
            </div>

            {cart.items.map((item) => (
                <div key={item.id} className="p-4 border-b last:border-0 flex gap-4">
                    <div className="h-3 w-3 mt-2 rounded-full bg-green-500 flex-shrink-0"></div>
                    <div className="flex-1">
                        <h4 className="font-medium text-gray-900">{item.itemName}</h4>
                        <p className="text-sm text-gray-500">₹{item.price}</p>
                    </div>
                    <div className="flex items-center gap-3 border rounded-lg px-2 py-1 h-fit bg-white">
                        <button
                            onClick={() => updateQuantity(item.id, item.quantity - 1)}
                            className="text-gray-500 font-bold px-2 hover:text-primary"
                        >
                            -
                        </button>
                        <span className="text-sm font-bold w-4 text-center">{item.quantity}</span>
                        <button
                            onClick={() => updateQuantity(item.id, item.quantity + 1)}
                            className="text-green-600 font-bold px-2 hover:text-green-700"
                        >
                            +
                        </button>
                    </div>
                    <div className="text-right w-20">
                        <p className="font-medium">₹{item.subtotal}</p>
                        <button
                            onClick={() => removeItem(item.id)}
                            className="text-xs text-red-500 mt-1 hover:underline"
                        >
                            Remove
                        </button>
                    </div>
                </div>
            ))}
        </div>

        <div className="bg-white rounded-xl shadow-sm p-4">
            <h3 className="font-bold text-gray-800 mb-2">Delivery Instructions</h3>
            <textarea
                placeholder="Write instructions for delivery boy..."
                className="w-full border rounded-lg p-2 text-sm focus:ring-2 focus:ring-primary focus:outline-none"
                rows="2"
            ></textarea>
        </div>
      </div>

      {/* Bill Details */}
      <div className="lg:col-span-1">
         <div className="bg-white rounded-xl shadow-sm p-6 sticky top-24">
             <h3 className="font-bold text-gray-900 mb-4 text-lg">Bill Details</h3>

             <div className="space-y-3 text-sm text-gray-600 mb-4 border-b pb-4">
                 <div className="flex justify-between">
                     <span>Item Total</span>
                     <span>₹{cart.totalAmount}</span>
                 </div>
                 <div className="flex justify-between">
                     <span>Delivery Fee</span>
                     <span>₹40</span>
                 </div>
                 <div className="flex justify-between">
                     <span>Platform Fee</span>
                     <span>₹5</span>
                 </div>
                 <div className="flex justify-between">
                     <span>GST and Restaurant Charges</span>
                     <span>₹{(cart.totalAmount * 0.05).toFixed(2)}</span>
                 </div>
             </div>

             <div className="flex justify-between font-bold text-lg text-gray-900 mb-6">
                 <span>To Pay</span>
                 <span>₹{(cart.totalAmount + 40 + 5 + (cart.totalAmount * 0.05)).toFixed(2)}</span>
             </div>

             <button
                className="w-full bg-green-600 text-white font-bold py-3 rounded-lg shadow-lg hover:bg-green-700 transition"
                onClick={() => handleCheckout()}
             >
                 PROCEED TO PAY
             </button>
         </div>
      </div>
    </div>
  );
};

export default Cart;
