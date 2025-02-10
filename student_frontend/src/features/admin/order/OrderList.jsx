import { DataGrid } from '@mui/x-data-grid';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";
import { Select, MenuItem } from '@mui/material';

const OrderList = () => {
    const [orders, setOrders] = useState([]);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = () => {
        fetch(`${API_URL}orders`)
            .then((response) => response.json())
            .then((data) => setOrders(data))
            .catch((error) => console.error(error));
    };

    const handleShippingStatusChange = (e, orderId) => {
        const updatedOrders = orders.map(order =>
            order.id === orderId ? { ...order, shippingStatus: e.target.value } : order
        );
        setOrders(updatedOrders);
    };

    const columns = [
        { field: 'id', headerName: '주문 ID', flex: 1 },
        { field: 'memberName', headerName: '회원 이름', flex: 2 },
        { field: 'productName', headerName: '상품명', flex: 2 },
        { field: 'quantity', headerName: '수량', flex: 1 },
        { field: 'price', headerName: '가격', flex: 1 },
        { field: 'totalPrice', headerName: '총 가격', flex: 2 },
        { field: 'shippingAddress', headerName: '배송지', flex: 2 },
        {
            field: 'shippingStatus',
            headerName: '배송상태',
            flex: 2,
            renderCell: (params) => {
                return (
                    <Select
                        value={params.value || '결제대기'}
                        onChange={(e) => handleShippingStatusChange(e, params.row.id)}
                    >
                        <MenuItem value="결제대기">결제대기</MenuItem>
                        <MenuItem value="결제완료">결제완료</MenuItem>
                        <MenuItem value="상품대기">상품대기</MenuItem>
                        <MenuItem value="상품배송중">상품배송중</MenuItem>
                        <MenuItem value="상품도착">상품도착</MenuItem>
                    </Select>
                );
            }
        }
    ];

    return (
        <div>
            <h3>주문 내역</h3>
            <DataGrid rows={orders} columns={columns} pageSize={10} />
        </div>
    );
};

export default OrderList;
