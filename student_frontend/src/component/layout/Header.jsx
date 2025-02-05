import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { AppBar, Toolbar, Button, IconButton, Menu, MenuItem, Box, Typography } from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import { Link, useNavigate } from "react-router-dom";
import { clearUser } from "@/redux/authSlice";
import { fetchWithAuth } from "@features/auth/utils/fetchWithAuth";
import { API_URL } from "@/constant";
import { persistor } from "@/redux/store";
import "../../assets/styles/header.css";

const Header = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { user, isLoggedIn } = useSelector(state => state.auth);
    const [anchorEl, setAnchorEl] = React.useState(null);

    const handleMenuOpen = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleMenuItemClick = (path) => {
        handleMenuClose();
        if (path === "/cart" && !isLoggedIn) {
            navigate("/login");
        } else {
            navigate(path);
        }
    };

    const handleLogout = async () => {
        try {
            await fetchWithAuth(`${API_URL}auth/logout`, {
                method: "POST",
            });
            dispatch(clearUser());
            await persistor.purge();
            window.location.href = "/";
        } catch (error) {
            console.error("로그아웃 실패:", error.message);
            alert("로그아웃 중 오류가 발생했습니다.");
        }
    };

    return (
        <AppBar position="static" className="nav-bar" sx={{
            width: '100vw',
            boxShadow: 'none',
            position: 'relative',
            left: '50%',
            right: '50%',
            marginLeft: '-50vw',
            marginRight: '-50vw',
            backgroundColor: 'transparent',  // 배경색을 투명하게 설정
        }}>
            <Toolbar sx={{
                minHeight: '80px',
                display: 'flex',
                justifyContent: 'space-between',
                maxWidth: '1280px',
                margin: '0 auto',
                width: '100%',
                backgroundColor: '#f4f4f4',  // 툴바의 배경색을 흰색으로 설정
                color: '#000000',  // 텍스트 색상을 검정색으로 설정
            }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <IconButton
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        onClick={handleMenuOpen}
                        sx={{ mr: 2 }}
                        id="menu-button"
                    >
                        <MenuIcon />
                    </IconButton>
                    <Menu
                        anchorEl={anchorEl}
                        open={Boolean(anchorEl)}
                        onClose={handleMenuClose}
                    >
                        <MenuItem onClick={() => handleMenuItemClick("/products")}>상품</MenuItem>
                        <MenuItem onClick={() => handleMenuItemClick("/recommendation")}>추천</MenuItem>
                        <MenuItem onClick={() => handleMenuItemClick("/cart")}>장바구니</MenuItem>
                        <MenuItem onClick={() => handleMenuItemClick("/survey")}>설문조사</MenuItem>
                    </Menu>
                </Box>

                <Box sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center' }}>
                    <Link to="/" style={{ textDecoration: 'none' }}>
                        <img
                            src="/src/assets/images/logo.png"
                            alt="Pillution Logo"
                            style={{ height: '50px', verticalAlign: 'middle' }}
                        />
                    </Link>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                   {isLoggedIn && user ? (
                      <>
                        <Typography variant="body1" sx={{ mr: 2 }}>
                          {user.name}
                          {user?.roles?.includes("ROLE_ADMIN") ? " (관리자)" : " (사용자)"}
                        </Typography>
                        <Button color="inherit" onClick={() => navigate("/mypage")}>마이페이지</Button>
                        {/* 관리자일 경우 관리자 페이지로 가는 버튼 추가 */}
                        {user?.roles?.includes("ROLE_ADMIN") && (
                            <Button color="inherit" component={Link} to="/adminpage">관리자 페이지</Button>
                        )}
                        <Button color="inherit" onClick={handleLogout}>
                          {user.social ? '소셜 로그아웃' : '로그아웃'}
                        </Button>
                      </>
                    ) : (
                        <>
                            <Button color="inherit" onClick={() => navigate("/login")} sx={{ mr: 1 }}>로그인</Button>
                            <Button color="inherit" onClick={() => navigate("/registerMember")}>회원가입</Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Header;
