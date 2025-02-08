import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { TextField, Button, Box, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { API_URL } from "../../../constant";
import './EditProduct.css';

const EditProduct = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState({
        name: '',
        description: '',
        price: '',
        stock: '',
        active: true,
        categories: [],
        mainImageUrl: ''
    });
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [categoriesResponse, productResponse] = await Promise.all([
                    fetch(`${API_URL}categories`, { credentials: 'include' }),
                    fetch(`${API_URL}products/${productId}`, { credentials: 'include' })
                ]);

                if (!categoriesResponse.ok || !productResponse.ok) {
                    const errorMessage = await productResponse.text();
                    throw new Error(errorMessage || 'Failed to fetch data');
                }

                const categoriesData = await categoriesResponse.json();
                const productData = await productResponse.json();

                setCategories(categoriesData);
                setProduct({
                    name: productData.name || '',
                    description: productData.description || '',
                    price: productData.price || '',
                    stock: productData.stock || '',
                    active: productData.active,
                    categories: productData.categories || [],
                    mainImageUrl: productData.mainImageUrl || ''
                });
            } catch (error) {
                console.error("Error fetching data:", error);
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name === "categories") {
            // 다중 카테고리 선택 처리
            setProduct(prevProduct => ({
                ...prevProduct,
                categories: Array.isArray(value) ? value.map(id => ({ id })) : []
            }));
        } else {
            setProduct(prevProduct => ({
                ...prevProduct,
                [name]: value,
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch(`${API_URL}products/${productId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: 'include',
                body: JSON.stringify({
                    ...product,
                    categoryIds: product.categories.map(cat => cat.id)
                })
            });

            if (!response.ok) {
                throw new Error('Failed to update product');
            }

            navigate("/adminpage/products");
        } catch (error) {
            console.error("Error updating product:", error);
            setError(error.message);
        }
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <Box sx={{ maxWidth: '600px', margin: '50px auto', padding: '20px', backgroundColor: '#fff', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', borderRadius: '8px' }}>
            <h2 className="edit-product-title">상품 수정</h2>
            <form className="edit-product-form" onSubmit={handleSubmit}>
                <FormControl fullWidth margin="normal">
                    <InputLabel>카테고리</InputLabel>
                    <Select
                        name="category"
                        value={product.category ? product.category.id : ''}
                        onChange={handleChange}
                        required
                    >
                        {categories.map(category => (
                            <MenuItem key={category.id} value={category.id}>{category.name}</MenuItem>
                        ))}
                    </Select>
                </FormControl>

                <TextField
                    fullWidth
                    label="상품명"
                    name="name"
                    value={product.name || ''}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="가격"
                    name="price"
                    type="number"
                    value={product.price || ''}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="재고"
                    name="stock"
                    type="number"
                    value={product.stock || ''}
                    onChange={handleChange}
                    required
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="상품 상세 내용"
                    name="description"
                    value={product.description || ''}
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