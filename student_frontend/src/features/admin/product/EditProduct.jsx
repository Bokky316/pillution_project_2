import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { API_URL } from "../../../constant";
import "./EditProduct.css"; // CSS 파일 추가

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
        <div className="edit-product-container">
            <h2 className="edit-product-title">상품 수정</h2>
            <form className="edit-product-form" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>상품명:</label>
                    <input
                        type="text"
                        name="name"
                        value={product.name || ""}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>가격:</label>
                    <input
                        type="number"
                        name="price"
                        value={product.price || ""}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>카테고리:</label>
                    <input
                        type="text"
                        name="category"
                        value={product.category || ""}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>재고:</label>
                    <input
                        type="number"
                        name="stock"
                        value={product.stock || ""}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>상세 설명:</label>
                    <textarea
                        name="description"
                        value={product.description || ""}
                        onChange={handleChange}
                        rows={4}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>이미지 URL:</label>
                    <input
                        type="text"
                        name="imageUrl"
                        value={product.imageUrl || ""}
                        onChange={handleChange}
                    />
                </div>
                {product.imageUrl && (
                    <div className="image-preview">
                        <img src={product.imageUrl} alt="상품 이미지 미리보기" />
                    </div>
                )}
                <div className="form-buttons">
                    <button type="submit" className="save-button">저장</button>
                    <button type="button" className="cancel-button" onClick={() => navigate("/adminpage/products")}>
                        취소
                    </button>
                </div>
            </form>
        </div>
    );
};

export default EditProduct;
