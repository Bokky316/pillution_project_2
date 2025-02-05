import { DataGrid } from '@mui/x-data-grid';
import { Button, Snackbar } from '@mui/material';
import { useState, useEffect } from 'react';
import { API_URL } from "../../../constant";
import { useNavigate } from "react-router-dom";

const MemberList = () => {
    const [members, setMembers] = useState([]);
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [searchType, setSearchType] = useState('name'); // 검색 분류 (기본값: 이름)
    const [searchInput, setSearchInput] = useState(''); // 검색 입력값
    const [totalRows, setTotalRows] = useState(0); // 총 회원 수
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 10 });
    const navigate = useNavigate();

    useEffect(() => {
        fetchMembersData();
    }, [statusFilter, paginationModel]);

    const fetchMembersData = () => {
        const { page, pageSize } = paginationModel;
        fetch(`${API_URL}members?status=${statusFilter}&page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                // 여기에 Authorization 헤더를 추가하고 싶다면 아래처럼 추가할 수 있습니다.
                // 'Authorization': `Bearer ${accessToken}`,
            },
            credentials: 'include', // HttpOnly 쿠키를 포함해서 요청
        })
            .then((response) => response.json())
            .then((data) => {
                setMembers(data.data || []);
                setTotalRows(data.total || 0);
            })
            .catch((error) => {
                console.error(error);
                setSnackbarMessage('회원 정보를 가져오는 데 실패했습니다.');
                setSnackbarOpen(true);
            });

    };

    // 검색 기능 실행 함수
    const handleSearch = () => {
        const token = localStorage.getItem("accToken"); // 또는 쿠키에서 가져오기

        fetch(`${API_URL}members/search?type=${searchType}&value=${searchInput}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // JWT 토큰 추가
            },
            credentials: 'include' // 쿠키 포함 (필요한 경우)
        })
            .then((response) => {
                if (response.status === 401) {
                    throw new Error("접근 권한이 없습니다. 관리자에게 문의하세요.");
                }
                return response.json();
            })
            .then((data) => {
                if (data.status === "success") {
                    setMembers(data.data || []);
                } else {
                    throw new Error(data.message || "검색 결과를 가져오는 데 실패했습니다.");
                }
            })
            .catch((error) => {
                console.error(error);
                setSnackbarMessage(error.message);
                setSnackbarOpen(true);
            });
    };



    const changeMemberStatus = (memberId, status) => {
        fetch(`${API_URL}members/${memberId}/status?status=${status}`, { method: 'PATCH' })
            .then((response) => {
                if (response.ok) {
                    fetchMembersData();
                    setSnackbarMessage('회원 상태가 변경되었습니다.');
                    setSnackbarOpen(true);
                }
            })
            .catch((error) => console.error(error));
    };

    const columns = [
        { field: 'id', headerName: 'ID', flex: 1 },
        { field: 'name', headerName: '이름', flex: 2 },
        { field: 'email', headerName: '이메일', flex: 2 },
        { field: 'dob', headerName: '생년월일', flex: 2 },
        { field: 'gender', headerName: '성별', flex: 1 },
        { field: 'phone', headerName: '휴대폰번호', flex: 2 },
        { field: 'status', headerName: '활성상태', flex: 2 },
        { field: 'subscription', headerName: '구독여부', flex: 2 },
        {
            field: 'edit',
            headerName: '관리',
            flex: 2,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate(`/adminpage/members/${params.row.id}/edit`)} // 올바른 경로
                >
                    수정
                </Button>

            ),
        },
    ];

    return (
        <div>
            <h3>회원 관리</h3>

            {/* 총 회원 수 */}
            <p>총 회원수 : {totalRows}</p>

            {/* 활성 상태 필터 */}
            <select onChange={(e) => setStatusFilter(e.target.value)} value={statusFilter}>
                <option value="">전체</option>
                <option value="ACTIVE">활성회원</option>
                <option value="INACTIVE">휴먼회원</option>
                <option value="DELETED">탈퇴회원</option>
                </select>

            {/* 검색 기능 */}
            <div style={{ marginTop: "10px", marginBottom: "20px" }}>
                <select onChange={(e) => setSearchType(e.target.value)} value={searchType}>
                <option value="name">이름</option>
                <option value="email">이메일</option>
            </select>
            <input
                type="text"
                placeholder="검색어를 입력하세요"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                style={{ marginLeft: "10px" }}
            />
            <button onClick={handleSearch} style={{ marginLeft: "10px" }}>
                검색
            </button>
        </div>

            <DataGrid
                rows={members}
                columns={columns}
                rowCount={totalRows}
                paginationMode="server"
                pageSizeOptions={[5, 10, 20]}
                paginationModel={paginationModel}
                onPaginationModelChange={(newModel) => setPaginationModel(newModel)}
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

export default MemberList;
