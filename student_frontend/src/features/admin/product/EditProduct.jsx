import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { TextField, Button, Box, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { API_URL } from "../../../constant";
import './EditProduct.css'; // CSS 파일 추가

const EditProduct = () => {
    const { productId } = useParams(); // URL에서 productId 추출
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);

    useEffect(() => {
        // 상품 데이터 가져오기
        fetch(`${API_URL}products/${productId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        })
            .then((response) => response.json())
            .then((data) => setProduct(data))
            .catch((error) => console.error("Error fetching product:", error));
    }, [productId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({
            ...product,
            [name]: value,
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // 상품 수정 요청 보내기
        fetch(`${API_URL}products/${productId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(product),
        })
            .then(() => navigate("/adminpage/products"))
            .catch((error) => console.error("Error updating product:", error));
    };

    if (!product) return <div>Loading...</div>;

    return (
        <Box sx={{ maxWidth: '600px', margin: '50px auto', padding: '20px', backgroundColor: '#fff', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', borderRadius: '8px' }}>
            <h2 className="edit-product-title">상품 수정</h2>
            <form className="edit-product-form" onSubmit={handleSubmit}>
                <FormControl fullWidth margin="normal">
                    <InputLabel>카테고리</InputLabel>
                    <Select
                        name="category"
                        value={product.category}
                        onChange={handleChange}
                        required
                    >
                        <MenuItem value="의류">의류</MenuItem>
                        <MenuItem value="전자기기">전자기기</MenuItem>
                        <MenuItem value="식품">식품</MenuItem>
                        <MenuItem value="가구">가구</MenuItem>
                        <MenuItem value="도서">도서</MenuItem>
                    </Select>
                </FormControl>

                <TextField
                    fullWidth
                    label="상품명"
                    name="name"
                    value={product.name}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="가격"
                    name="price"
                    type="number"
                    value={product.price}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="재고"
                    name="stock"
                    type="number"
                    value={product.stock}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="상품 상세 내용"
                    name="description"
                    value={product.description}
                    onChange={handleChange}
                    required
                    multiline
                    rows={4}
                    margin="normal"
                />

                <div className="image-upload-container">
                    {['image1', 'image2', 'image3'].map((imageName, index) => (
                        <div key={imageName} className="image-upload-box">
                            {product[imageName] ? (
                                <img className="image-preview" src={product[imageName]} alt={`Preview ${imageName}`} />
                            ) : (
                                <span>이미지 {index + 1}</span>
                            )}
                            <input
                                className="file-input"
                                type="file"
                                name={imageName}
                                onChange={(e) => handleImageChange(e)}
                                accept="image/*"
                            />
                        </div>
                    ))}
                </div>

                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary" sx={{ width: '48%' }}>
                        저장
                    </Button>
                    <Button variant="outlined" color="secondary" sx={{ width: '48%' }} onClick={() => navigate('/adminpage/products')}>
                        취소
                    </Button>
                </Box>
            </form>
        </Box>
    );
};

export default EditProduct;
