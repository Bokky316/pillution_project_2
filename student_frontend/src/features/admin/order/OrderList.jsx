import { DataGrid } from '@mui/x-data-grid';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";

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

    const columns = [
        { field: 'id', headerName: '주문 ID', flex: 1 },
        { field: 'memberName', headerName: '회원 이름', flex: 2 },
        { field: 'productName', headerName: '상품명', flex: 2 },
        { field: 'quantity', headerName: '수량', flex: 1 },
        { field: 'totalPrice', headerName: '총 가격', flex: 2 },
    ];

    return (
        <div>
            <h3>주문 내역</h3>
            <DataGrid rows={orders} columns={columns} pageSize={10} />
        </div>
    );
};

export default OrderList;
