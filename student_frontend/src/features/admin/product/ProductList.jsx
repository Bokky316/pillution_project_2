import { DataGrid } from '@mui/x-data-grid';
import { Button, Snackbar, Select, MenuItem, FormControl, InputLabel, TextField } from '@mui/material';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";
import { useNavigate } from "react-router-dom";

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [totalRows, setTotalRows] = useState(0);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 10 });

  // 필터/검색 관련 상태
  const [filterType, setFilterType] = useState('전체');   // "전체" 또는 "카테고리"
  const [categories, setCategories] = useState([]);         // 카테고리 목록(배열)
  const [selectedCategory, setSelectedCategory] = useState('');

  // 사용자가 입력하는 검색어와 검색 유형 (실시간 조회되지 않고 버튼 클릭 시 적용)
  const [searchText, setSearchText] = useState('');
  const [searchField, setSearchField] = useState('상품명');
  // 실제로 목록 조회에 사용될 검색어와 유형
  const [appliedSearchText, setAppliedSearchText] = useState('');
  const [appliedSearchField, setAppliedSearchField] = useState('상품명');

  const navigate = useNavigate();
  const token = localStorage.getItem('accessToken');

  // 분류 기준이 "카테고리"면, 카테고리 목록을 가져옴 (토큰 포함)
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
          // 응답이 배열이 아닐 경우 대비하여 처리
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

  // 검색, 필터, 페이지네이션 – 단, 검색 관련 상태는 applied 상태를 사용하여
  useEffect(() => {
    fetchProductsData();
  }, [paginationModel, filterType, selectedCategory, appliedSearchText, appliedSearchField]);

  // 제품 조회 API (검색어가 적용되어 있으면 검색 API, 아니면 필터 API)
  const fetchProductsData = () => {
    const { page, pageSize } = paginationModel;
    let url = `${API_URL}products?page=${page}&size=${pageSize}`;

    // 검색어가 있다면 검색 API를 호출
    if (appliedSearchText.trim() !== '') {
      url = `${API_URL}products/search?page=${page}&size=${pageSize}&field=${encodeURIComponent(appliedSearchField)}&query=${encodeURIComponent(appliedSearchText)}`;
    }
    // 검색어가 없고 필터분류가 "카테고리"인 경우 선택된 카테고리로 필터링
    else if (filterType === '카테고리' && selectedCategory) {
      url = `${API_URL}products/filter?page=${page}&size=${pageSize}&categoryId=${selectedCategory}`;
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
        // 응답 구조에 따라 dtoList 또는 전체 데이터 사용
        setProducts(data.dtoList || data);
        setTotalRows(data.total || (data.dtoList ? data.dtoList.length : data.length) || 0);
      })
      .catch(error => {
        console.error('Error fetching data:', error);
        setSnackbarMessage('상품 정보를 가져오는 데 실패했습니다.');
        setSnackbarOpen(true);
      });
  };

  const toggleProductStatus = (id, currentActive) => {
    fetch(`${API_URL}products/${id}/toggle-active`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      credentials: 'include'
    })
      .then(() => fetchProductsData())
      .catch((error) => {
        console.error('Error toggling product status:', error);
        setSnackbarMessage('제품 상태 변경에 실패했습니다.');
        setSnackbarOpen(true);
      });
  };

  // 검색 버튼 클릭 시, 입력된 검색어/유형을 적용 상태에 반영
  const handleSearchClick = () => {
    setAppliedSearchText(searchText);
    setAppliedSearchField(searchField);
    // 변경된 appliedSearch 상태에 따라 useEffect에서 조회됨.
  };

  const columns = [
    { field: 'id', headerName: 'ID', flex: 1 },
    { field: 'name', headerName: '상품명', flex: 2 },
    { field: 'price', headerName: '가격', flex: 2 },
    {
      field: 'category',
      headerName: '카테고리',
      flex: 2,
      renderCell: (params) => (params.row.categories ? params.row.categories.map(c => c.name).join(', ') : '-')
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
      renderCell: (params) => (
        <Button
          variant="contained"
          color={params.row.active === 1 ? "primary" : "secondary"}
          onClick={() => toggleProductStatus(params.row.id, params.row.active)}
        >
          {params.row.active === 1 ? '비활성화' : '활성화'}
        </Button>
      )
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
        {/* 첫번째 드롭다운: 분류 기준 */}
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

        {/* 두번째 드롭다운: 카테고리 목록 (filterType이 '카테고리'일 경우 활성화) */}
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

        {/* 세번째 드롭다운: 검색 유형 */}
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

        {/* 검색어 입력창 */}
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
