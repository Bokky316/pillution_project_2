import { DataGrid } from '@mui/x-data-grid';
import { Button, Snackbar } from '@mui/material';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";
import { useNavigate } from "react-router-dom";

const ProductList = ({ fetchProducts }) => {
    const [products, setProducts] = useState([]);
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [totalRows, setTotalRows] = useState(0);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 10 });
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchProductsData();
    }, [paginationModel]);

    const fetchProductsData = () => {
        const { page, pageSize } = paginationModel;
        const token = localStorage.getItem('accessToken');

        fetch(`${API_URL}products?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '',
            },
            credentials: 'include',
        })
            .then((response) => {
                if (response.status === 401) {
                    setSnackbarMessage('인증이 필요합니다. 다시 로그인해 주세요.');
                    setSnackbarOpen(true);
                }
                return response.json();
            })
            .then((data) => {
                console.log('Received product data:', JSON.stringify(data, null, 2));
                setProducts(data.dtoList || []);
                setTotalRows(data.total || 0);
            })
            .catch((error) => {
                console.error('Error fetching data:', error);
                setSnackbarMessage('상품 정보를 가져오는 데 실패했습니다.');
                setSnackbarOpen(true);
            });
    };

    const columns = [
        { field: 'id', headerName: 'ID', flex: 1 },
        { field: 'name', headerName: '상품명', flex: 2 },
        { field: 'price', headerName: '가격', flex: 2 },
        { field: 'category', headerName: '카테고리', flex: 2, renderCell: (params) => renderCategories(params.row.categories) },
        { field: 'stock', headerName: '재고', flex: 1 },
        {
            field: 'edit',
            headerName: '수정',
            flex: 1,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate(`/adminpage/products/${params.row.id}/edit`)}
                >
                    수정
                </Button>
            ),
        },
        {
            field: 'manage',
            headerName: '관리',
            flex: 1,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color={params.row.active === 1 ? "primary" : "secondary"}
                    onClick={() => toggleProductStatus(params.row.id, params.row.active)}
                >
                    {params.row.active === 1 ? '비활성화' : '활성화'}
                </Button>
            ),
        },
    ];

    const renderCategories = (categories) => {
      if (!categories || categories.length === 0) {
        return '-';
      }
      return categories.map(category => category.name).join(', ');
    };





    return (
        <div style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h3>상품 관리</h3>
                <Button variant="contained" color="primary" onClick={() => navigate('/adminpage/products/add')}>
                    상품 등록
                </Button>
            </div>
            <DataGrid
                rows={products}
                columns={columns}
                rowCount={totalRows}
                paginationMode="server"
                pageSizeOptions={[5, 10, 20]}
                paginationModel={paginationModel}
                onPaginationModelChange={(newModel) => setPaginationModel(newModel)}
                disableRowSelectionOnClick
            />

            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={() => setSnackbarOpen(false)}
                message={snackbarMessage}
            />
        </div>
    );
};

export default ProductList;
