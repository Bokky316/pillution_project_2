import React, { useState, useEffect } from 'react';
import { Typography, useMediaQuery, useTheme, Box } from "@mui/material";
import { Link } from "react-router-dom";
import "@/styles/footer.css";
import { useLocation } from "react-router-dom";

const Footer = () => {
    const [isBottom, setIsBottom] = useState(false);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
    const location = useLocation();
    const isAdminPage = location.pathname.toLowerCase().startsWith("/adminpage"); // ✅ 대소문자 구분 제거

    useEffect(() => {
        const handleScroll = () => {
            const documentHeight = document.documentElement.scrollHeight;
            const windowHeight = window.innerHeight;
            const scrollY = window.scrollY;

            if (scrollY + windowHeight >= documentHeight - 100) {
                setIsBottom(true);
            } else {
                setIsBottom(false);
            }
        };

        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    const footerStyle = {

        padding: isMobile ? '10px' : '20px',
        backgroundColor: '#f5f5f5',
        textAlign: 'center',
        position: 'relative',
        left: 0,
        bottom: 0,
        width: '100%',
        boxShadow: isBottom ? '0px -5px 10px rgba(0,0,0,0.2)' : 'none',
        transition: 'box-shadow 0.3s ease-in-out',
    };

    return (
        <footer style={footerStyle}>
            <Box sx={{ maxWidth: '600px', margin: '0 auto' }}>
                <Typography variant={isMobile ? "caption" : "body2"} color="text.secondary">
                    © 2025 Pillution. All rights reserved.
                </Typography>
                {!isMobile && (
                    <>
                        <Typography variant="body2" color="text.secondary">
                            상호 ㈜ 필루션 | 대표 장보키 | 사업자 등록번호 123-45-6789
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            주소 23316 건강특별시 헬스케어구 웰니스로 42, 필루션타워 7층
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            대표전화 010-1234-5678 | 통신판매업 신고 제 2025-건강헿스케어-0042 호
                        </Typography>
                    </>
                )}
                <Typography variant={isMobile ? "caption" : "body2"} color="text.secondary">
                    이메일 support@pillution.co.kr
                </Typography>
            </Box>
        </footer>
    );
};

export default Footer;
