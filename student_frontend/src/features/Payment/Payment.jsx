// import React, { useEffect, useState } from "react";
// import { Button, Radio, RadioGroup, FormControlLabel, FormControl, Paper, Typography, Box } from "@mui/material";
// import { API_URL } from "@/utils/constants";
// import { fetchWithAuth } from "@/features/auth/fetchWithAuth";
// import { useNavigate, useLocation } from "react-router-dom";
// import "@/styles/Payment.css";
//
// const Payment = ({ orderId, merchantId, items, totalPrice, zipCode, address1, address2, purchaseType }) => {
//   const navigate = useNavigate();
//   const location = useLocation();
//   const { selectedItems, totalAmount, user: userData } = location.state || { selectedItems: [], totalAmount: 0, user: {} };
//
//   const [name, setName] = useState(userData?.name || '');
//   const [email, setEmail] = useState(userData?.email || '');
//   const [phone, setPhone] = useState(userData?.phone || '');
//   const [address, setAddress] = useState(userData?.address || '');
//   const [paymentMethod, setPaymentMethod] = useState("kakaopay");
//   const [isImpReady, setIsImpReady] = useState(false);
//
//   useEffect(() => {
//     const script = document.createElement("script");
//     script.src = "https://cdn.iamport.kr/js/iamport.payment-1.2.0.js";
//     script.async = true;
//     script.onload = () => {
//       if (window.IMP) {
//         window.IMP.init(merchantId);
//         setIsImpReady(true);
//       }
//     };
//     document.body.appendChild(script);
//     return () => {
//       document.body.removeChild(script);
//     };
//   }, [merchantId]);
//
//   const handlePaymentMethodChange = (event) => {
//     setPaymentMethod(event.target.value);
//   };
//
//   const handlePayment = async () => {
//     if (!isImpReady) {
//       alert("결제 모듈이 아직 로드되지 않았습니다. 잠시 후 다시 시도해주세요.");
//       return;
//     }
//
//     const IMP = window.IMP;
//
//     let pg = "";
//     if (paymentMethod === "kakaopay") {
//       pg = "kakaopay";
//     } else if (paymentMethod === "payco") {
//       pg = "payco";
//     }
//
//     const paymentData = {
//       pg: pg,
//       pay_method: "card",
//       merchant_uid: orderId,
//       name: selectedItems[0].name,
//       amount: totalAmount,
//       buyer_email: email,
//       buyer_name: name,
//       buyer_tel: phone,
//       buyer_addr: `${address1} ${address2}`,
//       buyer_postcode: zipCode,
//       m_redirect_url: "http://localhost:3000/payResult"
//     };
//
//     IMP.request_pay(paymentData, async (rsp) => {
//       if (rsp.success) {
//         alert("결제가 완료되었습니다!");
//         console.log("결제 완료 응답:", rsp);
//         const response = await processPayment(rsp);
//         if (response.ok) {
//           const data = await response.json();
//           navigate("/payResult", { state: { paymentInfo: data } });
//         }
//       } else {
//         alert(`결제 실패: ${rsp.error_msg}`);
//       }
//     });
//   };
//
//   const processPayment = async (rsp) => {
//     const paymentRequest = {
//       impUid: rsp.imp_uid,
//       merchantUid: orderId,
//       paidAmount: rsp.paid_amount,
//       name: rsp.name,
//       pgProvider: rsp.pg_provider,
//       buyerEmail: rsp.buyer_email,
//       buyerName: rsp.buyer_name,
//       buyTel: rsp.buyer_tel,
//       buyerAddr: rsp.buyer_addr,
//       buyerPostcode: rsp.buyer_postcode,
//       paidAt: rsp.paid_at,
//       status: "PAYMENT_COMPLETED",
//     };
//
//     return fetchWithAuth(`${API_URL}payments/request?purchaseType=${purchaseType}`, {
//       method: "POST",
//       headers: {
//         'Content-Type': 'application/json'
//       },
//       body: JSON.stringify(paymentRequest),
//     });
//   };
//
//   return (
//     <Paper elevation={3} className="payment-container">
//       <Typography variant="h6" gutterBottom className="payment-title">
//         결제 정보 입력
//       </Typography>
//       <Typography variant="body1" className="order-info">
//         주문 번호: {orderId}
//       </Typography>
//       <Typography variant="body1" className="order-info">
//         총 결제 금액: {totalPrice}원
//       </Typography>
//
//       <FormControl component="fieldset" className="payment-method-select">
//         <Typography variant="subtitle1" gutterBottom>
//           결제 수단 선택
//         </Typography>
//         <RadioGroup
//           aria-label="paymentMethod"
//           name="paymentMethod"
//           value={paymentMethod}
//           onChange={handlePaymentMethodChange}
//         >
//           <FormControlLabel value="kakaopay" control={<Radio />} label="카카오페이" />
//           <FormControlLabel value="payco" control={<Radio />} label="페이코" />
//           <FormControlLabel value="card" control={<Radio />} label="신용카드" />
//         </RadioGroup>
//       </FormControl>
//
//       <Box mt={2} textAlign="center">
//         <Button
//           variant="contained"
//           color="primary"
//           size="large"
//           onClick={handlePayment}
//           disabled={!isImpReady}
//         >
//           {isImpReady ? (paymentMethod === 'card' ? '신용카드로 결제하기' : `${paymentMethod}로 결제하기`) : '결제 모듈 로딩 중...'}
//         </Button>
//       </Box>
//     </Paper>
//   );
// };
//
// export default Payment;
