import { DataGrid } from '@mui/x-data-grid';
import { Button, Snackbar, Select, MenuItem, FormControl, InputLabel, TextField } from '@mui/material';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";
import { useNavigate } from "react-router-dom";
import ViewProduct from './ViewProduct'; // ViewProduct 모달 컴포넌트 import

const ProductList = () => {
    const [products, setProducts] = useState([]);
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [totalRows, setTotalRows] = useState(0);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 10 });

    // 필터/검색 관련 상태 등...
    const [filterType, setFilterType] = useState('전체');
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [searchText, setSearchText] = useState('');
    const [searchField, setSearchField] = useState('상품명');
    const [appliedSearchText, setAppliedSearchText] = useState('');
    const [appliedSearchField, setAppliedSearchField] = useState('상품명');

    // 모달 관련 상태 : 선택된 상품 ID와 모달 open 상태
    const [selectedProductId, setSelectedProductId] = useState(null);
    const [modalOpen, setModalOpen] = useState(false);

    const navigate = useNavigate();
    const token = localStorage.getItem('accessToken');

    // 카테고리 목록 가져오기
    useEffect(() => {
        if (filterType === '카테고리') {
            fetch(`${API_URL}categories`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : ''
                },
                credentials: 'include'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('카테고리 조회 실패');
                    }
                    return response.json();
                })
                .then(data => {
                    setCategories(Array.isArray(data) ? data : []);
                })
                .catch(err => {
                    console.error("카테고리 조회 실패:", err);
                    setCategories([]);
                });
        } else {
            setCategories([]);
            setSelectedCategory('');
        }
    }, [filterType, token]);

    // 제품 목록 조회 – 검색/필터/페이지네이션
    useEffect(() => {
        fetchProductsData();
    }, [paginationModel, filterType, selectedCategory, appliedSearchText, appliedSearchField]);

    const fetchProductsData = () => {
            const { page, pageSize } = paginationModel;

            // 기본 전체 상품 조회 URL
            let url = `${API_URL}products?page=${page}&size=${pageSize}`;

            // 검색어가 있는 경우
            if (appliedSearchText.trim() !== '') {
                url = `${API_URL}products/search?page=${page}&size=${pageSize}&field=${encodeURIComponent(appliedSearchField)}&query=${encodeURIComponent(appliedSearchText)}`;
            }
            // 카테고리 필터링만 있는 경우
            else if (filterType === '카테고리' && selectedCategory) {
                url = `${API_URL}products/filter?categoryId=${selectedCategory}`;
            }

            fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : ''
                },
                credentials: 'include'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error: ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Received product data:', data);

                    // filter API와 search API의 응답 구조가 다르므로 분기 처리
                    if (url.includes('/filter')) {
                        // filter API 응답 처리
                        setProducts(data);
                        setTotalRows(data.length);
                    } else {
                        // search API 또는 기본 목록 응답 처리
                        const productList = data.dtoList || data;
                        setProducts(productList);
                        setTotalRows(data.total || productList.length);
                    }
                })
                .catch(error => {
                    console.error('Error fetching data:', error);
                    setSnackbarMessage('상품 정보를 가져오는 데 실패했습니다.');
                    setSnackbarOpen(true);
                });
        };

        const toggleProductStatus = async (id, currentActive) => {
            try {
                const response = await fetch(`${API_URL}products/${id}/toggle-active`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': token ? `Bearer ${token}` : ''
                    },
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('상태 변경 실패');
                }

                // boolean 값을 토글
                setProducts(prevProducts =>
                    prevProducts.map(product =>
                        product.id === id
                            ? { ...product, active: !currentActive }
                            : product
                    )
                );

                setSnackbarMessage('상품 상태가 변경되었습니다.');
                setSnackbarOpen(true);

            } catch (error) {
                console.error('Error toggling product status:', error);
                setSnackbarMessage('제품 상태 변경에 실패했습니다.');
                setSnackbarOpen(true);
            }
        };

// 필터 타입 변경 시 처리
    const handleFilterTypeChange = (newFilterType) => {
        setFilterType(newFilterType);
        setSelectedCategory('');
        setSearchText('');
        setAppliedSearchText('');
        // 필터 타입이 변경되면 검색 조건 초기화
        setPaginationModel({ page: 0, pageSize: 10 });
    };

    // 카테고리 선택 시 처리
    const handleCategoryChange = (newCategory) => {
        setSelectedCategory(newCategory);
        // 카테고리가 선택되면 검색 조건 초기화
        setSearchText('');
        setAppliedSearchText('');
        setPaginationModel({ page: 0, pageSize: 10 });
    };

    // 검색 버튼 클릭 시 처리
    const handleSearchClick = () => {
        if (searchText.trim() !== '') {
            // 검색 시 카테고리 필터 초기화
            setFilterType('전체');
            setSelectedCategory('');
        }
        setAppliedSearchText(searchText);
        setAppliedSearchField(searchField);
        setPaginationModel({ page: 0, pageSize: 10 });
    };

    // 여기는 상품명을 클릭했을 때 모달을 여는 함수입니다.
    const handleOpenModal = (productId) => {
        setSelectedProductId(productId);
        setModalOpen(true);
    };

    // DataGrid의 컬럼 정의 – 상품명 열의 renderCell 함수에서 모달 오픈
    const columns = [
        { field: 'id', headerName: 'ID', flex: 1 },
        {
            field: 'name',
            headerName: '상품명',
            flex: 2,
            renderCell: (params) => (
                <Button onClick={() => handleOpenModal(params.row.id)} color="primary">
                    {params.row.name}
                </Button>
            )
        },
        { field: 'price', headerName: '가격', flex: 2 },
        {
            field: 'category',
            headerName: '카테고리',
            flex: 2,
            renderCell: (params) => (
                params.row.categories ? params.row.categories.map(c => c.name).join(', ') : '-'
            )
        },
        { field: 'stock', headerName: '재고', flex: 1 },
        {
            field: 'edit',
            headerName: '수정',
            flex: 1,
            renderCell: (params) => (
                <Button variant="contained" color="primary" onClick={() => navigate(`/adminpage/products/${params.row.id}/edit`)}>
                    수정
                </Button>
            )
        },
        {
                    field: 'manage',
                                headerName: '관리',
                                flex: 1,
                                renderCell: (params) => {
                                    // boolean 타입으로 처리
                                    const isActive = params.row.active;
                                    return (
                                        <Button
                                            variant="contained"
                                            color={isActive ? "primary" : "secondary"}
                                            onClick={() => toggleProductStatus(params.row.id, params.row.active)}
                                        >
                                            {isActive ? '비활성화하기' : '활성화하기'}
                                        </Button>
                                    );
                                }
                            }
                        ];


    return (
        <div style={{ width: '100%' }}>
            {/* 헤더 영역 */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h3>상품 관리</h3>
                <Button variant="contained" color="primary" onClick={() => navigate('/adminpage/products/add')}>
                    상품 등록
                </Button>
            </div>
            {/* 필터 및 검색 UI 영역 */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', marginBottom: '16px' }}>
                <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                    <InputLabel>분류 기준</InputLabel>
                    <Select
                        label="분류 기준"
                        value={filterType}
                        onChange={(e) => {
                            setFilterType(e.target.value);
                            setSelectedCategory('');
                        }}
                    >
                        <MenuItem value="전체">전체</MenuItem>
                        <MenuItem value="카테고리">카테고리</MenuItem>
                    </Select>
                </FormControl>
                <FormControl variant="outlined" sx={{ minWidth: 150 }} disabled={filterType !== '카테고리'}>
                    <InputLabel>카테고리</InputLabel>
                    <Select
                        label="카테고리"
                        value={selectedCategory}
                        onChange={(e) => setSelectedCategory(e.target.value)}
                    >
                        {categories.map((cat) => (
                            <MenuItem key={cat.id} value={cat.id}>{cat.name}</MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                    <InputLabel>검색 유형</InputLabel>
                    <Select
                        label="검색 유형"
                        value={searchField}
                        onChange={(e) => setSearchField(e.target.value)}
                    >
                        <MenuItem value="상품명">상품명</MenuItem>
                        <MenuItem value="카테고리">카테고리</MenuItem>
                        <MenuItem value="영양성분">영양성분</MenuItem>
                    </Select>
                </FormControl>
                <TextField
                    variant="outlined"
                    label="검색어"
                    value={searchText}
                    onChange={(e) => setSearchText(e.target.value)}
                />
                <Button variant="contained" color="primary" onClick={handleSearchClick}>
                    검색
                </Button>
            </div>

            {/* 제품 목록 DataGrid */}
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

            {/* 스낵바 메시지 */}
            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={() => setSnackbarOpen(false)}
                message={snackbarMessage}
            />

            {/* ViewProduct 모달 컴포넌트 - 선택한 상품의 상세 정보를 보여줌 */}
            <ViewProduct
                productId={selectedProductId}
                open={modalOpen}
                onClose={() => { setModalOpen(false); setSelectedProductId(null); }}
            />
        </div>
    );
};

export default ProductList;
