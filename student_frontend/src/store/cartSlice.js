import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { API_URL } from '@/utils/constants';
import { fetchWithAuth } from '@/features/auth/fetchWithAuth';

/**
 * 장바구니 아이템 목록을 가져오는 비동기 액션
 * @returns {Promise<Array>} 장바구니 아이템 목록
 */
export const fetchCartItems = createAsyncThunk(
  'cart/fetchCartItems',
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchWithAuth(`${API_URL}cart`);
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to fetch cart items: ${response.status} - ${errorText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Error fetching cart items:', error);
      return rejectWithValue(error.message);
    }
  }
);

/**
 * 장바구니에 상품을 추가하는 비동기 액션
 * @param {Object} cartItemDto - 장바구니에 추가할 상품 정보
 * @returns {Promise<Object>} 추가된 장바구니 아이템 정보
 */
export const addToCart = createAsyncThunk(
  'cart/addToCart',
  async (cartItemDto, { rejectWithValue }) => {
    try {
      const response = await fetchWithAuth(`${API_URL}cart`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(cartItemDto),
      });
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to add item to cart: ${response.status} - ${errorText}`);
      }
      return await response.json();
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

/**
 * 장바구니 아이템 수량을 수정하는 비동기 액션
 * @param {Object} params - 수정할 장바구니 아이템 정보
 * @param {number} params.cartItemId - 수정할 장바구니 아이템 ID
 * @param {number} params.count - 변경할 수량
 * @returns {Promise<number>} 수정된 장바구니 아이템 ID
 */
export const updateCartItem = createAsyncThunk(
  'cart/updateCartItem',
  async ({ cartItemId, count }, { rejectWithValue }) => {
    try {
      console.log('Updating cart item:', cartItemId, 'with count:', count);

      const response = await fetchWithAuth(`${API_URL}cart/${cartItemId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ count }), // 요청 바디에 count 를 담아서 보냄
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to update cart item: ${response.status} - ${errorText}`);
      }

      const updatedItem = await response.json();
      console.log('Updated cart item:', updatedItem);
      return updatedItem;
    } catch (error) {
      console.error('Error updating cart item:', error);
      return rejectWithValue(error.message);
    }
  }
);

/**
 * 장바구니 아이템을 삭제하는 비동기 액션
 * @param {number} cartItemId - 삭제할 장바구니 아이템 ID
 * @returns {Promise<number>} 삭제된 장바구니 아이템 ID
 */
export const removeCartItem = createAsyncThunk(
  'cart/removeCartItem',
  async (cartItemId, { rejectWithValue }) => {
    try {
      console.log('Removing cart item:', cartItemId);

      const response = await fetchWithAuth(`${API_URL}cart/${cartItemId}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to remove cart item: ${response.status} - ${errorText}`);
      }

      const removedItemId = await response.json();
      console.log('Removed cart item ID:', removedItemId);
      return removedItemId;
    } catch (error) {
      console.error('Error removing cart item:', error);
      return rejectWithValue(error.message);
    }
  }
);

/**
 * 장바구니 상품을 주문하는 비동기 액션
 * @param {Object} cartOrderRequestDto - 주문할 장바구니 아이템 정보
 * @returns {Promise<number>} 생성된 주문 ID
 */
export const orderCartItems = createAsyncThunk(
  'cart/orderCartItems',
  async (cartOrderRequestDto, { rejectWithValue }) => {
    try {
      const response = await fetchWithAuth(`${API_URL}cart/orders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(cartOrderRequestDto),
      });
      if (!response.ok) {
         const errorText = await response.text();
         throw new Error(`Failed to order cart items: ${response.status} - ${errorText}`);
      }
      return await response.json();
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

/**
 * 장바구니 슬라이스 정의
 */
const cartSlice = createSlice({
  name: 'cart',
  initialState: {
    items: [],
    status: 'idle',
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // 장바구니 아이템 불러오기 - 로딩 상태
      .addCase(fetchCartItems.pending, (state) => {
        state.status = 'loading';
      })
      // 장바구니 아이템 불러오기 - 성공
      .addCase(fetchCartItems.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload;
      })
      // 장바구니 아이템 불러오기 - 실패
      .addCase(fetchCartItems.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })
      // 장바구니에 아이템 추가 - 성공
      .addCase(addToCart.fulfilled, (state, action) => {
        state.items.push(action.payload);
      })
      // 장바구니 아이템 수량 업데이트 - 성공
      .addCase(updateCartItem.fulfilled, (state, action) => {
        console.log('Update action payload:', action.payload);
        const updatedItem = action.payload;
        const index = state.items.findIndex(item => item.cartItemId === updatedItem.cartItemId);
        if (index !== -1) {
          console.log('Updating item at index:', index);
          state.items[index] = { ...state.items[index], ...updatedItem };
        } else {
          console.log('Item not found in state:', updatedItem);
        }
      })

      // 장바구니에서 아이템 제거 - 성공
      .addCase(removeCartItem.fulfilled, (state, action) => {
        // action.payload는 삭제된 cartItemId
        state.items = state.items.filter(item => item.cartItemId !== action.payload);
      })
      // 장바구니 아이템 주문 - 성공
      .addCase(orderCartItems.fulfilled, (state) => {
        state.items = []; // 주문 완료 후 장바구니 비우기
      });
  },
});

export default cartSlice.reducer;
