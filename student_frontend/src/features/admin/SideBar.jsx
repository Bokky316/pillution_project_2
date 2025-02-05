import React, { useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux"; // Redux 사용
import { setActiveMenu } from "../../redux/sidebarSlice"; // 액션 임포트
import "./SideBar.css";

const SideBar = () => {
  const location = useLocation();
  const dispatch = useDispatch();
  const activeMenu = useSelector((state) => state.sidebar.activeMenu); // Redux 상태에서 activeMenu 가져오기

  useEffect(() => {
    const path = location.pathname.split("/")[2]; // URL에서 메뉴 부분만 추출
    dispatch(setActiveMenu(path)); // URL에 맞는 메뉴를 active로 설정
  }, [location, dispatch]);

  return (
    <div className="sidebar">
      <h2 className="sidebar-title">관리자 메뉴</h2>
      <ul className="sidebar-menu">
        <li className={activeMenu === "members" ? "active" : ""}>
          <Link to="/adminPage/members">회원 관리</Link>
        </li>
        <li className={activeMenu === "orders" ? "active" : ""}>
          <Link to="/adminPage/orders">주문 관리</Link>
        </li>
        <li className={activeMenu === "products" ? "active" : ""}>
          <Link to="/adminPage/products">상품 관리</Link>
        </li>
      </ul>
    </div>
  );
};

export default SideBar;
