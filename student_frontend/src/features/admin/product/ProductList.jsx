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

        // 로그인 후 토큰을 localStorage에서 가져옵니다.
        const token = localStorage.getItem('accessToken');  // 저장된 토큰을 가져옴

        fetch(`${API_URL}products?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '', // 토큰이 있으면 Authorization 헤더에 추가
            },
            credentials: 'include', // HttpOnly 쿠키를 포함해서 요청
        })
            .then((response) => {
                if (response.status === 401) {
                    // 인증 실패 시 처리
                    setSnackbarMessage('인증이 필요합니다. 다시 로그인해 주세요.');
                    setSnackbarOpen(true);
                }
                return response.json();
            })
            .then((data) => {
                setProducts(data.data || []);
                setTotalRows(data.total || 0);
            })
            .catch((error) => {
                console.error('Error fetching data:', error);
                setSnackbarMessage('상품 정보를 가져오는 데 실패했습니다.');
                setSnackbarOpen(true);
            });
    };

    const deleteProduct = (id) => {
        if (window.confirm('정말로 삭제하시겠습니까?')) {
            const token = localStorage.getItem('accessToken');

            fetch(`${API_URL}products/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : '', // 인증 토큰 포함
                },
            })
                .then((response) => {
                    if (response.ok) {
                        fetchProductsData();
                        setSnackbarMessage('상품이 삭제되었습니다.');
                        setSnackbarOpen(true);
                    } else if (response.status === 401) {
                        setSnackbarMessage('인증이 필요합니다. 다시 로그인해 주세요.');
                        setSnackbarOpen(true);
                    } else {
                        alert('상품 삭제 실패');
                    }
                })
                .catch((error) => console.error(error));
        }
    };

    const openEditModal = (product) => {
        setSelectedProduct(product);
        setIsEditModalOpen(true);
    };

    const closeEditModal = () => {
        setSelectedProduct(null);
        setIsEditModalOpen(false);
    };

    const updateProduct = (updatedProduct) => {
        const token = localStorage.getItem('accessToken');

        fetch(`${API_URL}products/${updatedProduct.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '', // 인증 토큰 포함
            },
            body: JSON.stringify(updatedProduct),
        })
            .then((response) => {
                if (response.ok) {
                    fetchProductsData();
                    setSnackbarMessage('상품 정보가 수정되었습니다.');
                    setSnackbarOpen(true);
                } else if (response.status === 401) {
                    setSnackbarMessage('인증이 필요합니다. 다시 로그인해 주세요.');
                    setSnackbarOpen(true);
                } else {
                    alert('상품 수정 실패');
                }
            })
            .catch((error) => console.error(error))
            .finally(() => closeEditModal());
    };

    const columns = [
        { field: 'id', headerName: 'ID', flex: 1 },
        { field: 'name', headerName: '상품명', flex: 2 },
        { field: 'price', headerName: '가격', flex: 2 },
        { field: 'category', headerName: '카테고리', flex: 2 },
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
            field: 'delete',
            headerName: '삭제',
            flex: 1,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="secondary"
                    onClick={() => deleteProduct(params.row.id)}
                >
                    삭제
                </Button>
            ),
        },
    ];

    return (
        <div style={{ height: 700, width: '100%' }}>
            <h3>상품 관리</h3>
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

            <Button variant="contained" onClick={() => navigate('/addProduct')}>
                상품 등록
            </Button>

            {isEditModalOpen && (
                <EditProduct
                    productData={selectedProduct}
                    updateProduct={updateProduct}
                    onClose={closeEditModal}
                />
            )}
        </div>
    );
};

export default ProductList;
