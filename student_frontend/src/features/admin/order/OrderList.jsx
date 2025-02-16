import { DataGrid } from '@mui/x-data-grid';
import { useState, useEffect } from 'react';
import { API_URL } from '@/utils/constants';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Typography, Tooltip } from '@mui/material';

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = () => {
    fetch(`${API_URL}orders/admin?page=0&size=10`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("accToken")}`
      },
      credentials: 'include'
    })
      .then(response => {
        if (!response.ok) throw new Error('데이터 가져오기 실패');
        return response.json();
      })
      .then(data => {
        setOrders(data.content);
      })
      .catch(error => console.error('에러 발생:', error));
  };

  // 주문 상태를 한글로 변환하는 함수
  const getOrderStatusKorean = (status) => {
    const statusMap = {
      'ORDERED': '주문완료',
      'PAYMENT_PENDING': '입금대기',
      'PAYMENT_COMPLETED': '결제완료',
      'PREPARING_SHIPMENT': '배송준비',
      'IN_TRANSIT': '배송중',
      'DELIVERED': '배송완료',
      'RETURN_REQUESTED': '반품요청',
      'CANCELED': '주문취소',
      'ORDER_COMPLETED': '주문완료'
    };
    return statusMap[status] || status;
  };

  const handleCancelClick = (order) => {
    setSelectedOrder(order);
    setErrorMessage(""); // 에러 메시지 초기화
    setDialogOpen(true);
  };

  const handleCancelConfirm = () => {
    if (!selectedOrder) return;

    // 서버 응답을 텍스트로 로깅하는 함수
    const logServerResponse = async (response) => {
      const text = await response.text();
      console.log('서버 응답 텍스트:', text);
      return text;
    };

    // 주의: 다른 URL 시도 (/api/admin/orders/...)
    fetch(`${API_URL}admin/orders/${selectedOrder.orderId}/cancel`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("accToken")}`
      },
      credentials: 'include'
    })
    .then(response => {
      if (!response.ok) {
        return logServerResponse(response).then(text => {
          if (response.status === 500) {
            if (text.includes("이미 취소된") || text.includes("배송 시작 이후")) {
              throw new Error(text);
            } else {
              throw new Error(`서버 내부 오류가 발생했습니다. 상세: ${text}`);
            }
          } else {
            throw new Error(`요청 처리 중 오류가 발생했습니다 (${response.status}): ${text}`);
          }
        });
      }
      return response.json();
    })
    .then(data => {
      alert("주문이 성공적으로 취소되었습니다");
      setDialogOpen(false);
      fetchOrders();
    })
    .catch(error => {
      console.error('주문 취소 에러:', error);
      setErrorMessage(error.message);
    });
  };

  const columns = [
    { field: 'orderId', headerName: '주문 ID', flex: 1 },
    { field: 'memberName', headerName: '회원 이름', flex: 2 },
    { field: 'productName', headerName: '상품명', flex: 2 },
    { field: 'quantity', headerName: '주문 수량', flex: 1 },
    { field: 'totalPrice', headerName: '금액', flex: 2 },
    { field: 'orderDate', headerName: '주문일자', flex: 2 },
    { field: 'shippingAddress', headerName: '주소', flex: 3 },
    { field: 'paymentMethod', headerName: '결제수단', flex: 2 },
    {
      field: 'orderStatus',
      headerName: '주문상태',
      flex: 1,
      renderCell: (params) => {
        const isCanceled = params.value === 'CANCELED';
        return (
          <Typography style={{ color: isCanceled ? 'red' : 'inherit' }}>
            {getOrderStatusKorean(params.value)}
          </Typography>
        );
      }
    },
    {
      field: 'manage',
      headerName: '관리',
      flex: 2,
      renderCell: (params) => {
        const isCanceled = params.row.orderStatus === 'CANCELED';
        const isDelivered = ['IN_TRANSIT', 'DELIVERED', 'ORDER_COMPLETED'].includes(params.row.orderStatus);

        // 배송 시작 이후에는 특별 메시지 표시
        let tooltipMessage = "";
        if (isCanceled) {
          tooltipMessage = "이미 취소된 주문입니다";
        } else if (isDelivered) {
          tooltipMessage = "배송 시작 이후의 주문은 취소할 수 없습니다";
        }

        return (
          <Tooltip
            title={tooltipMessage}
            disableHoverListener={!isCanceled && !isDelivered}
          >
            <span>
              <Button
                variant="contained"
                color={isCanceled ? 'default' : 'secondary'}
                disabled={isCanceled || isDelivered}
                onClick={() => handleCancelClick(params.row)}
              >
                {isCanceled ? '주문 취소 완료' : '주문 취소'}
              </Button>
            </span>
          </Tooltip>
        );
      }
    }
  ];

  return (
    <div>
      <h3>관리자 주문 내역</h3>
      <DataGrid
        rows={orders}
        columns={columns}
        pageSize={10}
        autoHeight
      />

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>주문 취소 확인</DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <>
              <Typography>주문 번호: {selectedOrder.orderId}</Typography>
              <Typography>현재 주문 상태: {getOrderStatusKorean(selectedOrder.orderStatus)}</Typography>
              <Typography>이 주문을 취소하시겠습니까?</Typography>
              {errorMessage && (
                <Typography color="error" style={{marginTop: '16px'}}>
                  오류: {errorMessage}
                </Typography>
              )}
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>취소</Button>
          <Button onClick={handleCancelConfirm} color="secondary" disabled={!!errorMessage}>
            확인
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default OrderList;