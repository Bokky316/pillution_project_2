import { createSlice } from "@reduxjs/toolkit";

// Sidebar의 active 상태를 관리
const initialState = {
  activeMenu: "members", // 기본값으로 'members' 메뉴를 활성화
};

const sidebarSlice = createSlice({
  name: "sidebar",
  initialState,
  reducers: {
    setActiveMenu: (state, action) => {
      state.activeMenu = action.payload;
    },
  },
});

export const { setActiveMenu } = sidebarSlice.actions;
export default sidebarSlice.reducer;
