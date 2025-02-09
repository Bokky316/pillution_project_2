import React, { useState, useEffect } from 'react';
import { TextField, Button, Select, MenuItem, FormControl, InputLabel, Box } from '@mui/material';
import { API_URL } from '../../../constant';
import { useNavigate } from 'react-router-dom';
import './AddProduct.css';

const AddProduct = () => {
    const [product, setProduct] = useState({
        categoryIds: [],
        ingredientIds: [],
        name: '',
        price: '',
        stock: '',
        description: '',
        active: true,
    });

    const [images, setImages] = useState({
        mainImage: null,
    });
    const [imagePreviews, setImagePreviews] = useState({
        mainImage: null,
    });
    const navigate = useNavigate();

    // 카테고리 목록을 저장할 state
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        // 컴포넌트가 마운트될 때 카테고리 목록을 가져옴
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${API_URL}categories`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : '',
                },
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            setCategories(data);
        } else {
            throw new Error('카테고리 목록을 불러오는 데 실패했습니다.');
        }
        } catch (error) {
            console.error('카테고리 목록을 불러오는 중 오류가 발생했습니다.', error);
            alert(error.message);
        }
    };

    const availableIngredients = [
        { id: 1, name: '성분1' },
        { id: 2, name: '성분2' },
        { id: 3, name: '성분3' }
    ];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct(prev => ({
        ...prev,
        [name]: (name === 'price' || name === 'stock') ? Number(value) : value,
        }));
    };

    const handleCategoryChange = (e) => {
        const { value } = e.target;
        setProduct(prev => ({
            ...prev,
            categoryIds: value
        }));
    };

    const handleIngredientChange = (e) => {
        const { value } = e.target;
        setProduct(prev => ({
            ...prev,
            ingredientIds: value
        }));
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
        formData.append('product', new Blob([JSON.stringify(product)], { type: "application/json" }));
        if (images.mainImage) {
            formData.append('mainImage', images.mainImage);
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
                        name="categoryIds"
                        value={product.categoryIds}
                        onChange={handleCategoryChange}
                        multiple
                        required
                    >
                        {categories.map(cat => (
                            <MenuItem key={cat.id} value={cat.id}>{cat.name}</MenuItem>
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
                    <div className="image-upload-container">
                        <div className="image-upload-box">
                            <input
                                className="file-input"
                                type="file"
                                name="mainImage"
                                onChange={handleImageChange}
                                accept="image/*"
                            />
                            {imagePreviews.mainImage ? (
                                <img className="image-preview" src={imagePreviews.mainImage} alt="Preview" />
                            ) : (
                                <span>상품 이미지 선택</span>
                            )}
                        </div>
                    </div>
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
                        <MenuItem value={true}>활성화</MenuItem>
                        <MenuItem value={false}>비활성화</MenuItem>
                    </Select>
                </FormControl>
                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary">저장</Button>
                    <Button variant="outlined" color="secondary" onClick={() => navigate('/adminpage/products')}>취소</Button>
                </Box>
            </form>
        </Box>
    );
};

export default AddProduct;
