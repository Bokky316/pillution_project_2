import React, { useState } from 'react';

const AddProduct = () => {
    const [product, setProduct] = useState({
        name: '',
        price: '',
        description: '',
        active: 1, // 기본값을 활성화로 설정
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({
            ...product,
            [name]: value,
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('상품 추가:', product);
        // 여기서 API 호출로 상품을 추가하는 로직을 구현할 수 있습니다.
    };

    return (
        <div>
            <h2>상품 추가</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>상품 이름:</label>
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
                <div>
                    <label>상품 활성화:</label>
                    <select
                        name="active"
                        value={product.active}
                        onChange={handleChange}
                    >
                        <option value={1}>활성화</option>
                        <option value={0}>비활성화</option>
                    </select>
                </div>
                <button type="submit">상품 추가</button>
            </form>
        </div>
    );
};

export default AddProduct;
