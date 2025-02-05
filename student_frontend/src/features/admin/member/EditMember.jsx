import { useState, useEffect } from 'react';
import { TextField, Button, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { API_URL } from "../../../constant";
import { useNavigate, useParams } from "react-router-dom";
import './EditMember.css'; // CSS 파일 추가

const EditMember = () => {
    const [member, setMember] = useState(null); // 회원 정보 상태
    const [isLoading, setIsLoading] = useState(true); // 로딩 상태
    const navigate = useNavigate();
    const { memberId } = useParams(); // URL에서 memberId 가져오기

    // 회원 정보 가져오기
    useEffect(() => {
        console.log("Fetching member with ID:", memberId);

        fetch(`${API_URL}members/${memberId}`, {
            method: 'GET',
            credentials: 'include',
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then((data) => {
                console.log("Fetched member data:", data); // 디버깅 로그 추가
                setMember(data); // 상태에 데이터 설정
                setIsLoading(false);
            })
            .catch((error) => {
                console.error("Error fetching member data:", error);
                setIsLoading(false);
            });
    }, [memberId]);


    // 회원 정보 업데이트 요청
    const handleUpdate = () => {
        fetch(`${API_URL}members/${memberId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(member),
        })
            .then((response) => response.json())
            .then(() => {
                navigate('/adminpage/members'); // 수정 후 목록으로 이동
            })
            .catch((error) => console.error(error));
    };

    if (isLoading) return <div>Loading...</div>;
    if (!member) return <div>회원 정보를 불러올 수 없습니다.</div>;

    return (
        <div className="edit-member-container">
            <h3>회원 수정</h3>
            <form className="edit-member-form">
                {/* ID (읽기 전용) */}
                <TextField
                    label="ID"
                    value={member?.id || ""}
                    InputProps={{ readOnly: true }} // 읽기 전용 필드
                    className="edit-member-field"
                />

                {/* 이름 */}
                <TextField
                    label="이름"
                    value={member?.name || ""} // 기존 이름 표시
                    onChange={(e) => setMember({ ...member, name: e.target.value })}
                    className="edit-member-field"
                />

                {/* 이메일 */}
                <TextField
                    label="이메일"
                    value={member?.email || ""} // 기존 이메일 표시
                    onChange={(e) => setMember({ ...member, email: e.target.value })}
                    className="edit-member-field"
                />

                {/* 생년월일 */}
                <TextField
                    label="생년월일"
                    type="date"
                    value={member?.dob || ""} // 기존 생년월일 표시
                    onChange={(e) => setMember({ ...member, dob: e.target.value })}
                    className="edit-member-field"
                    InputLabelProps={{ shrink: true }}
                />

                {/* 성별 */}
                <FormControl className="edit-member-field">
                    <InputLabel>성별</InputLabel>
                    <Select
                        value={member?.gender || ""} // 기존 성별 표시
                        onChange={(e) => setMember({ ...member, gender: e.target.value })}
                    >
                        <MenuItem value="남성">남성</MenuItem>
                        <MenuItem value="여성">여성</MenuItem>
                        <MenuItem value="기타">기타</MenuItem>
                    </Select>
                </FormControl>

                {/* 휴대폰번호 */}
                <TextField
                    label="휴대폰번호"
                    value={member?.phone || ""} // 기존 휴대폰번호 표시
                    onChange={(e) => setMember({ ...member, phone: e.target.value })}
                    className="edit-member-field"
                />

                {/* 활성 상태 */}
                <FormControl className="edit-member-field">
                    <InputLabel>활성 상태</InputLabel>
                    <Select
                        value={member?.status || "ACTIVE"} // "ACTIVE"를 기본값으로 설정
                        onChange={(e) => setMember({ ...member, status: e.target.value })}
                    >
                        <MenuItem value="ACTIVE">활성회원</MenuItem>
                        <MenuItem value="INACTIVE">휴면회원</MenuItem>
                        <MenuItem value="DELETED">탈퇴회원</MenuItem>
                    </Select>
                </FormControl>


                {/* 구독 여부 */}
                <FormControl className="edit-member-field">
                    <InputLabel>구독 여부</InputLabel>
                    <Select
                        value={member?.subscription ? "구독중" : "구독안함"} // 기존 구독 여부 표시
                        onChange={(e) =>
                            setMember({ ...member, subscription: e.target.value === "구독중" })
                        }
                    >
                        <MenuItem value="구독중">구독중</MenuItem>
                        <MenuItem value="구독안함">구독안함</MenuItem>
                    </Select>
                </FormControl>

                {/* 버튼 */}
                <div className="edit-member-buttons">
                    <Button variant="contained" color="primary" onClick={handleUpdate}>
                        수정
                    </Button>
                    <Button variant="outlined" color="secondary" onClick={() => navigate('/adminpage/members')}>
                        취소
                    </Button>
                </div>
            </form>
        </div>
    );
};

export default EditMember;
