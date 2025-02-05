// src/features/admin/product/ViewProduct.jsx

import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

const ViewProduct = () => {
    const { productId } = useParams();  // URL에서 productId를 받아옵니다.
    const [product, setProduct] = useState(null);

    // 상품 상세 정보를 가져오는 함수 (API를 연결할 때 사용)
    useEffect(() => {
        const fetchProduct = async () => {
            try {
                // 여기에 실제 API 호출을 연결해서 상품 상세 정보를 가져옵니다.
                // 예: const response = await fetch(`/api/products/${productId}`);
                // const data = await response.json();

                // 임시 데이터 (실제 API를 연결할 때는 이 부분을 변경해야 합니다.)
                const data = {
                    id: productId,
                    name: 'Sample Product',
                    price: 100,
                    description: 'This is a sample product description.',
                };

                setProduct(data);
            } catch (error) {
                console.error('상품을 가져오는 데 오류가 발생했습니다.', error);
            }
        };

        fetchProduct();
    }, [productId]);  // productId가 바뀔 때마다 호출됩니다.

    if (!product) {
        return <div>상품을 불러오는 중...</div>;
    }

    return (
        <div>
            <h2>{product.name}</h2>
            <p>가격: {product.price}원</p>
            <p>상세 설명: {product.description}</p>
        </div>
    );
};

export default ViewProduct;
