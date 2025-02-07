import { useState, useEffect } from 'react';
import { TextField, Button, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { API_URL } from "../../../constant";
import { useNavigate, useParams } from "react-router-dom";
import './EditMember.css';

const EditMember = () => {
    const [member, setMember] = useState({
        id: '',
        name: '',
        email: '',
        phone: '',
        address: '',
        birthDate: '',
        gender: '',
        activate: false,
        role: ''
    });
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();
    const { memberId } = useParams();

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
            .then((responseData) => {
                if (responseData.status === 'success' && responseData.data) {
                    const memberData = responseData.data;
                    setMember({
                        id: memberData.id,
                        name: memberData.name || '',
                        email: memberData.email || '',
                        phone: memberData.phone || '',
                        address: memberData.address || '',
                        birthDate: memberData.birthDate || '',
                        gender: memberData.gender || '',
                        activate: memberData.activate || false,
                        role: memberData.role || ''
                    });
                    console.log("Updated member state:", memberData);
                }
                setIsLoading(false);
            })
            .catch((error) => {
                console.error("Error fetching member data:", error);
                setIsLoading(false);
            });
    }, [memberId]);

    // 회원 정보 업데이트 요청
    const handleUpdate = () => {
        const updateData = {
            name: member.name,
            email: member.email, // 이메일도 추가
            phone: member.phone,
            address: member.address,
            birthDate: member.birthDate || null,
            gender: member.gender || null,
            activate: member.activate
        };

        fetch(`${API_URL}members/${memberId}/update`, { // 엔드포인트를 '/update'로 변경
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'  // 이 부분 추가
            },
            credentials: 'include',
            body: JSON.stringify(updateData)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                alert('수정이 완료되었습니다.');
                navigate('/adminpage/members');
            } else {
                alert(data.message || '업데이트 실패');
            }
        })
        .catch(error => {
            console.error('Error updating member:', error);
            alert('업데이트 중 오류가 발생했습니다.');
        });
    };

    if (isLoading) return <div>Loading...</div>;
    if (!member) return <div>회원 정보를 불러올 수 없습니다.</div>;

    return (
        <div className="edit-member-container">
            <h3>회원 수정</h3>
            <form className="edit-member-form">
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
                    value={member.birthDate || ""}
                    onChange={(e) => setMember({ ...member, birthDate: e.target.value })}
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
                <TextField
                    label="주소"
                    value={member.address || ""}
                    onChange={(e) => setMember({ ...member, address: e.target.value })}
                    className="edit-member-field"
                />
                <FormControl className="edit-member-field">
                    <InputLabel>계정 상태</InputLabel>
                    <Select
                        value={member.activate ? "활성" : "비활성"}
                        onChange={(e) => setMember({ ...member, activate: e.target.value === "활성" })}
                    >
                        <MenuItem value="활성">활성</MenuItem>
                        <MenuItem value="비활성">비활성</MenuItem>
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