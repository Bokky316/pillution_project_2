import React, { useState, useEffect } from 'react';

const EditProduct = ({ productData, updateProduct, onClose }) => {
    const [product, setProduct] = useState(productData || {});

    useEffect(() => {
        setProduct(productData); // 제품 데이터가 변경될 때마다 상태를 갱신
    }, [productData]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({
            ...product,
            [name]: value,
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        updateProduct(product);
    };

    return (
        <div>
            <h2>상품 수정</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>상품명:</label>
                    <input
                        type="text"
                        name="name"
                        value={product.name}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>가격:</label>
                    <input
                        type="number"
                        name="price"
                        value={product.price}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>상세 설명:</label>
                    <textarea
                        name="description"
                        value={product.description}
                        onChange={handleChange}
                        required
                    />
                </div>
                <button type="submit">상품 수정</button>
                <button type="button" onClick={onClose}>취소</button>
            </form>
        </div>
    );
};

export default EditProduct;
