import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
    TextField,
    Button,
    Box,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Checkbox,
    ListItemText
} from '@mui/material';
import { API_URL } from "../../../constant";
import './EditProduct.css';
import axios from "axios";

const EditProduct = () => {
    const { productId } = useParams();
    const navigate = useNavigate();

    // 모든 필드를 하나의 state로 관리
    const [product, setProduct] = useState({
        name: '',
        description: '',
        price: '',
        stock: '',
        active: true,
        mainImageUrl: '',
    });

    const [imageFile, setImageFile] = useState(null); // 이미지 파일 state
    const [categories, setCategories] = useState([]);
    const [selectedCategoryIds, setSelectedCategoryIds] = useState([]); // 선택된 카테고리 ID들
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

                console.log('카테고리 데이터:', categoriesData);
                console.log('상품 데이터:', productData);

                setCategories(categoriesData);
                setProduct({
                    name: productData.name || '',
                    description: productData.description || '',
                    price: productData.price || '',
                    stock: productData.stock || '',
                    active: productData.active,
                    mainImageUrl: productData.mainImageUrl || '',
                });
                // 상품에 연결된 카테고리 ID 설정
                setSelectedCategoryIds(productData.categories.map(cat => cat.id));

            } catch (error) {
                console.error("Error fetching data:", error);
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId]);

    // 일반적인 input 변경 처리
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setProduct(prev => ({ ...prev, [name]: value }));
    };

    // Checkbox 변경 처리
    const handleCheckboxChange = (e) => {
        const { name, checked } = e.target;
        setProduct(prev => ({ ...prev, [name]: checked }));
    };

    // 이미지 파일 선택 시 호출
    const handleImageChange = (e) => {
        const file = e.target.files[0];
        setImageFile(file); // 파일 state 업데이트
    };

    // 카테고리 변경 시 호출
    const handleCategoryChange = (e) => {
        setSelectedCategoryIds(e.target.value);
    };

    // 이미지 업로드 API 호출
    const uploadImage = async () => {
        if (!imageFile) return null;

        const formData = new FormData();
        formData.append("imageFile", imageFile);

        try {
            const response = await axios.post(`${API_URL}upload`, formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            return response.data.imageUrl; // 업로드된 이미지 URL 반환
        } catch (error) {
            console.error("이미지 업로드 실패:", error);
            alert("이미지 업로드에 실패했습니다.");
            return null;
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        let uploadedImageUrl = product.mainImageUrl; // 기존 이미지 URL 유지
        if (imageFile) {
            // 새 이미지가 선택된 경우에만 업로드
            uploadedImageUrl = await uploadImage();
            if (!uploadedImageUrl) {
                return; // 이미지 업로드 실패 시 중단
            }
        }

        const updateData = {
            ...product, // 기존 product 데이터 복사
            mainImageUrl: uploadedImageUrl, // 업로드된 이미지 URL 설정
            categoryIds: selectedCategoryIds // 선택된 카테고리 ID 리스트
        };

        console.log("백엔드로 보낼 updateData:", updateData);

        try {
            await axios.put(`${API_URL}products/${productId}`, updateData, {
                headers: { "Content-Type": "application/json" }
            });
            alert("상품이 성공적으로 업데이트되었습니다.");
            navigate('/adminpage/products');
        } catch (error) {
            console.error("Error updating product:", error);
            alert("상품 업데이트 중 오류가 발생했습니다.");
        }
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <Box sx={{ maxWidth: '600px', margin: '50px auto', padding: '20px', backgroundColor: '#fff', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', borderRadius: '8px' }}>
            <h2 className="edit-product-title">상품 수정</h2>
            <form className="edit-product-form" onSubmit={handleSubmit}>
                <FormControl fullWidth margin="normal">
                    <InputLabel id="multiple-checkbox-label">카테고리</InputLabel>
                    <Select
                        labelId="multiple-checkbox-label"
                        id="multiple-checkbox"
                        multiple
                        value={selectedCategoryIds}
                        onChange={handleCategoryChange}
                        renderValue={(selected) => {
                            const selectedCategories = categories.filter(cat => selected.includes(cat.id));
                            return selectedCategories.map(cat => cat.name).join(', ');
                        }}
                    >
                        {categories.map((category) => (
                            <MenuItem key={category.id} value={category.id}>
                                <Checkbox checked={selectedCategoryIds.includes(category.id)} />
                                <ListItemText primary={category.name} />
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>

                <TextField fullWidth label="상품명" name="name" value={product.name} onChange={handleInputChange} required margin="normal" />
                <TextField fullWidth label="가격" name="price" type="number" value={product.price} onChange={handleInputChange} required margin="normal" />
                <TextField fullWidth label="재고" name="stock" type="number" value={product.stock} onChange={handleInputChange} required margin="normal" />
                <TextField fullWidth label="상품 상세 내용" name="description" value={product.description} onChange={handleInputChange} required multiline rows={4} margin="normal" />

                <FormControl fullWidth margin="normal">
                    <InputLabel id="active-checkbox-label">활성 상태</InputLabel>
                    <Select
                        labelId="active-checkbox-label"
                        id="active-checkbox"
                        name="active"
                        value={product.active}
                        onChange={handleInputChange}
                    >
                        <MenuItem value={true}>활성</MenuItem>
                        <MenuItem value={false}>비활성</MenuItem>
                    </Select>
                </FormControl>

                <TextField
                    fullWidth
                    margin="normal"
                    type="file"
                    name="mainImageUrl"
                    onChange={handleImageChange}
                    InputLabelProps={{ shrink: true }}
                />

                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary" sx={{ width: '48%' }}>저장</Button>
                    <Button variant="outlined" color="secondary" sx={{ width: '48%' }} onClick={() => navigate('/adminpage/products')}>취소</Button>
                </Box>
            </form>
        </Box>
    );
};

export default EditProduct;
