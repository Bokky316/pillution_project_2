import { useState, useEffect } from 'react';
import { TextField, Button, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { API_URL } from "../../../constant";
import { useNavigate, useParams } from "react-router-dom";
import './EditMember.css'; // CSS 파일 추가

const EditMember = () => {
    const [member, setMember] = useState({
      id: '',
      name: '',
      email: '',
      dob: '',
      gender: '',
      phone: '',
      status: '',
      subscription: false
    });
    const [isLoading, setIsLoading] = useState(true); // 로딩 상태
    const navigate = useNavigate();
    const { memberId } = useParams(); // URL에서 memberId 가져오기

    // 회원 정보 가져오기
    useEffect(() => {
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
          const validStatus = ['ACTIVE', 'INACTIVE', 'DELETED'].includes(data.status) ? data.status : '';
          const updatedMember = {
            ...data,
            status: validStatus,
            dob: data.birthDate || '', // birthDate를 dob로 매핑
            subscription: data.activate || false // activate를 subscription으로 매핑
          };
          setMember(updatedMember);
          console.log("Updated member state:", JSON.stringify(updatedMember, null, 2));
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
                {/* 폼 필드들... */}
                <TextField
                    label="ID"
                    value={member.id || ""}
                    InputProps={{ readOnly: true }}
                    className="edit-member-field"
                />
                <TextField
                    label="이름"
                    value={member.name || ""}
                    onChange={(e) => setMember({ ...member, name: e.target.value })}
                    className="edit-member-field"
                />
                <TextField
                    label="이메일"
                    value={member.email || ""}
                    onChange={(e) => setMember({ ...member, email: e.target.value })}
                    className="edit-member-field"
                />
                <TextField
                    label="생년월일"
                    type="date"
                    value={member.dob || ""}
                    onChange={(e) => setMember({ ...member, dob: e.target.value })}
                    className="edit-member-field"
                    InputLabelProps={{ shrink: true }}
                />
                <FormControl className="edit-member-field">
                    <InputLabel>성별</InputLabel>
                    <Select
                        value={member.gender || ""}
                        onChange={(e) => setMember({ ...member, gender: e.target.value })}
                    >
                        <MenuItem value="남성">남성</MenuItem>
                        <MenuItem value="여성">여성</MenuItem>
                        <MenuItem value="기타">기타</MenuItem>
                    </Select>
                </FormControl>
                <TextField
                    label="휴대폰번호"
                    value={member.phone || ""}
                    onChange={(e) => setMember({ ...member, phone: e.target.value })}
                    className="edit-member-field"
                />
                <FormControl className="edit-member-field">
                    <InputLabel id="status-label">활성 상태</InputLabel>
                    <Select
                        labelId="status-label"
                        value={member.status || ''}
                        onChange={(e) => setMember({ ...member, status: e.target.value })}
                        label="활성 상태"
                    >
                        <MenuItem value="ACTIVE">활성회원</MenuItem>
                        <MenuItem value="INACTIVE">휴면회원</MenuItem>
                        <MenuItem value="DELETED">탈퇴회원</MenuItem>
                    </Select>
                </FormControl>
                <FormControl className="edit-member-field">
                    <InputLabel id="subscription-label">구독 여부</InputLabel>
                    <Select
                        labelId="subscription-label"
                        value={member.subscription ? "구독중" : "구독안함"}
                        onChange={(e) =>
                            setMember({ ...member, subscription: e.target.value === "구독중" })
                        }
                        label="구독 여부"
                    >
                        <MenuItem value="구독중">구독중</MenuItem>
                        <MenuItem value="구독안함">구독안함</MenuItem>
                    </Select>
                </FormControl>
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
