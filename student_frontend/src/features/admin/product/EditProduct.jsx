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

    const [product, setProduct] = useState({
        name: '',
        description: '',
        price: '',
        stock: '',
        active: true,
        mainImageUrl: '',
    });

    const [imageFile, setImageFile] = useState(null);
    const [imagePreviewUrl, setImagePreviewUrl] = useState(''); // 이미지 미리보기 URL
    const [categories, setCategories] = useState([]);
    const [selectedCategoryIds, setSelectedCategoryIds] = useState([]);
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
                    mainImageUrl: productData.mainImageUrl || '',
                });
                setSelectedCategoryIds(productData.categories.map(cat => cat.id));

                // 이미지 미리보기 URL 설정
                if (productData.mainImageUrl) {
                    setImagePreviewUrl(productData.mainImageUrl);
                }

            } catch (error) {
                console.error("Error fetching data:", error);
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setProduct(prev => ({ ...prev, [name]: value }));
    };

    const handleCheckboxChange = (e) => {
        const { name, checked } = e.target;
        setProduct(prev => ({ ...prev, [name]: checked }));
    };

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        setImageFile(file);

        // 이미지 미리보기
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreviewUrl(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleImageDelete = () => {
        setImageFile(null);
        setImagePreviewUrl('');
        setProduct(prev => ({ ...prev, mainImageUrl: '' })); // mainImageUrl을 빈 문자열로 설정
    };

    const handleCategoryChange = (e) => {
        setSelectedCategoryIds(e.target.value);
    };

    const uploadImage = async () => {
        if (!imageFile) return null;

        const formData = new FormData();
        formData.append("imageFile", imageFile);

        try {
            const response = await axios.post(`${API_URL}upload`, formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            return response.data.imageUrl;
        } catch (error) {
            console.error("이미지 업로드 실패:", error);
            alert("이미지 업로드에 실패했습니다.");
            return null;
        }
    };



    const handleSubmit = async (event) => {
        event.preventDefault();

        // 만약 imageFile이 없고, 이미 사용자가 이미지 삭제 버튼을 눌렀다면
        let uploadedImageUrl = product.mainImageUrl; // 빈 문자열로 삭제된 경우 반영

        if (imageFile) {
            uploadedImageUrl = await uploadImage();
            if (!uploadedImageUrl) {
                return;
            }
        }

        const updateData = {
            ...product,
            mainImageUrl: uploadedImageUrl, // 삭제된 경우 빈 문자열을 전송
            categoryIds: selectedCategoryIds
        };

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

                {/* 이미지 미리보기 및 삭제 */}
                <Box margin="normal">
                    {imagePreviewUrl ? (
                        <Box>
                            <img src={imagePreviewUrl} alt="상품 이미지 미리보기" style={{ width: '100%', height: 'auto', maxHeight: '300px', objectFit: 'cover' }} />
                            <Button variant="outlined" color="secondary" onClick={handleImageDelete} sx={{ mt: 2 }}>이미지 삭제</Button>
                        </Box>
                    ) : (
                        <TextField
                            fullWidth
                            margin="normal"
                            type="file"
                            name="mainImageUrl"
                            onChange={handleImageChange}
                            InputLabelProps={{ shrink: true }}
                        />
                    )}
                </Box>

                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary" sx={{ width: '48%' }}>저장</Button>
                    <Button variant="outlined" color="secondary" sx={{ width: '48%' }} onClick={() => navigate('/adminpage/products')}>취소</Button>
                </Box>
            </form>
        </Box>
    );
};

export default EditProduct;
