import React, { useState } from 'react';
import { TextField, Button, Select, MenuItem, FormControl, InputLabel, Box } from '@mui/material';
import { API_URL } from "../../../constant";
import { useNavigate } from "react-router-dom";
import './AddProduct.css';

const AddProduct = () => {
    const [product, setProduct] = useState({
        category: '',
        name: '',
        price: '',
        stock: '',
        description: '',
        active: 1
    });
    const [images, setImages] = useState({
        image1: null,
        image2: null,
        image3: null
    });
    const [imagePreviews, setImagePreviews] = useState({
        image1: null,
        image2: null,
        image3: null
    });
    const navigate = useNavigate();

    const categories = ['의류', '전자기기', '식품', '가구', '도서'];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({
            ...product,
            [name]: name === 'price' || name === 'stock' || name === 'active' ? Number(value) : value,
        });
    };

    const handleImageChange = (e) => {
        const { name, files } = e.target;
        if (files && files[0]) {
            setImages(prev => ({
                ...prev,
                [name]: files[0]
            }));
            setImagePreviews(prev => ({
                ...prev,
                [name]: URL.createObjectURL(files[0])
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('accessToken');

        const formData = new FormData();
        for (const key in product) {
            formData.append(key, product[key]);
        }
        for (const key in images) {
            if (images[key]) {
                formData.append(key, images[key]);
            }
        }

        try {
            const response = await fetch(`${API_URL}products`, {
                method: 'POST',
                headers: {
                    'Authorization': token ? `Bearer ${token}` : '',
                },
                body: formData,
            });

            if (response.ok) {
                alert('상품이 성공적으로 추가되었습니다.');
                navigate('/adminpage/products');
            } else {
                throw new Error('상품 추가에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert(error.message);
        }
    };

    return (
        <Box sx={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
            <h2>상품 추가</h2>
            <form onSubmit={handleSubmit}>
                <FormControl fullWidth margin="normal">
                    <InputLabel>카테고리</InputLabel>
                    <Select
                        name="category"
                        value={product.category}
                        onChange={handleChange}
                        required
                    >
                        <MenuItem value="" disabled>카테고리를 선택하세요</MenuItem>
                        {categories.map((category) => (
                            <MenuItem key={category} value={category}>{category}</MenuItem>
                        ))}
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
                <Box sx={{ mt: 2 }}>
                        <InputLabel sx={{ mb: 1 }}>상품 이미지</InputLabel>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            {['image1', 'image2', 'image3'].map((imageName, index) => (
                                <div key={imageName} className="image-upload-box">
                                    {imagePreviews[imageName] ? (
                                        <img className="image-preview" src={imagePreviews[imageName]} alt={`Preview ${imageName}`} />
                                    ) : (
                                        <span>이미지 {index + 1}</span>
                                    )}
                                    <input
                                        className="file-input"
                                        type="file"
                                        name={imageName}
                                        onChange={handleImageChange}
                                        accept="image/*"
                                    />
                                </div>
                            ))}
                        </Box>
                    </Box>

                <FormControl fullWidth margin="normal" variant="outlined">
                    <InputLabel shrink htmlFor="active-select">상품 활성화</InputLabel>
                    <Select
                        id="active-select"
                        name="active"
                        value={product.active}
                        onChange={handleChange}
                        label="상품 활성화"
                    >
                        <MenuItem value={1}>활성화</MenuItem>
                        <MenuItem value={0}>비활성화</MenuItem>
                    </Select>
                </FormControl>


                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary">
                        저장
                    </Button>
                    <Button variant="outlined" color="secondary" onClick={() => navigate('/adminpage/products')}>
                        취소
                    </Button>
                </Box>
            </form>
        </Box>
    );
};

export default AddProduct;
